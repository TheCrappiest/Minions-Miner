package com.thecrappiest.minions.miner.listeners.custom;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.events.CreateMinionEntityEvent;
import com.thecrappiest.minions.items.ItemCreation;
import com.thecrappiest.minions.items.ItemNBT;
import com.thecrappiest.minions.items.SkullCreation;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.maps.miniondata.ThreadsHolder;
import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.messages.Messages;
import com.thecrappiest.minions.methods.ConversionMethods;
import com.thecrappiest.minions.methods.MinionEntityMethods;
import com.thecrappiest.minions.methods.NumberUtil;
import com.thecrappiest.minions.methods.PermissionMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.minions.threads.MovementThread;
import com.thecrappiest.objects.Minion;

public class CreateMinionEntity implements Listener {

	public final MinerCore minerCore;
	public CreateMinionEntity(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onCreateMinionEntity(CreateMinionEntityEvent event) {
		Player player = event.getPlayer();
		String minionType = event.getMinionType();
		Location location = event.getPlaceLocation();
		
		if(!minionType.equalsIgnoreCase("MINER")) {return;}
		
		// * Variables incase only a UUID was used to activate this event
		String ownerName = null;
		UUID uuid = null;
		if(player == null) {
			if(event.getUUID() != null) {
				uuid = event.getUUID();
				OfflinePlayer offPlay = Bukkit.getOfflinePlayer(uuid);
				ownerName = offPlay.getName();
			}
		}else {
			ownerName = player.getName();
			uuid = player.getUniqueId();
		}
		
		if(event.getPath() != null) {
			String path = event.getPath().split("\\.")[1];
			location = ConversionMethods.getLocationFromString(path);
		}
		
		// * Stops the minion from being created if the world is disabled
		if(minerCore.getConfig().getStringList("Disabled_Worlds").contains(location.getWorld().getName())) {
			ConsoleOutput.warn("CreationFailure >> Minion at W:"+location.getWorld().getName()+
					", X:"+location.getBlockX()+", Y:"+location.getBlockY()+", Z:"+location.getBlockZ()+" has failed to load.");
			ConsoleOutput.warn("Reason: DISABLED_WORLD");
			event.setCancelled(true);
		    return;
		}
		
		if(!location.getWorld().getNearbyEntities(location, .5, .5, .5).isEmpty()) {
			location.getWorld().getNearbyEntities(location,.5,.5,.5).forEach(entity -> {
				if(entity instanceof ArmorStand) {
					entity.remove();
				}
			});
		}
		
		if(event.isCancelled()) {return;}
		
		YamlConfiguration entityData = MinerConfigurations.getInstance().getYaml("entity");
		YamlConfiguration entitySettings = MinerConfigurations.getInstance().getYaml("settings");
		
		ArmorStand armorstand = (ArmorStand) MinionEntityMethods.generateMinionEntity(location);
		
		if(entityData.getString("Size").equalsIgnoreCase("small")) {
			armorstand.setSmall(true);
		}else {
			armorstand.setSmall(false);
		}
		
		armorstand.getEquipment().setHelmet(
				ItemNBT.getNBTUtils().addNBTTagString(
						SkullCreation.getInstance().createSkullItem(entityData.getString("Head").replace("%minion_owner%", ownerName))
						, "MinionsHelmet", "DEFAULT", null));
		
		armorstand.getEquipment().setChestplate(
				ItemNBT.getNBTUtils().addNBTTagString(
						ItemCreation.createItem(entityData.getConfigurationSection("Chestplate"), null)
						, "MinionsChestplate", "DEFAULT", null));
		
		armorstand.getEquipment().setLeggings(
				ItemNBT.getNBTUtils().addNBTTagString(
						ItemCreation.createItem(entityData.getConfigurationSection("Leggings"), null)
						, "MinionsLeggings", "DEFAULT", null));
		
		armorstand.getEquipment().setBoots(
				ItemNBT.getNBTUtils().addNBTTagString(
						ItemCreation.createItem(entityData.getConfigurationSection("Boots"), null)
						, "MinionsBoots", "DEFAULT", null));
		
		armorstand.getEquipment().setItemInMainHand(
				ItemNBT.getNBTUtils().addNBTTagString(
						ItemCreation.createItem(entityData.getConfigurationSection("Held_Item"), null)
						, "MinionsHeldItem", "DEFAULT", null));
		
		armorstand.setCustomName(Messages.util().addColor(entityData.getString("Entity_Name")));
		armorstand.setCustomNameVisible(entityData.getBoolean("Show_Name"));
		
		Minion miner = new Minion(armorstand, armorstand.getUniqueId(), uuid, "MINER");
		MinionData.getInstance().loadedMinions.put(armorstand.getUniqueId(), miner);
		
		miner.setPlaceHolder("%minion_blocksmined%", "0");
		miner.setPlaceHolder("%minion_collectedexp%", "0");
		
		// * Starting the minions movement thread and setting the default delay		
		miner.setMovementDelay(NumberUtil.delayConverter(entitySettings.getDouble("Default_Movement_Speed")));
		if(player != null) {
			double permissionDelay = PermissionMethods.getLowestValue(player, entitySettings.getStringList("Movement_Speed_Permissions"));
			if(permissionDelay > -1) {
				miner.setMovementDelay(NumberUtil.delayConverter(permissionDelay));
			}
		}
		
		if(event.shouldMove()) {
			MovementThread movementThread = ThreadsHolder.getInstance().getMovementThread(miner);
			if(!movementThread.getMinionDelays().containsKey(miner)) {
				movementThread.addMinion(miner);
			}
		}
		
		// * Adjusting chunk settings
		if(entitySettings.getBoolean("Keep_Chunk_Loaded")) {
			armorstand.getLocation().getChunk().setForceLoaded(true);
		}
		
		// * Setting the default health and hunger settings
		miner.addDefaultHealthAndHungerSettings();
		
		// * If empty will generate default placeholders
		miner.getPlaceHolders();
		miner.setMovementDelayPlaceholder();
		
		ItemStack minionItem = event.getMinionItem();
		if(minionItem != null) {
			ItemNBT nbt = ItemNBT.getNBTUtils();
			if(nbt.itemContainsNBTTag(minionItem, "Health")) {
				miner.setHealth(Integer.valueOf(nbt.getStringFromNBT(minionItem, "Health")));
				miner.setPlaceHolder("%minion_health%", String.valueOf(miner.getHealth()));
			}
			if(nbt.itemContainsNBTTag(minionItem, "MaxHealth")) {
				miner.setMaxHealth(Integer.valueOf(nbt.getStringFromNBT(minionItem, "MaxHealth")));
				miner.setPlaceHolder("%minion_max_health%", String.valueOf(miner.getMaxHealth()));
			}
			if(nbt.itemContainsNBTTag(minionItem, "Hunger")) {
				miner.setHunger(Integer.valueOf(nbt.getStringFromNBT(minionItem, "Hunger")));
				miner.setPlaceHolder("%minion_hunger%", String.valueOf(miner.getHunger()));
			}
			if(nbt.itemContainsNBTTag(minionItem, "MaxHunger")) {
				miner.setHunger(Integer.valueOf(nbt.getStringFromNBT(minionItem, "MaxHunger")));
				miner.setPlaceHolder("%minion_max_hunger%", String.valueOf(miner.getMaxHunger()));
			}
		}
		
		// * Creates a new miner object and adds it to the miners map
		Miner minerOBJ = new Miner(miner.getEntity(), miner.getEntityID(), miner.getOwner(), miner.getType());
		MinerData.getInstance().miners.put(miner, minerOBJ);
		
		// * Adds blocks that give exp to map
	    if(entitySettings.isSet("EXP_Blocks") && !entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false).isEmpty()) {
	    	for(String blockType : entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false)) {
	    		Material material = Material.valueOf(blockType);
	    		if(material != null && material != Material.AIR) {
	    			minerOBJ.addEXPForBlock(material, entitySettings.getInt("EXP_Blocks."+blockType));
	    		}
	    	}
	    }
	    
