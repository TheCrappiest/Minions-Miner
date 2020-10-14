package com.thecrappiest.minions.miner.listeners.custom;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.events.InteractWithMinionEvent;
import com.thecrappiest.minions.items.ItemNBT;
import com.thecrappiest.minions.methods.NumberUtil;
import com.thecrappiest.minions.methods.PermissionMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.threads.GenerateMinionInventory;
import com.thecrappiest.objects.Minion;

public class InteractWithMinion implements Listener {

	public final MinerCore minerCore;
	public InteractWithMinion(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true)
	public void onInteractWithMinion(InteractWithMinionEvent event) {
		Player player = event.getPlayer();
		Minion minion = event.getMinion();
		ItemStack interactionItem = event.getInteractionItem();
		
		// * Test if minion type is a miner
		if(!minion.getType().equalsIgnoreCase("MINER")) {return;}
		
		// * Tests if minion belongs to player
		if(!minion.getOwner().equals(player.getUniqueId())) {return;}
		
		if(!player.isSneaking()) {
			// * Tests if player is holding an item
			if(interactionItem == null || (interactionItem != null && interactionItem.getType() == Material.AIR)) {
				
				// * Creates and sets variables of the inventory creation thread
				GenerateMinionInventory generate_minion_inventory_thread = new GenerateMinionInventory();
			    generate_minion_inventory_thread.setMinion(minion);
			    generate_minion_inventory_thread.setPlayer(player);
			    generate_minion_inventory_thread.setYaml(MinerConfigurations.getInstance().getYaml("inventory"));
			    
			    // * Creates and starts the inventory generation thread
			    Thread thread = new Thread(generate_minion_inventory_thread);
			    generate_minion_inventory_thread.startThread(thread);
			    
			}else if(interactionItem != null && interactionItem.getType() != Material.AIR) {
				YamlConfiguration yaml = MinerConfigurations.getInstance().getYaml("entity");
				
				// * Checks if the player has permission to change the minions item
				if(!player.hasPermission(yaml.getString("Give_Item_Permission"))) {return;}
				
				// * Tests if the interaction item is allowed to be held by the minion
				if(yaml.getStringList("Holdable_Materials").contains(interactionItem.getType().name())) {
					ArmorStand as = (ArmorStand) minion.getEntity();
					ItemStack heldItem = null;
					
					if(Core.isLegacy()) {
						heldItem = as.getEquipment().getItemInHand();
					}else {
						heldItem = as.getEquipment().getItemInMainHand();
					}
					
					// * If minion is holding a non default item it will be given to the player
					if(!ItemNBT.getNBTUtils().itemContainsNBTTag(heldItem, "MinionsHeldItem")) {
						// * Tests if the item matches the item the minion is already holding
						if(!interactionItem.isSimilar(heldItem)) {
							if(player.getInventory().firstEmpty() == -1) {
								player.getWorld().dropItemNaturally(player.getLocation(), heldItem);
							}else {
								player.getInventory().addItem(heldItem);
							}
						}
					}
					
					// * Sets the new item in minions hand
					if(Core.isLegacy()) {
						as.getEquipment().setItemInHand(interactionItem);
					}else {
						as.getEquipment().setItemInMainHand(interactionItem);
					}
					
					// * Removes item from players inventory
					if(interactionItem.getAmount() > 1) {
						ItemStack cloneItem = interactionItem.clone();
						cloneItem.setAmount(1);
						player.getInventory().removeItem(cloneItem);
					}else {
					    player.getInventory().removeItem(interactionItem);
				    }
					
					// * Map of items enchantments
					Map<Enchantment, Integer> enchants = interactionItem.getEnchantments();
					
					boolean altered_Movement_Delay = false;
					
					// * Checks if item contains enchants
					if(!enchants.isEmpty()) {
						if(enchants.containsKey(Enchantment.DIG_SPEED)) {
							int level = interactionItem.getEnchantmentLevel(Enchantment.DIG_SPEED);
							int delay = minion.getMovementDelay();
							
							int levelConversion = level*10;
							
							// * Sets the new delay from efficiency level
							if(delay - levelConversion <= 10) {
								minion.setMovementDelay(10);
							}else {
								minion.setMovementDelay(delay-levelConversion);
							}
							
							// * Sets the delay placeholder
							minion.setMovementDelayPlaceholder();
							altered_Movement_Delay = true;
						}
					}
					
					if(!altered_Movement_Delay) {
                        YamlConfiguration entitySettings = MinerConfigurations.getInstance().getYaml("settings");
						
						// * Setting the minions movement delay to its default	
						minion.setMovementDelay(NumberUtil.delayConverter(entitySettings.getDouble("Default_Movement_Speed")));
						if(player != null) {
							double permissionDelay = PermissionMethods.getLowestValue(player, entitySettings.getStringList("Movement_Speed_Permissions"));
							if(permissionDelay > -1) {
								minion.setMovementDelay(NumberUtil.delayConverter(permissionDelay));
							}
						}
						minion.setMovementDelayPlaceholder();
					}
				}
			}
		}
	}
	
}
