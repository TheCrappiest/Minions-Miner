package com.thecrappiest.minions.miner.map.miniondata;

import java.util.HashMap;
import java.util.Map;

import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class MinerData {

	public static MinerData instance;
	public static MinerData getInstance() {
		if(instance == null) {
			instance = new MinerData();
		}
		return instance;
	}
	
	// * Stored data of miner objects
	public Map<Minion, Miner> miners = new HashMap<>();
	
	// * Returns a miner object if stored
	public Miner getMinerFromMinion(Minion minion) {
		return miners.entrySet().stream().filter(entry -> entry.getKey().equals(minion)).findAny().orElse(null).getValue();
	}
	
}