	    // * Adds list of mineable block types
	    if(entitySettings.isSet("Mineable")) {
	    	for(String blockType : entitySettings.getStringList("Mineable")) {
	    		Material material = Material.valueOf(blockType);
	    		if(material != null && material != Material.AIR) {
	    			minerOBJ.addMinableBlock(material);
	    		}
	    	}
	    }
	    
	    // * Sets whether or not exp should be bottled
	    minerOBJ.bottleEXP(entitySettings.getBoolean("Bottle_EXP"));
	    
	    if(event.getMinionItem() != null) {
	    	ItemStack item = event.getMinionItem();
	    	if(ItemNBT.getNBTUtils().itemContainsNBTTag(item, "BlocksMined")) {
	    		miner.setPlaceHolder("%minion_blocksmined%", String.valueOf(ItemNBT.getNBTUtils().getStringFromNBT(item, "BlocksMined")));
	    		minerOBJ.setBlocksMined(Integer.valueOf(ItemNBT.getNBTUtils().getStringFromNBT(item, "BlocksMined")));
	    	}
	    	if(ItemNBT.getNBTUtils().itemContainsNBTTag(item, "CollectedEXP")) {
	    		miner.setPlaceHolder("%minion_collectedexp%", String.valueOf(ItemNBT.getNBTUtils().getStringFromNBT(item, "CollectedEXP")));
	    		minerOBJ.setCollectedEXP(Integer.valueOf(ItemNBT.getNBTUtils().getStringFromNBT(item, "CollectedEXP")));
	    	}
	    }
	    
