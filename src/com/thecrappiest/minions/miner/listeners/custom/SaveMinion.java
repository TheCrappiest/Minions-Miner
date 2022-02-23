package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.thecrappiest.minions.events.SaveMinionEvent;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class SaveMinion implements Listener {

	public SaveMinion(MinerCore minerCore) {
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onMinionSave(SaveMinionEvent event) {
		Minion minion = event.getMinion();
		if(!(minion instanceof Miner)) return;
		
		Miner miner = (Miner) minion;
		
		miner.addSaveData("CollectedEXP", miner.getCollectedEXP());
		miner.addSaveData("BlocksMined", miner.getBlocksMined());
	}
	
}
