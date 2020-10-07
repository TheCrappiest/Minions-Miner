package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.thecrappiest.minions.events.AutoLoadMinionsEvent;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.methods.LoadMiners;

public class AutoLoadMiners implements Listener {

	public final MinerCore minerCore;
	public AutoLoadMiners(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onAutoLoad(AutoLoadMinionsEvent event) {
		LoadMiners.AutoloadMinerMinions();
	}
	
}
