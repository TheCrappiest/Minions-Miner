package com.thecrappiest.minions.miner.listeners.miniontask;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.events.MinionPerformTaskEvent;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.methods.DropsUsage;
import com.thecrappiest.minions.methods.MinionEntityMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.events.MinerBreakBlockAttemptEvent;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.minions.support.hooks.HookManager;
import com.thecrappiest.minions.support.hooks.skyblock.SuperiorSBHook;
import com.thecrappiest.objects.Minion;
import com.thecrappiest.objects.MinionInventory;
import com.thecrappiest.versionclasses.VersionMaterial;

public class PerformMinerTask implements Listener {

    public PerformMinerTask(MinerCore minerCore) {
        Bukkit.getPluginManager().registerEvents(this, minerCore);
    }

    private MinionData minionData = MinionData.getInstance();
    private HookManager hookManager = HookManager.getInstance();
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPerformTask(MinionPerformTaskEvent event) {
        
        Minion minion = event.getMinion();
        if(!(minion instanceof Miner))
            return;
        
        Miner miner = (Miner) minion;
        ArmorStand as = (ArmorStand) miner.getEntity();
        ItemStack heldItem = Core.isLegacy() ? as.getEquipment().getItemInHand() : as.getEquipment().getItemInMainHand();
        
        Location minionLocation = as.getLocation();
        Location chestLocation = miner.getLinkedChest("Link_Chest");
        BlockFace facing = BlockFace.valueOf(MinionEntityMethods.getDirection(miner));
        MinionInventory minionInventory = minionData.getInventoryForMinion(miner);
        List<Material> mineable = miner.getMineableBlocks();
        
        int blocksBroken = 0;
        Map<ItemStack, Integer> drops = new HashMap<>();
        
        Block lastBlock = null;
        for(int i = 0; i < miner.getRadius(); i++) {
            Block current = lastBlock == null ? minionLocation.getBlock().getRelative(facing) : lastBlock.getRelative(facing);
            Material blockType = current.getType();
            
            lastBlock = current;
            
            if(!mineable.isEmpty() && !mineable.contains(blockType))
                continue;
            
            Collection<ItemStack> blockDrops = current.getDrops(heldItem);
            if(blockDrops == null || blockDrops.isEmpty())
                continue;
            
            MinerBreakBlockAttemptEvent minerbba = new MinerBreakBlockAttemptEvent(current, minion);
            Bukkit.getPluginManager().callEvent(minerbba);
            
            if(minerbba.isCancelled())
                continue;
            
            int expFromBlock = miner.getEXPForBlock(blockType);
            miner.setEXP(miner.getCollectedEXP() + expFromBlock);
            
            boolean removeStacked = SuperiorSBHook.instance().removeStackedBlock(current);
            
            if(removeStacked) {
                if(chestLocation != null) {
                    blockDrops.forEach(item -> drops.put(item, drops.containsKey(item) ? drops.get(item) + item.getAmount() : item.getAmount()));
                    current.setType(Material.AIR);
                }else {
                    current.breakNaturally(heldItem);
                }
            }else {
                blockDrops.forEach(item -> drops.put(item, drops.containsKey(item) ? drops.get(item) + item.getAmount() : item.getAmount()));
            }
            
            hookManager.sendBlockUpdateToSupports(current, true, minion);
            blocksBroken++;
        }
        
        if(blocksBroken == 0)
            return;
        
        int bottleCount = 0;
        boolean shouldBottle = miner.shouldBottleEXP();
        
        if(shouldBottle) {
            int currentEXP = miner.getCollectedEXP();
            if(currentEXP >= 5) {
                bottleCount = currentEXP/5;
                miner.setEXP(currentEXP-(bottleCount*5));
            }
            
            ItemStack expBottle = VersionMaterial.EXPERIENCE_BOTTLE.getItemStack();
            drops.put(expBottle, bottleCount);
        }
        
        if(!drops.isEmpty())
            DropsUsage.getInstance().addItemToChest(drops, minion);

        minion.takeHunger();
        
        miner.setBlocksMined(miner.getBlocksMined()+blocksBroken);
        minion.setPlaceHolder("%minion_blocksmined%", String.valueOf(miner.getBlocksMined()));
        
        if(minionInventory != null && !minionInventory.getInventory().getViewers().isEmpty())
            minionInventory.updateInventoryItems();
    }
}