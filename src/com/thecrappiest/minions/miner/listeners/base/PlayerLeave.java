package com.thecrappiest.minions.miner.listeners.base;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.thecrappiest.minions.configuration.UserConfigurations;
import com.thecrappiest.minions.events.SaveMinionEvent;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.maps.miniondata.ThreadsHolder;
import com.thecrappiest.minions.methods.SaveMinion;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.objects.Minion;

public class PlayerLeave implements Listener {
	
	public final MinerCore minerCore;
	
	public PlayerLeave(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		// * Retrieves players minions
		List<Minion> player_minions = MinionData.getInstance().getPlayersMinions(uuid);
		
		// * Checks if the list is null or empty
		if(player_minions != null && !player_minions.isEmpty()) {
			
			// * Loops through list
			for(Minion minion : player_minions) {
				if(minion.getType().equalsIgnoreCase("MINER")) {
					YamlConfiguration entitySettings = MinerConfigurations.getInstance().getYaml("settings");
					
					boolean unload = true;
					if(entitySettings.getBoolean("Stay_Loaded")) {
						unload = false;
						
						if(!entitySettings.getBoolean("Work_Offline") && !entitySettings.getBoolean("Offline_Movement")) {
							// * Stops the minions animation
							ThreadsHolder.getInstance().removeMinionFromThreads(minion);
						}
					}
					
					// * Saves the minions data
					SaveMinionEvent saveminion = new SaveMinionEvent(minion, SaveMinion.save(minion));
					Bukkit.getPluginManager().callEvent(saveminion);
					
					if(unload) {
						MinionData.getInstance().unloadMinion(minion, false);
					}
				}
			}
		}
		
		UserConfigurations uc = UserConfigurations.getInstance();
		uc.saveYaml(uuid, false);
		
		if(uc.getYaml(uuid).getKeys(false).isEmpty()) {
			uc.deleteFile(uuid);
		}
		
		// * Clears the recently created list of minion data
		player_minions.clear();
	}
	
}
