package com.thecrappiest.minions.miner.listeners.custom;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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

	public final MinerCore minerCore;
	public PickupMinion(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onPickupMinion(PickupMinionEvent event) {
		Player player = event.getPlayer();
		Minion minion = event.getMinion();
		// Location location = event.getLocation();
		
		if(event.isCancelled()) {return;}
		if(!minion.getType().equalsIgnoreCase("MINER")) {return;}
		
		Map<String, String> nbtTags = new HashMap<>();
		
		if(minion.useHealth() && minion.getHealth() != -1) {
			nbtTags.put("Health", String.valueOf(minion.getHealth()));
			if(minion.getMaxHealth() != -1) {
				nbtTags.put("MaxHealth", String.valueOf(minion.getMaxHealth()));
			}
		}
		
		if(minion.useHunger() && minion.getHunger() != -1) {
			nbtTags.put("Hunger", String.valueOf(minion.getHunger()));
			if(minion.getMaxHunger() != -1) {
				nbtTags.put("MaxHunger", String.valueOf(minion.getMaxHunger()));
			}
		}
		
		Miner miner = MinerData.getInstance().getMinerFromMinion(minion);
		nbtTags.put("BlocksMined", String.valueOf(miner.getBlocksMined()));
		nbtTags.put("CollectedEXP", String.valueOf(miner.getCollectedEXP()));
		
		player.getInventory().addItem(
				ItemNBT.getNBTUtils().addNBTTagString(
						ItemCreation.createItem(MinerConfigurations.getInstance().getYaml("item").getConfigurationSection(""), minion.getPlaceHolders())
						, "MinionType", "Miner", nbtTags));
		
		MinionData.getInstance().unloadMinion(minion, true);
		MinerData.getInstance().miners.remove(minion);
	}
	
}
