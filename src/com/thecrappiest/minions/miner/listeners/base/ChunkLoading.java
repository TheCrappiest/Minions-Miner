package com.thecrappiest.minions.miner.listeners.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.objects.Minion;

public class ChunkLoading implements Listener {

	public final MinerCore pl;
	public ChunkLoading(MinerCore pl) {
		this.pl = pl;
		Bukkit.getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		
		if(chunk.getEntities().length == 0) {return;}
		
		for(Entity entity : chunk.getEntities()) {
			if(entity instanceof ArmorStand) {
				Minion minion = MinionData.getInstance().getMinionFromLocation(entity.getLocation());
				if(minion != null && minion.getType().equalsIgnoreCase("MINER")) {
					YamlConfiguration settings = MinerConfigurations.getInstance().getYaml("settings");
					if(settings.getBoolean("Keep_Chunk_Loaded") && Core.isLegacy()) {
						try {
							Method cancel = event.getClass().getMethod("setCancelled", boolean.class);
							cancel.invoke(event, true);
						} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

}
