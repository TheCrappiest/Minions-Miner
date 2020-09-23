package com.thecrappiest.minions.miner.listeners.custom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.thecrappiest.minions.events.InteractWithMinionEvent;
import com.thecrappiest.minions.items.ItemNBT;
import com.thecrappiest.minions.miner.MinerCore;
import com.thecrappiest.minions.miner.configurations.MinerConfigurations;
import com.thecrappiest.minions.threads.GenerateMinionInventory;
import com.thecrappiest.objects.Minion;

public class InteractWithMinion implements Listener {

	public final MinerCore minerCore;
	public InteractWithMinion(MinerCore minerCore) {
		this.minerCore = minerCore;
		Bukkit.getPluginManager().registerEvents(this, minerCore);
	}
	
	@EventHandler
	public void onInteractWithMinion(InteractWithMinionEvent event) {
		Player player = event.getPlayer();
		Minion minion = event.getMinion();
		ItemStack interactionItem = event.getInteractionItem();
		
		if(event.isCancelled()) {return;}
		if(!minion.getType().equalsIgnoreCase("MINER")) {return;}
		
		if(!player.isSneaking()) {
			
			if(interactionItem == null || (interactionItem != null && interactionItem.getType() == Material.AIR)) {
				if(minion.getType().equalsIgnoreCase("MINER")) {
			    	GenerateMinionInventory generate_minion_inventory_thread = new GenerateMinionInventory();
				    generate_minion_inventory_thread.setMinion(minion);
				    generate_minion_inventory_thread.setPlayer(player);
				    generate_minion_inventory_thread.setYaml(MinerConfigurations.getInstance().getYaml("inventory"));
				    Thread thread = new Thread(generate_minion_inventory_thread);
				    generate_minion_inventory_thread.startThread(thread);
			    }
			}else if(interactionItem != null && interactionItem.getType() != Material.AIR) {
				// * Tests if the interaction item is allowed to be held by the minion
				if(MinerConfigurations.getInstance().getYaml("entity").getStringList("Holdable_Materials").contains(interactionItem.getType().name())) {
					ArmorStand as = (ArmorStand) minion.getEntity();
					
					// * If minion is holding a non default item it will be given to the player
					if(!ItemNBT.getNBTUtils().itemContainsNBTTag(as.getEquipment().getItemInMainHand(), "MinionsHeldItem")) {
						if(player.getInventory().firstEmpty() == -1) {
							player.getWorld().dropItemNaturally(player.getLocation(), as.getEquipment().getItemInMainHand());
						}else {
							player.getInventory().addItem(as.getEquipment().getItemInMainHand());
						}
					}
					
					// * Sets the new item in minions hand
					as.getEquipment().setItemInMainHand(interactionItem);
					
					// * Removes item from players inventory
					if(interactionItem.getAmount() > 1) {
						ItemStack cloneItem = interactionItem.clone();
						cloneItem.setAmount(1);
						player.getInventory().removeItem(cloneItem);
					}else {
					    player.getInventory().removeItem(interactionItem);
				    } 
				}
			}
		}
	}
	
}
