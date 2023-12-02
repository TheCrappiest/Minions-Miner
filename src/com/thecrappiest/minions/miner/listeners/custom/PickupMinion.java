package com.thecrappiest.minions.miner.listeners.custom;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.events.PickupMinionEvent;
import com.thecrappiest.minions.items.ItemCreation;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.nbt.NBTMethods;
import com.thecrappiest.nbt.NBTUtility;
import com.thecrappiest.objects.Minion;

public class PickupMinion implements Listener {

    public PickupMinion(MinerCore minerCore) {
        Bukkit.getPluginManager().registerEvents(this, minerCore);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onPickupMinion(PickupMinionEvent event) {
        Player player = event.getPlayer();
        Minion minion = event.getMinion();

        if (!(minion instanceof Miner))
            return;

        Map<String, Object> nbtTags = new HashMap<>();

        if (minion.useHealth() && minion.getHealth() != -1) {
            nbtTags.put("Health", minion.getHealth());
            if (minion.getMaxHealth() != -1)
                nbtTags.put("MaxHealth", minion.getMaxHealth());
        }

        if (minion.useHunger() && minion.getHunger() != -1) {
            nbtTags.put("Hunger", minion.getHunger());
            if (minion.getMaxHunger() != -1)
                nbtTags.put("MaxHunger", minion.getMaxHunger());
        }

        ArmorStand as = (ArmorStand) minion.getEntity();
        ItemStack heldItem = Core.isLegacy() ? as.getEquipment().getItemInHand()
                : as.getEquipment().getItemInMainHand();

        if (!NBTMethods.get().hasKey(heldItem, "MinionsHeldItem")) {
            if (player.getInventory().firstEmpty() == -1)
                player.getWorld().dropItemNaturally(player.getLocation(), heldItem);
            else
                player.getInventory().addItem(heldItem);
        }

        Miner miner = (Miner) minion;
        nbtTags.put("BlocksMined", miner.getBlocksMined());
        nbtTags.put("CollectedEXP", miner.getCollectedEXP());
        nbtTags.put("Radius", miner.getRadius());

        YamlConfiguration itemData = MinerConfigurations.getInstance().getYaml("item");

        player.getInventory().addItem(NBTUtility.get().addNBTTags(
                ItemCreation.createItem(itemData.getConfigurationSection(""), minion.getPlaceHolders()), nbtTags));

        MinionData.getInstance().unloadMinion(minion, true);
        MinerData.getInstance().miners.remove(minion);
    }

}