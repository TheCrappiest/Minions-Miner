package com.thecrappiest.minions.miner.listeners.custom;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.thecrappiest.minions.events.CreateMinionEntityEvent;
import com.thecrappiest.minions.events.LoadMinionAttemptEvent;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.maps.miniondata.ThreadsHolder;
import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.threads.MovementThread;
import com.thecrappiest.objects.Minion;

public class LoadMinionAttempt implements Listener {

	public final MinerCore minerCore;
	public LoadMinionAttempt(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onLoadAttempt(LoadMinionAttemptEvent event) {
		// * Tests if minion is a miner
		if(!event.getMinionType().equalsIgnoreCase("Miner")) {return;}
		
		// * Sets config variable
		YamlConfiguration entitySettings = MinerConfigurations.getInstance().getYaml("settings");
		
		// * Boolean used to tell even if minion should be created
		boolean createMinion = true;
		
		// * Sets the location variable
		Location location = null;
		if(event.getPath() != null) {
			String path = event.getPath().split("\\.")[1];
			String world = path.split("_")[0];
			double x = Double.valueOf(path.split("_")[1].replace("|", "."));
			double y = Double.valueOf(path.split("_")[2].replace("|", "."));
			double z = Double.valueOf(path.split("_")[3].replace("|", "."));
			double yaw = Double.valueOf(path.split("_")[4].replace("|", "."));
			location = new Location(Bukkit.getWorld(world),x,y,z,(float) yaw, 0f);
		}
		
		if(location != null) {
			Minion minion = MinionData.getInstance().getMinionFromLocation(location);
			
			// * Checks if location contains minion
			if(minion != null) {
				// * If minion is not "stay loaded" send log
				if(minion.getType().equals("MINER") && !entitySettings.getBoolean("Stay_Loaded")) {
					ConsoleOutput.warn("LoadAttemptFailure >> Minion at W:"+location.getWorld().getName()+
							", X:"+location.getBlockX()+", Y:"+location.getBlockY()+", Z:"+location.getBlockZ()+" has failed to load.");
					ConsoleOutput.warn("Reason: ANOTHER_MINION_FOUND_ON_LOCATION");
					event.setCancelled(true);
					return;
				}
				
				// * If minion is "stay loaded" re-add to movement thread.
				if(entitySettings.getBoolean("Stay_Loaded")) {
					createMinion = false;
					
					MovementThread mt = ThreadsHolder.getInstance().getMovementThread(minion);
					if(mt != null) {
						if(!mt.getMinionDelays().containsKey(minion)) {
							mt.addMinion(minion);
						}
					}
				}
			}
		}

		if(createMinion) {
			// * Creates and runs the CreateMinionEntityEvent
			CreateMinionEntityEvent createminionentity = createEvent(event.getPlayer(), event.getUUID(), event.getMinionType(), event.getPath(), event.getData(), event.shouldMove());
			new BukkitRunnable() {
				public void run() {
					Bukkit.getPluginManager().callEvent(createminionentity);
				}
			}.runTask(minerCore);
		}
	}
	
	private CreateMinionEntityEvent createEvent(Player player, UUID uuid, String minionType, String path, String data, boolean shouldMove) {
		if(player != null) {
			return new CreateMinionEntityEvent(player, minionType, path, data, shouldMove);
		}else {
			return new CreateMinionEntityEvent(uuid, minionType, path, data, shouldMove);
		}
	}
	
}
