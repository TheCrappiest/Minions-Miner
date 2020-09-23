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
        YamlConfiguration entitySettings = MinerConfigurations.getInstance().getYaml("settings");
		
		if(entitySettings.getBoolean("Auto_Load")) {
			File userDataFolder = new File(Core.getInstance().getDataFolder()+sChar+"UserStorage");
			for(File userData : userDataFolder.listFiles()) {
				String uuid = userData.getName().toString().replace(".yml", "");
				YamlConfiguration yaml = UserConfigurations.getInstance().getYaml(UUID.fromString(uuid));
				if(Bukkit.getPlayer(UUID.fromString(uuid)) == null) {
					if(!yaml.getConfigurationSection("").getKeys(false).isEmpty()) {
						for(String minionData : yaml.getConfigurationSection("MINER").getKeys(false)) {
							String path = "MINER."+minionData;
							String data = yaml.getString(path);
							
							boolean shouldMove = (entitySettings.getBoolean("Work_Offline") || entitySettings.getBoolean("Offline_Movement")) ? true: false;
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
	
}
