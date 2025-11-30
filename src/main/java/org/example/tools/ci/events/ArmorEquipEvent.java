package org.example.tools.ci.events;

import lombok.Getter;
import net.minecraft.server.v1_7_R4.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArmorEquipEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    @Getter
    private final Player player;
    @Getter
    private final ItemStack newItem;

    public ArmorEquipEvent(Player player, ItemStack newItem) {
        this.player = player;
        this.newItem = newItem;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
