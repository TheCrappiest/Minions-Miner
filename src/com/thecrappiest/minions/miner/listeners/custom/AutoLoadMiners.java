package com.thecrappiest.minions.miner.listeners.custom;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.configuration.UserConfigurations;
import com.thecrappiest.minions.events.AutoLoadMinionsEvent;
import com.thecrappiest.minions.events.LoadMinionAttemptEvent;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;

public class AutoLoadMiners implements Listener {

	public final MinerCore minerCore;
	public AutoLoadMiners(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	String sChar = File.separator;
	
	@EventHandler
	public void onAutoLoad(AutoLoadMinionsEvent event) {
		// * Settings yaml variable
        YamlConfiguration entitySettings = MinerConfigurations.getInstance().getYaml("settings");
		
        // * Tests if miners should be auto loaded
		if(!entitySettings.getBoolean("Auto_Load")) {return;}
		
		// * Sets variable for user data folder
		File userDataFolder = new File(Core.getInstance().getDataFolder()+sChar+"UserStorage");
		
		// * Loops through all user data files
		for(File userData : userDataFolder.listFiles()) {
			String uuid = userData.getName().toString().replace(".yml", "");
			YamlConfiguration yaml = UserConfigurations.getInstance().getYaml(UUID.fromString(uuid));
			
			// * Tests if player is online (Minions are usually already loaded if they are)
			if(Bukkit.getPlayer(UUID.fromString(uuid)) == null) {
				if(!yaml.getConfigurationSection("").getKeys(false).isEmpty()) {
					for(String minionData : yaml.getConfigurationSection("MINER").getKeys(false)) {
						String path = "MINER."+minionData;
						String data = yaml.getString(path);
						
						// * Since the player is offline should the minion be moving or not
						boolean shouldMove = (entitySettings.getBoolean("Work_Offline") || entitySettings.getBoolean("Offline_Movement")) ? true: false;
						
						// * Runs the load minion attempt event asynchronously
						new BukkitRunnable() {
							public void run() {
								LoadMinionAttemptEvent loadminionattempt = new LoadMinionAttemptEvent(UUID.fromString(uuid), "Miner", null, null, path, data, shouldMove, true);
						    	Bukkit.getPluginManager().callEvent(loadminionattempt);
							}
						}.runTaskAsynchronously(minerCore);
					}
				}
			}
		}
	}
	
}
