package com.thecrappiest.minions.miner.configurations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;
import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.configuration.MinionTypeConfigurations;
import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.miner.MinerCore;

public class MinerConfigurations {

	private static MinerConfigurations instance;
	public static MinerConfigurations getInstance() {
		if(instance == null) {
			instance = new MinerConfigurations();
		}
		return instance;
	}
	
	// * Gets the file separator for the system
	String sChar = File.separator;
	
	// * File for the miner minions item
	private File item_file = new File(Core.instance.getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"item.yml");
	
	// * File for the miner minions inventory
	private File inventory_file = new File(Core.instance.getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"inventory.yml");
	
	// * File for the miner minions entity
	private File entity_file = new File(Core.instance.getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"entity.yml");
	
	// * File for the miner minions entity
	private File settings_file = new File(Core.instance.getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"settings.yml");
	
	// * Load default item.yml
	// * key = the file name you want to load
	// * File names; entity, inventory, item and settings
	public void loadDefault(String key) {
		switch(key.toLowerCase()) {
		case "entity":
			MinerCore.instance.saveResource("Minion-Configurations"+sChar+"Miner"+sChar+"entity.yml", true);
			File loadedEntity = new File(MinerCore.getInstance().getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"entity.yml");
			loadAndCopyDefaults(loadedEntity, entity_file);
			break;
		case "inventory":
			MinerCore.instance.saveResource("Minion-Configurations"+sChar+"Miner"+sChar+"inventory.yml", true);
			File loadedInventory = new File(MinerCore.getInstance().getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"inventory.yml");
			loadAndCopyDefaults(loadedInventory, inventory_file);
			break;
		case "item":
			MinerCore.instance.saveResource("Minion-Configurations"+sChar+"Miner"+sChar+"item.yml", true);
			File loadedItem = new File(MinerCore.getInstance().getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"item.yml");
			loadAndCopyDefaults(loadedItem, item_file);
			break;
		case "settings":
			MinerCore.instance.saveResource("Minion-Configurations"+sChar+"Miner"+sChar+"settings.yml", true);
			File loadedSettings = new File(MinerCore.getInstance().getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner"+sChar+"settings.yml");
			loadAndCopyDefaults(loadedSettings, settings_file);
			break;
		}
		if(MinerCore.getInstance().getDataFolder().exists()) {
			new File(MinerCore.getInstance().getDataFolder()+sChar+"Minion-Configurations"+sChar+"Miner").delete();
			new File(MinerCore.getInstance().getDataFolder()+sChar+"Minion-Configurations").delete();
			MinerCore.getInstance().getDataFolder().delete();
		}
	}
	
	public void loadAndCopyDefaults(File load, File copy) {
		if(load.exists()) {
			try {
				Core.getInstance().createFile(copy);
				Files.copy(load, copy);
			} catch (IOException e) {
				ConsoleOutput.debug("Butchers "+copy.getName()+" has failed to copy to save location. Please message the author with any stack traces logged.");
				e.printStackTrace();
			}
			load.delete();
		}
	}
	
	// * Map holds yaml configurations for easy access
	// * key = file name
	// * value = yaml configuration
	Map<String, YamlConfiguration> yamlConfigs = new HashMap<>();
	
	// * Loads and/or retrieves the yaml configuration
	// * key = the file name you want to retrieve
	public YamlConfiguration getYaml(String key) {
		if(yamlConfigs.containsKey(key)) {
			return yamlConfigs.get(key);
		}
		
		YamlConfiguration yml = null;
		
		switch(key.toLowerCase()) {
		case "entity":
			if(!entity_file.exists()) {
				loadDefault(key);
			}
			yml = YamlConfiguration.loadConfiguration(entity_file);
			break;
		case "inventory":
			if(!inventory_file.exists()) {
				loadDefault(key);
			}
			yml = YamlConfiguration.loadConfiguration(inventory_file);
			break;
		case "item":
			if(!item_file.exists()) {
				loadDefault(key);
			}
			yml = YamlConfiguration.loadConfiguration(item_file);
			break;
		case "settings":
			if(!settings_file.exists()) {
				loadDefault(key);
			}
			yml = YamlConfiguration.loadConfiguration(settings_file);
			break;
		}
		
		if(yml != null) {
		    MinionTypeConfigurations.getInstance().updateConfig("MINER", key.toLowerCase(), yml);
		    yamlConfigs.put(key, yml);
		}
		
		return yamlConfigs.get(key);
	}
	
	// * Saves or reloads the yaml configuration
	// * key = file name
	// * reload = Should the yaml be saved or reloaded
	public void saveYaml(String key, boolean reload) {
		YamlConfiguration yaml = getYaml(key);
		
		if(yaml != null) {
			try{
				if(reload) {
					switch(key.toLowerCase()) {
					case "entity": yaml.load(entity_file); break;
					case "inventory": yaml.load(inventory_file); break;
					case "item": yaml.load(item_file); break;
					case "settings": yaml.load(settings_file); break;
					}
				}else {
					switch(key.toLowerCase()) {
					case "entity": yaml.save(entity_file); break;
					case "inventory": yaml.save(inventory_file); break;
					case "item": yaml.save(item_file); break;
					case "settings": yaml.save(settings_file); break;
					}
				}
			}catch (IOException | InvalidConfigurationException exc) {
				ConsoleOutput.warn("An error has occured while saving or loading the "+key+" data for miner.");
				exc.printStackTrace();
			}
			
			yamlConfigs.put(key, yaml);
			MinionTypeConfigurations.getInstance().updateConfig("MINER", key.toLowerCase(), yaml);
		}
	}
	
	// * Loads or attempts to load the given configuration
	public void loadConfig(String key) {
		YamlConfiguration yaml = getYaml(key);
		if(yaml != null) {
			if(!yaml.getKeys(false).isEmpty()) {
			    ConsoleOutput.info(key.toLowerCase()+".yml for miner loaded");
			}else {
				ConsoleOutput.info(key.toLowerCase()+".yml for miner was loaded but is empty. Attempting to load default values...");
				loadDefault(key);
				yaml = getYaml(key);
				if(yaml != null && yaml.getKeys(false).isEmpty()) {
					ConsoleOutput.warn("Attempting to load "+key.toLowerCase()+".yml defaults for miner has failed. Please message the author with any stack traces logged.");
				}else {
					ConsoleOutput.info("The attempt to load "+key.toLowerCase()+".yml defaults for miner were succesful.");
				}
			}
		}
	}
	
	// * Clears all data saved in this class
	// * Useful for when users /reload or just to clear up memory
	// * save - Should the yaml files be saved before cleared
	public static void clear(boolean save) {
		// * If true will save the configurations
		if(save) {
			getInstance().saveYaml("entity", true);
			getInstance().saveYaml("inventory", true);
			getInstance().saveYaml("item", true);
			getInstance().saveYaml("settings", true);
		}
		
		// * Clears the yaml map
		getInstance().yamlConfigs.clear();
	}
	
}
