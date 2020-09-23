package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.thecrappiest.minions.events.ReloadPluginEvent;
import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;

public class ReloadPlugin implements Listener {

	public final MinerCore minerCore;
	public ReloadPlugin(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onPluginReload(ReloadPluginEvent event) {
		MinerConfigurations.clear(true);
		
		ConsoleOutput.info("Configuration File Loader:");
		ConsoleOutput.info(" ");
		ConsoleOutput.info("Miner Minion Configurations:");
		MinerConfigurations.getInstance().loadConfig("entity");
		MinerConfigurations.getInstance().loadConfig("inventory");
		MinerConfigurations.getInstance().loadConfig("item");
		MinerConfigurations.getInstance().loadConfig("settings");
	}
	
}
