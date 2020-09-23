package com.thecrappiest.minions.miner;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.miner.listeners.base.PlayerLeave;
import com.thecrappiest.minions.miner.listeners.custom.AutoLoadMiners;
import com.thecrappiest.minions.miner.listeners.custom.CreateMinionEntity;
import com.thecrappiest.minions.miner.listeners.custom.GiveCommand;
import com.thecrappiest.minions.miner.listeners.custom.InteractWithMinion;
import com.thecrappiest.minions.miner.listeners.custom.LastPose;
import com.thecrappiest.minions.miner.listeners.custom.LoadMinionAttempt;
import com.thecrappiest.minions.miner.listeners.custom.PerformItemActions;
import com.thecrappiest.minions.miner.listeners.custom.PickupMinion;
import com.thecrappiest.minions.miner.listeners.custom.ReloadPlugin;
import com.thecrappiest.minions.miner.listeners.custom.SaveMinion;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.methods.SaveMiner;
import com.thecrappiest.objects.Minion;

public class MinerCore extends JavaPlugin {

	// * Instance of core class
	public static MinerCore instance;
	
	// * Gets the file separator for the system
	String sChar = File.separator;

	// * Method runs when plugin enables
	public void onEnable() {
		// * Sets instance
		instance = this;
		
		ConsoleOutput.info("Configuration File Loader:");
		ConsoleOutput.info(" ");
		ConsoleOutput.info("Miner Minion Configurations:");
		MinerConfigurations.getInstance().loadConfig("entity");
		MinerConfigurations.getInstance().loadConfig("inventory");
		MinerConfigurations.getInstance().loadConfig("item");
		MinerConfigurations.getInstance().loadConfig("settings");
		
		new GiveCommand(this);
		new CreateMinionEntity(this);
		new LastPose(this);
		new InteractWithMinion(this);
		new AutoLoadMiners(this);
		new PickupMinion(this);
		new LoadMinionAttempt(this);
		new ReloadPlugin(this);
		new PlayerLeave(this);
		new SaveMinion(this);
		new PerformItemActions(this);
	}
	
	// * Method runs when plugin disables
	public void onDisable() {
		
		for(Minion minion : MinerData.getInstance().miners.keySet()) {
			SaveMiner.saveMinion(minion, com.thecrappiest.minions.methods.SaveMinion.save(minion));
		}
		
		// * Just for the sake of it
		instance = null;
	}
	
	// * Returns an instance of the core
	public static MinerCore getInstance() {
		return instance;
	}
	
}