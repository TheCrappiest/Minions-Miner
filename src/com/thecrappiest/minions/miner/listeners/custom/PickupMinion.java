package com.thecrappiest.minions.miner.listeners.custom;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.events.PickupMinionEvent;
import com.thecrappiest.minions.items.ItemCreation;
import com.thecrappiest.minions.items.ItemNBT;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class PickupMinion implements Listener {

	public PickupMinion(MinerCore minerCore) {
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true)
	public void onPickupMinion(PickupMinionEvent event) {
		Player player = event.getPlayer();
		Minion minion = event.getMinion();
		
		// * Tests if minion is a miner
		if(!(minion instanceof Miner)) {return;}
		
		// * Creates map that will have data stored to it
		Map<String, String> nbtTags = new HashMap<>();
		
		// * Tests if health should be added to stored data
		if(minion.useHealth() && minion.getHealth() != -1) {
			nbtTags.put("Health", String.valueOf(minion.getHealth()));
			if(minion.getMaxHealth() != -1) {
				nbtTags.put("MaxHealth", String.valueOf(minion.getMaxHealth()));
			}
		}
		
		// * Tests if hunger should be added to stored data
		if(minion.useHunger() && minion.getHunger() != -1) {
			nbtTags.put("Hunger", String.valueOf(minion.getHunger()));
			if(minion.getMaxHunger() != -1) {
				nbtTags.put("MaxHunger", String.valueOf(minion.getMaxHunger()));
			}
		}
		
		ArmorStand as = (ArmorStand) minion.getEntity();
		ItemStack heldItem = Core.isLegacy() ? as.getEquipment().getItemInHand() : as.getEquipment().getItemInMainHand();
		
		// * If minion is holding a non default item it will be given to the player
		if(!ItemNBT.getNBTUtils().itemContainsNBTTag(heldItem, "MinionsHeldItem")) {
			if(player.getInventory().firstEmpty() == -1) {
				player.getWorld().dropItemNaturally(player.getLocation(), heldItem);
			}else {
				player.getInventory().addItem(heldItem);
			}
		}
		
		// * Gets miner object and stores the miner object data
		Miner miner = (Miner) minion;
		nbtTags.put("BlocksMined", String.valueOf(miner.getBlocksMined()));
		nbtTags.put("CollectedEXP", String.valueOf(miner.getCollectedEXP()));
		
		YamlConfiguration itemData = MinerConfigurations.getInstance().getYaml("item");
		
		// * Adds the minion item to the players inventory
		player.getInventory().addItem(
				ItemNBT.getNBTUtils().addNBTTagString(
						ItemCreation.createItem(itemData.getConfigurationSection(""), minion.getPlaceHolders())
						, "MinionType", itemData.getString("NBT_Tags.MinionType"), nbtTags));
		
		// * Unloads and removes the miner and minion data
		MinionData.getInstance().unloadMinion(minion, true);
		MinerData.getInstance().miners.remove(minion);
	}
	
}
