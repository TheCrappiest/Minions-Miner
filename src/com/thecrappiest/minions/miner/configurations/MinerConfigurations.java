package com.thecrappiest.minions.miner.configurations;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.configuration.MinionTypeConfigurations;
import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.miner.MinerCore;

public class MinerConfigurations {

	// * Creates or returns an instance of this class
	private static MinerConfigurations instance;
	public static MinerConfigurations getInstance() {
		return instance = instance == null ? new MinerConfigurations() : instance;
	}

	// * Gets the file separator for the system
	String sChar = File.separator;
	
	// * Initializes some variables
	private MinerCore minerCore = MinerCore.getInstance();
	private File coreDataFolder = Core.getInstance().getDataFolder();
	private String minerConfigurations = coreDataFolder+sChar+"Minion-Configurations"+sChar+"Miner"+sChar;
	
	// * Generates new map to be used to cache miner files
	private Map<String, File> minerFiles = new HashMap<>();
	
	// * Retrieves files based on the key provided
	// * key = The file name you want to load
	// * File names; entity, inventory, item and settings
	public File getFile(String key) {
		if(minerFiles.containsKey(key))
			return minerFiles.get(key);
		
		File f = new File(minerConfigurations+key+".yml");
		if(!f.exists()) {
			String configPath = "Minion-Configurations/Miner/";
			try {
				YamlConfiguration.loadConfiguration(new InputStreamReader(minerCore.getResource(configPath+key+".yml"))).save(f);
			} catch (IOException e) {
				ConsoleOutput.warn("Attempting to load "+key.toLowerCase()+".yml defaults for Collector has failed. Please message the author with any stack traces logged.");
				e.printStackTrace();
			}
		}
		
		return f;
	}
	
	// * Generates new map to be used to cache miner yamlconfigurations
	Map<String, YamlConfiguration> minerYamls = new HashMap<>();
	
	// * Retrieves yamlconfigurations based on the key provided
	// * key = The yamlconfiguration name you want to load
	// * File names; entity, inventory, item and settings
	public YamlConfiguration getYaml(String key) {
		if(minerYamls.containsKey(key))
			return minerYamls.get(key);
		
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getFile(key));
		MinionTypeConfigurations.getInstance().updateConfig("MINER", key.toLowerCase(), yaml);
		minerYamls.put(key, yaml);
		return yaml;
	}
	
	// * Saves or reloads the yamlconfiguration
	// * key = File name
	// * reload = Should the yaml be saved or reloaded
	public void saveYaml(String key, boolean reload) {
		File f = getFile(key);
		YamlConfiguration yaml = getYaml(key);
		if(yaml == null) return;
		
		try {
			if(reload)
				yaml.load(f);
			else
				yaml.save(f);
		} catch (IOException | InvalidConfigurationException e) {
			ConsoleOutput.warn("An error has occured while saving or loading the "+key+" data for collector.");
			e.printStackTrace();
		}
		MinionTypeConfigurations.getInstance().updateConfig("MINER", key.toLowerCase(), yaml);
		minerYamls.put(key, yaml);
	}
	
	public static void clear(boolean save) {
		if(save)
			Arrays.asList("entity", "inventory", "item", "settings").forEach(key -> getInstance().saveYaml(key, true));
		
		getInstance().minerYamls.clear();
		getInstance().minerFiles.clear();
	}
}