		new BukkitRunnable() {
			public void run() {
				
				// * Checking if the minion is being loaded with data
				if(event.getData() != null) {
					JSONObject jsonData = null;
					try {
						jsonData = (JSONObject) new JSONParser().parse(event.getData());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					if(jsonData != null) {
						// * Checking for a saved delay time
						if(jsonData.containsKey("Delay")) {
							miner.setMovementDelay(Integer.valueOf(jsonData.get("Delay").toString()));
							miner.getPlaceHolders();
							miner.setMovementDelayPlaceholder();
						}
						
						// * Checking for a saved held item
						if(jsonData.containsKey("HeldItemData")) {
							armorstand.getEquipment().setItemInMainHand(ItemCreation.createItem(jsonData.get("HeldItemData").toString(), null));
						}
						
						// * Checks for saved health
						if(jsonData.containsKey("Health")) {
							miner.setHealth(Integer.valueOf(jsonData.get("Health").toString()));
							miner.setPlaceHolder("%minion_health%", String.valueOf(miner.getHealth()));
						}
						
						// * Checks for saved hunger
						if(jsonData.containsKey("Hunger")) {
							miner.setHunger(Integer.valueOf(jsonData.get("Hunger").toString()));
							miner.setPlaceHolder("%minion_hunger%", String.valueOf(miner.getHunger()));
						}
						
						if(jsonData.containsKey("Link_Chest")) {
							Location linkChest = ConversionMethods.getLocationFromString(jsonData.get("Link_Chest").toString());
							miner.addLinkedChest("Link_Chest", linkChest);
						}
						
						if(jsonData.containsKey("Link_Food_Chest")) {
							Location linkChest = ConversionMethods.getLocationFromString(jsonData.get("Link_Food_Chest").toString());
							miner.addLinkedChest("Link_Food_Chest", linkChest);
						}
						
						if(jsonData.containsKey("CollectedEXP")) {
							minerOBJ.setCollectedEXP(Integer.valueOf(jsonData.get("CollectedEXP").toString()));
							miner.setPlaceHolder("%minion_collectedexp%", String.valueOf(minerOBJ.getCollectedEXP()));
						}
						
						if(jsonData.containsKey("BlocksMined")) {
							minerOBJ.setBlocksMined(Integer.valueOf(jsonData.get("BlocksMined").toString()));
							miner.setPlaceHolder("%minion_blocksmined%", String.valueOf(minerOBJ.getBlocksMined()));
						}
						
						// * Checking if the minion is enabled from saved data
						if(jsonData.containsKey("Enabled")) {
							if(!Boolean.valueOf(jsonData.get("Enabled").toString())) {
								miner.setEnabled(false);
								miner.setPlaceHolder("%minion_enabled%", String.valueOf(false));
							}
						}
					}
				}
				
				// * Setting the minion as loaded (Will allow the minion to be interacted with)
				miner.setLoaded(true);
			}
		}.runTaskLaterAsynchronously(Core.getInstance(), 20);
	}
	
}
