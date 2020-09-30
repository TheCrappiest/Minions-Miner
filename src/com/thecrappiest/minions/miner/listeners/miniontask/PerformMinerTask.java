package com.thecrappiest.minions.miner.listeners.miniontask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.events.MinionPerformTaskEvent;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.methods.MinionEntityMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.events.MinerBreakBlockAttemptEvent;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;
import com.thecrappiest.objects.MinionInventory;

public class PerformMinerTask implements Listener {

	public MinerCore minerCore;
	public PerformMinerTask(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPerformTask(MinionPerformTaskEvent event) {
		
		// * Sets variables used in the event
		Minion minion = event.getMinion();
		MinionInventory minionInv = MinionData.getInstance().getInventoryForMinion(minion);
		ArmorStand as = (ArmorStand) minion.getEntity();
		
		// * Sets variable for miner object
		Miner miner = MinerData.getInstance().getMinerFromMinion(minion);
		if(miner == null) {return;}
		
		// * Sets block variables
		Block block = as.getLocation().getBlock().getRelative(BlockFace.valueOf(MinionEntityMethods.getDirection(minion)));
		if(block == null) {return;}
		Material blockType = block.getType();

		// * Returns if block doesn't drop items or is air
		if(blockType == Material.AIR || block.getDrops(as.getEquipment().getItemInMainHand()).isEmpty()) {return;}

		// * Returns if block type is not whitelisted
		if(!miner.getMineableBlocks().contains(blockType)) {return;}

		// * Creates and calls the MinerBreakBlockAttemptEvent (Allows other plugins to know a miner is attempting to break the block)
		MinerBreakBlockAttemptEvent minerbba = new MinerBreakBlockAttemptEvent(block, minion);
		Bukkit.getPluginManager().callEvent(minerbba);

		// * Returns if the break block attempt was cancelled
		if(minerbba.isCancelled()) {return;}

		// * Gets exp amount from the block type
		int expFromBlock = miner.getEXPForBlock(blockType);

		// * Sets whether the miner should bottle exp or just collect it
		boolean shouldBottle = miner.shouldBottleEXP();

		// * Counts the amount of bottls that should be created once exp has been added
		int amountOfBottles = 0;
		if(shouldBottle) {
			int currentEXP = expFromBlock+miner.getCollectedEXP();
			if(currentEXP >= 5) {
				amountOfBottles = currentEXP/5;
			}
		}

		// * Tests minion for a link chest
		if(minion.getLinkedChest("Link_Chest") != null) {
			Location loc = minion.getLinkedChest("Link_Chest");
			Block chestBlock = loc.getBlock();
			
			// * Tests if block is a chest or trapped chest
			if(chestBlock.getType() == Material.CHEST || chestBlock.getType() == Material.TRAPPED_CHEST) {
				
				BlockState state = chestBlock.getState();
				Inventory inv = ((Container)state).getInventory();
				
				// * Tests if inventory has space for items
				if(inv.firstEmpty() != -1) {
					
					// * Adds items to the chest
					block.getDrops(as.getEquipment().getItemInMainHand()).forEach(item -> {
						if(inv.firstEmpty() != -1) {
							inv.addItem(item);
						}
					});
					
					// * Sets the block type to air to simulate it breaking
					block.setType(Material.AIR);
					
					// * Takes hunger from minion (Nothing affect if hunger is disabled)
					minion.takeHunger();
					
					// * Adds 1 to blocks mined
					miner.setBlocksMined(miner.getBlocksMined()+1);
					minion.setPlaceHolder("%minion_blocksmined%", String.valueOf(miner.getBlocksMined()));
					
					if(expFromBlock > 0) {
						// * Sets the exp of the miner
						miner.setCollectedEXP(miner.getCollectedEXP()+expFromBlock);
						minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
						
						if(amountOfBottles > 0) {
							// * Adds bottles of enchanting until/unless chest is full
							for(int i = 1; i <= amountOfBottles; i++) {
								if(inv.firstEmpty() != -1) {
									// * Updates the collectedexp
									miner.setCollectedEXP(miner.getCollectedEXP()-5);
									minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
									inv.addItem(new ItemStack(Material.EXPERIENCE_BOTTLE));
								}
							}
						}
					}
					
					// * If an inventory exists and a player is viewing it the items will be updated
					if(minionInv != null && !minionInv.getInventory().getViewers().isEmpty()) {
						minionInv.updateInventoryItems();
					}
				}
				
			}
		}else {
			// * Breaks the block using the item held
			block.breakNaturally(as.getEquipment().getItemInMainHand());
			
			// * Adds 1 to blocks mined
			miner.setBlocksMined(miner.getBlocksMined()+1);
			minion.setPlaceHolder("%minion_blocksmined%", String.valueOf(miner.getBlocksMined()));
			
			// * Takes hunger from minion (Nothing affect if hunger is disabled)
			minion.takeHunger();
			
			if(expFromBlock > 0) {
				// * Sets the exp of the miner
				miner.setCollectedEXP(miner.getCollectedEXP()+expFromBlock);
				minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
				
				if(amountOfBottles > 0) {
					// * Adds bottles of enchanting until/unless chest is full
					for(int i = 1; i <= amountOfBottles; i++) {
						// * Updates the collected exp
						miner.setCollectedEXP(miner.getCollectedEXP()-5);
						minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
						block.getWorld().dropItemNaturally(minion.getEntity().getLocation(), new ItemStack(Material.EXPERIENCE_BOTTLE));
					}
				}
			}
			
			// * If an inventory exists and a player is viewing it the items will be updated
			if(minionInv != null && !minionInv.getInventory().getViewers().isEmpty()) {
				minionInv.updateInventoryItems();
			}
		}
	}
	
}
