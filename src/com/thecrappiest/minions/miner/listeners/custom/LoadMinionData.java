package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import com.thecrappiest.minions.configuration.MinionTypeConfigurations;
import com.thecrappiest.minions.events.CreateMinionObjectEvent;
import com.thecrappiest.minions.events.LoadSavedMinionDataEvent;
import com.thecrappiest.minions.methods.ConversionMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.nbt.NBTMethods;
import com.thecrappiest.nbt.NBTUtility;
import com.thecrappiest.objects.Minion;
import com.thecrappiest.versionclasses.VersionMaterial;

public class LoadMinionData implements Listener {

    public LoadMinionData(MinerCore minerCore) {
        Bukkit.getPluginManager().registerEvents(this, minerCore);
    }

    @EventHandler
    public void onCreateMinionObject(CreateMinionObjectEvent event) {
        Minion minion = event.getMinion();
        String minionType = minion.getType();

        if (!minionType.equalsIgnoreCase("MINER"))
            return;

        Miner miner = new Miner(minion);
        event.setMinion(miner);
        YamlConfiguration entitySettings = MinionTypeConfigurations.getInstance().getYaml(minionType, "settings");

        miner.setPlaceHolder("%minion_blocksmined%", "0");

        if (entitySettings.isSet("EXP_Blocks")
                && !entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false).isEmpty()) {
            for (String blockType : entitySettings.getConfigurationSection("EXP_Blocks").getKeys(false)) {
                Material material = VersionMaterial.valueOf(blockType).getMaterial();
                if (material != null && material != VersionMaterial.AIR.getMaterial())
                    miner.addEXPForBlock(material, entitySettings.getInt("EXP_Blocks." + blockType));
            }
        }

        if (entitySettings.isSet("Mineable")) {
            for (String blockType : entitySettings.getStringList("Mineable")) {
                Material material = VersionMaterial.valueOf(blockType).getMaterial();
                if (material != null && material != VersionMaterial.AIR.getMaterial())
                    miner.addMinableBlock(material);
            }
        }

        miner.bottleEXP(entitySettings.getBoolean("Bottle_EXP"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLoadSavedMinerData(LoadSavedMinionDataEvent event) {
        Minion minion = event.getMinion();
        if (!(minion instanceof Miner))
            return;

        Miner miner = (Miner) minion;

        if (event.getMinionItem() != null) {
            ItemStack minionItem = event.getMinionItem();
            NBTUtility nbt = NBTUtility.get();
            NBTMethods nbtMethods = NBTMethods.get();

            if (nbtMethods.hasKey(minionItem, "BlocksMined"))
                miner.setBlocksMined((int) nbt.getTagObject(minionItem, "BlocksMined"));
        }

        if (event.getData() == null)
            return;
        
        JSONObject jsonData = ConversionMethods.parseString(event.getData());
        if (jsonData == null)
            return;

        if (jsonData.containsKey("BlocksMined"))
            miner.setBlocksMined(Integer.valueOf(jsonData.get("BlocksMined").toString()));
    }
}