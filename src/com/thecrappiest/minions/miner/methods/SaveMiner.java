package com.thecrappiest.minions.miner.methods;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thecrappiest.minions.configuration.UserConfigurations;
import com.thecrappiest.minions.miner.map.miniondata.MinerData;
import com.thecrappiest.minions.miner.objects.Miner;
import com.thecrappiest.objects.Minion;

public class SaveMiner {

	public static String saveMinion(Minion minion, String dataString) {
		Miner miner = MinerData.getInstance().getMinerFromMinion(minion);
		
		JSONObject jsonData = null;
		try {
			jsonData = (JSONObject) new JSONParser().parse(dataString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		Map<Object, Object> data = new HashMap<>();
		for(Object keyEntry : jsonData.keySet()) {
			String key = String.valueOf(keyEntry);
			String value = String.valueOf(jsonData.get(key));
			data.put(key, value);
		}
		
		data.put("CollectedEXP", miner.getCollectedEXP());
		data.put("BlocksMined", miner.getBlocksMined());
		
		JSONObject jsonOBJ = new JSONObject(data);
		
		UUID ownerID = minion.getOwner();
		YamlConfiguration yaml = UserConfigurations.getInstance().getYaml(ownerID);
		Location location = minion.getEntity().getLocation();
		String locationString = location.getWorld().getName()+"_"+location.getX()+"_"+location.getY()+"_"+location.getZ()+"_"+location.getYaw();
		String path = minion.getType().toUpperCase()+"."+locationString.replace(".", "|");
		yaml.set(path, jsonOBJ.toJSONString());
		
		return jsonOBJ.toJSONString();
	}
	
}
