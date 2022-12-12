package com.thecrappiest.minions.miner.listeners.miniontask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.Core;
import com.thecrappiest.minions.events.MinionPerformTaskEvent;
import com.thecrappiest.minions.maps.miniondata.MinionData;
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

	public MinerCore minerCore;
	public PerformMinerTask(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onPerformTask(MinionPerformTaskEvent event) {
		
		// * Sets variables used in the event
		Minion minion = event.getMinion();
		MinionInventory minionInv = MinionData.getInstance().getInventoryForMinion(minion);
		ArmorStand as = (ArmorStand) minion.getEntity();
		Location minionLocation = as.getLocation();
		BlockFace facing = BlockFace.valueOf(MinionEntityMethods.getDirection(minion));
		
		int brokenBlocks = 0;
		
		if(!(minion instanceof Miner)) 
			return;
		
		// * Sets variable for miner object
		Miner miner = (Miner) minion;
		
		// * Sets block variables
		List<Block> mineable = new ArrayList<>();
		for(int i = 0; i < miner.getRadius(); i++)
			mineable.add(mineable.isEmpty() ? minionLocation.getBlock().getRelative(facing) : mineable.get(i-1).getRelative(facing));
		
		if(mineable.isEmpty())
			return;
		
		// * Checking if linked chest is available and apply variables
		Location linkedChest = minion.getLinkedChest("Link_Chest");
		Inventory chestInventory = null;
		
		if(linkedChest != null) {
			Block chestBlock = linkedChest.getBlock();
			if(chestBlock.getType() == Material.CHEST || chestBlock.getType() == Material.TRAPPED_CHEST) {
				BlockState state = chestBlock.getState();
				if(Core.isLegacy()) {
					Chest chest = (Chest) state;
					chestInventory = chest.getInventory();
				}else {
					chestInventory = ((Container) state).getInventory();
				}
			}
		}
		
		// * List of collected items from blocks
		List<ItemStack> blockDrops = new ArrayList<>();
		
		// * Creating variable for minions held item
		ItemStack heldItem = Core.isLegacy() ? as.getEquipment().getItemInHand() : as.getEquipment().getItemInMainHand();
		
		// * Loops through retrieved blocks
		for(Block block : mineable) {
			// * Continues if block type is not whitelisted
			Material blockType = block.getType();
			if(!miner.getMineableBlocks().contains(blockType))
				continue;
			
			// * Continues if block doesn't drop items
			Collection<ItemStack> drops = block.getDrops(heldItem);
			if(drops == null || (drops != null && drops.isEmpty()))
				continue;
			
			// * Creates and calls the MinerBreakBlockAttemptEvent (Allows other plugins to know a miner is attempting to break the block)
			MinerBreakBlockAttemptEvent minerbba = new MinerBreakBlockAttemptEvent(block, minion);
			Bukkit.getPluginManager().callEvent(minerbba);

			// * Continues if the break block attempt was cancelled
			if(minerbba.isCancelled()) 
				continue;
			
			// * Tests if block is stacked by supported hooks
			boolean removeStacked = SuperiorSBHook.instance().removeStackedBlock(block);
			
			// * Gets exp amount from the block type
			int expFromBlock = miner.getEXPForBlock(blockType);
			miner.setEXP(miner.getCollectedEXP() + expFromBlock);
			
			// * Breaks block or sets to air while also collecting drops
			if(removeStacked) {
				if(chestInventory != null) {
					blockDrops.addAll(drops);
					block.setType(Material.AIR);
				}else {
					block.breakNaturally(heldItem);
				}
			}else {
				blockDrops.addAll(drops);
			}
			
			// * Sends block updates to supported hooks
			HookManager.getInstance().sendBlockUpdateToSupports(block, true, minion);
			
			brokenBlocks++;
		}
		
		// * Returns if no blocks were mined
		if(brokenBlocks == 0)
			return;
		
		// * Bottles of exp the miner can provide
		int bottleCount = 0;
		
		// * Sets whether the miner should bottle exp or just collect it
		boolean shouldBottle = miner.shouldBottleEXP();
		if(shouldBottle) {
			int currentEXP = miner.getCollectedEXP();
			if(currentEXP >= 5) {
				bottleCount = currentEXP/5;
				miner.setEXP(currentEXP-bottleCount);
			}
			
			ItemStack expBottle = VersionMaterial.EXPERIENCE_BOTTLE.getItemStack();
			int max = expBottle.getMaxStackSize();
			
			if(bottleCount > max) {
				int stackAmount = bottleCount/max;
				int stacksUsed = 0;
				for(int sa = 0; sa < stackAmount; sa++) {
					expBottle.setAmount(max);
					blockDrops.add(expBottle);
					stacksUsed++;
				}
				expBottle.setAmount(bottleCount-((stacksUsed)*max));
				if(expBottle.getAmount() > 0)
					blockDrops.add(expBottle);
			}
		}
		
		if(!blockDrops.isEmpty()) {
			for(ItemStack item : blockDrops) {
				if(chestInventory != null && chestInventory.firstEmpty() != -1)
					chestInventory.addItem(item);
				else
					minionLocation.getWorld().dropItem(minionLocation, item);
			}
		}
		
		// * Takes hunger from minion (Nothing affected if hunger is disabled)
		minion.takeHunger();
		
		// * Adds 1 to blocks mined
		miner.setBlocksMined(miner.getBlocksMined()+brokenBlocks);
		minion.setPlaceHolder("%minion_blocksmined%", String.valueOf(miner.getBlocksMined()));
		
		// * If an inventory exists and a player is viewing it the items will be updated
		if(minionInv != null && !minionInv.getInventory().getViewers().isEmpty()) {
			minionInv.updateInventoryItems();
		}
	}
	
}
