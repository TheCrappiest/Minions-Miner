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

    private static MinerConfigurations instance;
    public static MinerConfigurations getInstance() {
        return instance = instance == null ? new MinerConfigurations() : instance;
    }

    String sChar = File.separator;

    private MinerCore minerCore = MinerCore.getInstance();
    private File coreDataFolder = Core.getInstance().getDataFolder();
    private String minerConfigurations = coreDataFolder + sChar + "Minion-Configurations" + sChar + "Miner" + sChar;

    private Map<String, File> minerFiles = new HashMap<>();

    public File getFile(String key) {
        if (minerFiles.containsKey(key))
            return minerFiles.get(key);

        File f = new File(minerConfigurations + key + ".yml");
        if (!f.exists()) {
            String configPath = "Minion-Configurations/Miner/";
            try {
                YamlConfiguration
                        .loadConfiguration(new InputStreamReader(minerCore.getResource(configPath + key + ".yml")))
                        .save(f);
            } catch (IOException e) {
                ConsoleOutput.warn("Attempting to load " + key.toLowerCase()
                        + ".yml defaults for Miner has failed. Please message the author with any stack traces logged.");
                e.printStackTrace();
            }
        }

        return f;
    }
    
    Map<String, YamlConfiguration> minerYamls = new HashMap<>();

    public YamlConfiguration getYaml(String key) {
        if (minerYamls.containsKey(key))
            return minerYamls.get(key);

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getFile(key));
        MinionTypeConfigurations.getInstance().updateConfig("MINER", key.toLowerCase(), yaml);
        minerYamls.put(key, yaml);
        return yaml;
    }

    public void saveYaml(String key, boolean reload) {
        File f = getFile(key);
        YamlConfiguration yaml = getYaml(key);
        if (yaml == null)
            return;

        try {
            if (reload)
                yaml.load(f);
            else
                yaml.save(f);
        } catch (IOException | InvalidConfigurationException e) {
            ConsoleOutput.warn("An error has occured while saving or loading the " + key + " data for miner.");
            e.printStackTrace();
        }
        MinionTypeConfigurations.getInstance().updateConfig("MINER", key.toLowerCase(), yaml);
        minerYamls.put(key, yaml);
    }

    public static void clear(boolean save) {
        if (save)
            Arrays.asList("entity", "inventory", "item", "settings").forEach(key -> getInstance().saveYaml(key, true));

        getInstance().minerYamls.clear();
        getInstance().minerFiles.clear();
    }
}