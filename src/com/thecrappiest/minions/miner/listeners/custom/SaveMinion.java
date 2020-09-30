package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.thecrappiest.minions.events.SaveMinionEvent;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class SaveMinion implements Listener {

	public final MinerCore minerCore;
	public SaveMinion(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onMinionSave(SaveMinionEvent event) {
		Minion minion = event.getMinion();
		Miner miner = MinerData.getInstance().getMinerFromMinion(minion);
		
		if(miner == null) {return;}
		
		minion.addSaveData("CollectedEXP", miner.getCollectedEXP());
		minion.addSaveData("BlocksMined", miner.getBlocksMined());
	}
	
}
