package com.thecrappiest.minions.miner.listeners.miniontask;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.thecrappiest.minions.events.MinionLastPoseEvent;
import com.thecrappiest.minions.events.MinionPerformTaskEvent;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class LastPose implements Listener {

	public final MinerCore minerCore;
	public LastPose(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onLastPose(MinionLastPoseEvent event) {
		// * Tests if minion is a miner
		if(!event.getMinion().getType().equalsIgnoreCase("MINER")) {return;}
		
		// * Sets variables used in the event
		Minion minion = event.getMinion();
		
		// * Sets variable for miner object
		Miner miner = MinerData.getInstance().getMinerFromMinion(minion);
		
		// * Returns if a miner object was not found
		if(miner == null) {return;}
		
		// * Creates and calls the MinionPerformTaskEvent (So MinionLastPoseEvent can finish and not hold up the server.)
		MinionPerformTaskEvent minionPerformTask = new MinionPerformTaskEvent(minion);
		Bukkit.getPluginManager().callEvent(minionPerformTask);
	}
	
}
