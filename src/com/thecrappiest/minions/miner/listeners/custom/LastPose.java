package com.thecrappiest.minions.miner.listeners.custom;

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

import com.thecrappiest.minions.events.MinionLastPoseEvent;
import com.thecrappiest.minions.maps.miniondata.MinionData;
import com.thecrappiest.minions.methods.MinionEntityMethods;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.events.MinerBreakBlockAttemptEvent;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;
import com.thecrappiest.objects.MinionInventory;

public class LastPose implements Listener {

	public final MinerCore minerCore;
	public LastPose(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onLastPose(MinionLastPoseEvent event) {
		if(!event.getMinion().getType().equals("MINER")) {return;}
		
		Minion minion = event.getMinion();
		MinionInventory minionInv = MinionData.getInstance().getInventoryForMinion(minion);
		ArmorStand as = (ArmorStand) minion.getEntity();
		
		Miner miner = MinerData.getInstance().getMinerFromMinion(minion);
		if(miner == null) {return;}
		
		Block block = as.getLocation().getBlock().getRelative(BlockFace.valueOf(MinionEntityMethods.getDirection(minion)));
		if(block == null) {return;}
		Material blockType = block.getType();
		if(blockType == Material.AIR || block.getDrops(as.getEquipment().getItemInMainHand()).isEmpty()) {return;}
		if(!miner.getMineableBlocks().contains(blockType)) {return;}
		
		MinerBreakBlockAttemptEvent minerbba = new MinerBreakBlockAttemptEvent(block, minion);
		Bukkit.getPluginManager().callEvent(minerbba);
		if(minerbba.isCancelled()) {return;}
		
		int expFromBlock = miner.getEXPForBlock(blockType);
		
		boolean shouldBottle = miner.shouldBottleEXP();
		
		int amountOfBottles = 0;
		if(shouldBottle) {
			int currentEXP = expFromBlock+miner.getCollectedEXP();
			if(currentEXP >= 5) {
				amountOfBottles = currentEXP/5;
			}
		}
		
		if(minion.getLinkedChest("Link_Chest") != null) {
			Location loc = minion.getLinkedChest("Link_Chest");
			Block chestBlock = loc.getBlock();
			if(chestBlock.getType() == Material.CHEST || chestBlock.getType() == Material.TRAPPED_CHEST) {
				
				BlockState state = chestBlock.getState();
				Inventory inv = ((Container)state).getInventory();
				if(inv.firstEmpty() != -1) {
					block.getDrops(as.getEquipment().getItemInMainHand()).forEach(item -> {
						if(inv.firstEmpty() != -1) {
							inv.addItem(item);
						}
					});
					block.setType(Material.AIR);
					minion.takeHunger();
					miner.setBlocksMined(miner.getBlocksMined()+1);
					minion.setPlaceHolder("%minion_blocksmined%", String.valueOf(miner.getBlocksMined()));
					
					if(expFromBlock > 0) {
						miner.setCollectedEXP(miner.getCollectedEXP()+expFromBlock);
						minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
						if(amountOfBottles > 0) {
							for(int i = 1; i <= amountOfBottles; i++) {
								if(inv.firstEmpty() != -1) {
									miner.setCollectedEXP(miner.getCollectedEXP()-5);
									minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
									inv.addItem(new ItemStack(Material.EXPERIENCE_BOTTLE));
								}
							}
						}
					}
					
					if(minionInv != null && !minionInv.getInventory().getViewers().isEmpty()) {
						minionInv.updateInventoryItems();
					}
				}
				
			}
		}else {
			block.breakNaturally(as.getEquipment().getItemInMainHand());
			miner.setBlocksMined(miner.getBlocksMined()+1);
			minion.setPlaceHolder("%minion_blocksmined%", String.valueOf(miner.getBlocksMined()));
			minion.takeHunger();
			
			if(expFromBlock > 0) {
				miner.setCollectedEXP(miner.getCollectedEXP()+expFromBlock);
				minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
				if(amountOfBottles > 0) {
					for(int i = 1; i <= amountOfBottles; i++) {
						miner.setCollectedEXP(miner.getCollectedEXP()-5);
						minion.setPlaceHolder("%minion_collectedexp%", String.valueOf(miner.getCollectedEXP()));
						block.getWorld().dropItemNaturally(minion.getEntity().getLocation(), new ItemStack(Material.EXPERIENCE_BOTTLE));
					}
				}
			}
			
			if(minionInv != null && !minionInv.getInventory().getViewers().isEmpty()) {
				minionInv.updateInventoryItems();
			}
		}
	}
	
}
