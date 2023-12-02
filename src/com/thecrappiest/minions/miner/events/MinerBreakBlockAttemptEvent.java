package com.thecrappiest.minions.miner.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.thecrappiest.objects.Minion;

public class MinerBreakBlockAttemptEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Block block;
    private Minion minion;

    public MinerBreakBlockAttemptEvent(Block block, Minion minion) {
        this.block = block;
        this.minion = minion;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean paramC) {
        cancelled = paramC;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Block getBlock() {
        return block;
    }

    public Minion getMinion() {
        return minion;
    }

}