package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.thecrappiest.minions.events.PerformItemActionsEvent;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class PerformItemActions implements Listener {

	public final MinerCore minerCore;
	public PerformItemActions(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onItemAction(PerformItemActionsEvent event) {
		Minion minion = event.getMinion();
		Player player = event.getPlayer();
		Miner miner = MinerData.getInstance().getMinerFromMinion(minion);
		
		switch(event.getAction().toUpperCase()) {
		case "COLLECT_EXP":
			if(miner.getCollectedEXP() > 0) {
				player.giveExp(miner.getCollectedEXP());
				miner.setCollectedEXP(0);
				minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(0));
			}
			break;
		}
	}

}
