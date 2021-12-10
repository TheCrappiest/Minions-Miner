package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.json.simple.JSONObject;
import com.thecrappiest.minions.configuration.MinionTypeConfigurations;
import com.thecrappiest.minions.events.CreateMinionEntityEvent;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.methods.ConversionMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.minions.threads.RunTask;
import com.thecrappiest.objects.Minion;
import com.thecrappiest.versionclasses.VersionMaterial;

public class CreateMinionEntity implements Listener {

	public final MinerCore minerCore;
	public CreateMinionEntity(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
	public void onCreateMinionEntity(CreateMinionEntityEvent event) {
        String minionType = event.getMinionType();
		Location location = event.getPlaceLocation();
		
        if(!minionType.equalsIgnoreCase("MINER")) {return;}
		
		if(event.getPath() != null) {
			String path = event.getPath().split("\\.")[1];
			location = ConversionMethods.getLocationFromString(path);
		}
		
		Minion minion = MinionData.getInstance().getMinionFromLocation(location);
		if(minion == null) return;
		
        minion.setLoaded(false);
		
		YamlConfiguration entitySettings = MinionTypeConfigurations.getInstance().getYaml(minionType, "settings");
		
		// * Creates a the minion object and adds it to the data map
		Miner minerOBJ = new Miner(minion.getEntity(), minion.getEntityID(), minion.getOwner(), minion.getType());
		MinerData.getInstance().miners.put(minion, minerOBJ);
		
		// * Setting default placeholders for miner
		minion.setPlaceHolder("%minion_blocksmined%", "0");
		minion.setPlaceHolder("%minion_collectedexp%", "0");
		
		// * Adds blocks that give exp to map
	    if(entitySettings.isSet("EXP_Blocks") && !entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false).isEmpty()) {
	    	for(String blockType : entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false)) {
	    		Material material = VersionMaterial.valueOf(blockType).getMaterial();
	    		if(material != null && material != VersionMaterial.AIR.getMaterial()) {
	    			minerOBJ.addEXPForBlock(material, entitySettings.getInt("EXP_Blocks."+blockType));
	    		}
	    	}
	    }
	    
	    // * Adds list of mineable block types
	    if(entitySettings.isSet("Mineable")) {
	    	for(String blockType : entitySettings.getStringList("Mineable")) {
	    		Material material = VersionMaterial.valueOf(blockType).getMaterial();
	    		if(material != null && material != VersionMaterial.AIR.getMaterial()) {
	    			minerOBJ.addMinableBlock(material);
	    		}
	    	}
	    }

	    // * Sets whether or not exp should be bottled
	    minerOBJ.bottleEXP(entitySettings.getBoolean("Bottle_EXP"));
	   
        RunTask.delayedAsync(() -> {
            // * Checking if the minion is being loaded with data
            if(event.getData() != null) {
                JSONObject jsonData = ConversionMethods.parseString(event.getData());
				
				if(jsonData != null) {
					// * Checks for saved collectedexp
					if(jsonData.containsKey("CollectedEXP")) {
						minerOBJ.setCollectedEXP(Integer.valueOf(jsonData.get("CollectedEXP").toString()));
						minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(minerOBJ.getCollectedEXP()));
					}
					
					// * Checks for saved blocks mined
					if(jsonData.containsKey("BlocksMined")) {
						minerOBJ.setBlocksMined(Integer.valueOf(jsonData.get("BlocksMined").toString()));
						minion.setPlaceHolder("%minion_blocksmined%", String.valueOf(minerOBJ.getBlocksMined()));
					}
				}
            }
			
			// * Setting the minion as loaded (Will allow the minion to be interacted with)
			minion.setLoaded(true);
	   }, 5);
	}
	
}
