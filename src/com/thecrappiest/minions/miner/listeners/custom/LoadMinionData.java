package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.json.simple.JSONObject;

import com.thecrappiest.minions.configuration.MinionTypeConfigurations;
import com.thecrappiest.minions.events.CreateMinionObjectEvent;
import com.thecrappiest.minions.events.LoadSavedMinionDataEvent;
import com.thecrappiest.minions.methods.ConversionMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;
import com.thecrappiest.versionclasses.VersionMaterial;

public class LoadMinionData implements Listener {

	public LoadMinionData(MinerCore minerCore) {
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	// * Creates a miner object and loads default values
	@EventHandler
	public void onCreateMinionObject(CreateMinionObjectEvent event) {
		Minion minion = event.getMinion();
		String minionType = minion.getType();
		
		if(!minion.getType().equalsIgnoreCase("MINER")) {return;}
		
		Miner miner = new Miner(minion);
		event.setMinion(miner);
		YamlConfiguration entitySettings = MinionTypeConfigurations.getInstance().getYaml(minionType, "settings");

		// * Setting default placeholders for miner
        minion.setPlaceHolder("%minion_blocksmined%", "0");
        minion.setPlaceHolder("%minion_collectedexp%", "0");

        // * Adds blocks that give exp to map
        if (entitySettings.isSet("EXP_Blocks")
                    && !entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false).isEmpty()) {
            for (String blockType : entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false)) {
                Material material = VersionMaterial.valueOf(blockType).getMaterial();
                if (material != null && material != VersionMaterial.AIR.getMaterial()) {
                    miner.addEXPForBlock(material, entitySettings.getInt("EXP_Blocks." + blockType));
                }
            }
        }

        // * Adds list of mineable block types
        if (entitySettings.isSet("Mineable")) {
            for (String blockType : entitySettings.getStringList("Mineable")) {
                Material material = VersionMaterial.valueOf(blockType).getMaterial();
                if (material != null && material != VersionMaterial.AIR.getMaterial()) {
                    miner.addMinableBlock(material);
                }
            }
         }

        // * Sets whether or not exp should be bottled
        miner.bottleEXP(entitySettings.getBoolean("Bottle_EXP"));
	}
	
	// * Loads the saved data of the minion into the miner
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	public void onLoadSavedCollectorData(LoadSavedMinionDataEvent event) {
		Minion minion = event.getMinion();
		if(!(minion instanceof Miner)) return;
		
		Miner miner = (Miner) minion;
		
		if(event.getData() == null) return;
		JSONObject jsonData = ConversionMethods.parseString(event.getData());
		if(jsonData == null) return;
		
		// * Checks for saved collectedexp
		if(jsonData.containsKey("CollectedEXP")) {
			miner.setCollectedEXP(Integer.valueOf(jsonData.get("CollectedEXP").toString()));
			miner.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
		}
		
		// * Checks for saved blocks mined
		if(jsonData.containsKey("BlocksMined")) {
			miner.setBlocksMined(Integer.valueOf(jsonData.get("BlocksMined").toString()));
			miner.setPlaceHolder("%minion_blocksmined%", String.valueOf(miner.getBlocksMined()));
		}
	}
}
