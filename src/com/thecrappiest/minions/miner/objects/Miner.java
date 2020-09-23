package com.thecrappiest.minions.miner.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Entity;

import com.thecrappiest.objects.Minion;

public class Miner extends Minion {
	
	public Miner(Entity entity, UUID entityUUID, UUID owner, String type) {
		super(entity, entityUUID, owner, type);
	}

	private boolean bottleEXP = false;
	private int collectedEXP = 0;
	private int blocksMined = 0;
	
	public boolean shouldBottleEXP() {
		return bottleEXP;
	}
	
	public void bottleEXP(boolean shouldBottle) {
		bottleEXP = shouldBottle;
	}
	
	public Integer getCollectedEXP() {
		return collectedEXP;
	}
	
	public void setCollectedEXP(int exp) {
		collectedEXP = exp;
	}
	
	public Integer getBlocksMined() {
		return blocksMined;
	}
	
	public void setBlocksMined(int blocksMined) {
		this.blocksMined = blocksMined;
	}
	
	List<Material> mineableBlocks = new ArrayList<>();
	
	public List<Material> getMineableBlocks() {
		return mineableBlocks;
	}
	
	public void addMinableBlock(Material blockType) {
		if(!mineableBlocks.contains(blockType)) {
			mineableBlocks.add(blockType);
		}
	}
	
	public void removeMineableBlock(Material blockType) {
		mineableBlocks.remove(blockType);
	}
	
	private Map<Material, Integer> expForBlocks = new HashMap<>();
	
	public Integer getEXPForBlock(Material blockType) {
		if(expForBlocks.containsKey(blockType)) {
			return expForBlocks.get(blockType);
		}else {
			return 0;
		}
	}
	
	public void addEXPForBlock(Material blockType, Integer exp) {
		expForBlocks.put(blockType, exp);
	}
	
}
