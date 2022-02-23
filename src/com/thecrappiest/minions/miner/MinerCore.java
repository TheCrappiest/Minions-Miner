package com.thecrappiest.minions.miner;

import java.io.File;
import java.util.Arrays;

import org.bukkit.plugin.java.JavaPlugin;

import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.miner.listeners.custom.LoadMinionData;
import com.thecrappiest.minions.miner.listeners.custom.PerformItemActions;
import com.thecrappiest.minions.miner.listeners.custom.PickupMinion;
import com.thecrappiest.minions.miner.listeners.custom.ConfigurationListeners;
import com.thecrappiest.minions.miner.listeners.custom.SaveMinion;
import com.thecrappiest.minions.miner.listeners.miniontask.LastPose;
import com.thecrappiest.minions.miner.listeners.miniontask.PerformMinerTask;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;

public class MinerCore extends JavaPlugin {

	// * Instance of core class
	private static MinerCore instance;
	
	// * Gets the file separator for the system
	String sChar = File.separator;

	// * Method runs when plugin enables
	public void onEnable() {
		// * Sets instance
		instance = this;
		
		// * Loads the configurations used by miner minions
		loadMinerConfigs();
		
		// * Loads all listeners used by the plugin
		new LoadMinionData(this);
		new LastPose(this);
		new PickupMinion(this);
		new ConfigurationListeners(this);
		new SaveMinion(this);
		new PerformItemActions(this);
		new PerformMinerTask(this);
	}
	
	// * Method runs when plugin disables
	public void onDisable() {
		
		// * Saves all data for loaded miners
		MinerData md = MinerData.getInstance();
		md.miners.forEach(miner -> {
			miner.addSaveData("CollectedEXP", miner.getCollectedEXP());
			miner.addSaveData("BlocksMined", miner.getBlocksMined());
		});
		
		// * Just for the sake of it
		instance = null;
	}
	
	// * Returns an instance of the core
	public static MinerCore getInstance() {
		return instance;
	}
	
	public void loadMinerConfigs() {
		MinerConfigurations minerConfigs = MinerConfigurations.getInstance();
		Arrays.asList("entity", "inventory", "item", "settings").forEach(key -> {
			minerConfigs.getYaml(key);
			ConsoleOutput.info(key.toLowerCase()+".yml for Collector loaded");
		});
	}
	
}
