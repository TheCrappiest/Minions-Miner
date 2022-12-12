package com.thecrappiest.minions.miner.map.miniondata;

import java.util.ArrayList;
import java.util.List;
import com.thecrappiest.minions.miner.objects.Miner;

public class MinerData {

	public static MinerData instance;

	public static MinerData getInstance() {
		return instance = instance == null ? new MinerData() : instance;
	}

	// * Stored data of miner objects
	public List<Miner> miners = new ArrayList<>();

}
