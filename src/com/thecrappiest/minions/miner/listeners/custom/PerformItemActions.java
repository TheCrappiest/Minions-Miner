package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.thecrappiest.minions.events.PerformItemActionsEvent;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class PerformItemActions implements Listener {

	public PerformItemActions(MinerCore minerCore) {
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onItemAction(PerformItemActionsEvent event) {
		
		// * Sets variables used by the event
		Minion minion = event.getMinion();
		
		if(!(minion instanceof Miner)) {return;}
		
		Player player = event.getPlayer();
		Miner miner = (Miner) minion;
		
		// * Creates switch for actions used
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
