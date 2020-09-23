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
	
	public Map<Minion, Miner> miners = new HashMap<>();
	
	public Miner getMinerFromMinion(Minion minion) {
		return miners.entrySet().stream().filter(entry -> entry.getKey().equals(minion)).findAny().orElse(null).getValue();
	}
	
}
