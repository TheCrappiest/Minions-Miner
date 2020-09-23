package com.thecrappiest.minions.miner.listeners.custom;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.events.GiveCommandEvent;
import com.thecrappiest.minions.items.ItemCreation;
import com.thecrappiest.minions.items.ItemNBT;
import com.thecrappiest.minions.messages.MessageSender;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;

public class GiveCommand implements Listener {

	public final MinerCore minerCore;
	public GiveCommand(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onGiveMinionCommand(GiveCommandEvent event) {
		// Doesn't continue if minion type is not "MINER"
		if(!event.getMinionType().equals("MINER")) {return;}
		
		// Variables
		int amount = event.getAmount();
		CommandSender sender = event.getSender();
		Player player = event.getPlayer();
		
		// Minion item from given minion type
		ItemStack minion_item = ItemNBT.getNBTUtils().addNBTTagString(
				ItemCreation.createItem(MinerConfigurations.getInstance().getYaml("item").getConfigurationSection(""), null)
				, "MinionType", "Miner", null);
		
		// Adds the amount of minion items to the recieving player
		for(int i = 0; i < amount; i++) {
			player.getInventory().addItem(minion_item);
		}
		
		// Creates placeholders
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("%minion_type%", WordUtils.capitalizeFully(event.getMinionType()));
		placeholders.put("%player%", event.getPlayer().getName());
		placeholders.put("%sender%", event.getSender().getName());
		placeholders.put("%amount_given%", String.valueOf(amount));
		placeholders.put("%blocks_mined%", String.valueOf(0));
		
		// Sends messages to the sender and reciever
		MessageSender.sendMessageFromData(null, sender, "Administrative.Give.Sender", placeholders);
		MessageSender.sendMessageFromData(player, null, "Administrative.Give.Reciever", placeholders);
	}
	
}
