package com.thecrappiest.minions.miner;

import java.util.Arrays;

import org.bukkit.plugin.java.JavaPlugin;

import com.thecrappiest.minions.messages.ConsoleOutput;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.miner.listeners.custom.LoadMinionData;
import com.thecrappiest.minions.miner.listeners.custom.PickupMinion;
import com.thecrappiest.minions.miner.listeners.custom.ConfigurationListeners;
import com.thecrappiest.minions.miner.listeners.custom.SaveMinion;
import com.thecrappiest.minions.miner.listeners.miniontask.LastPose;
import com.thecrappiest.minions.miner.listeners.miniontask.PerformMinerTask;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;

public class MinerCore extends JavaPlugin {

    private static MinerCore instance;

    public void onEnable() {
        instance = this;

        loadMinerConfigs();

        new LoadMinionData(this);
        new LastPose(this);
        new PickupMinion(this);
        new ConfigurationListeners(this);
        new SaveMinion(this);
        new PerformMinerTask(this);
    }

    public void onDisable() {

        MinerData md = MinerData.getInstance();
        md.miners.forEach(miner -> miner.addSaveData("BlocksMined", miner.getBlocksMined()));
        md.miners.clear();
        
        instance = null;
    }

    public static MinerCore getInstance() {
        return instance;
    }

    public void loadMinerConfigs() {
        MinerConfigurations minerConfigs = MinerConfigurations.getInstance();
        Arrays.asList("entity", "inventory", "item", "settings").forEach(key -> {
            minerConfigs.getYaml(key);
            ConsoleOutput.info(key.toLowerCase() + ".yml for Miner loaded");
        });
    }

}