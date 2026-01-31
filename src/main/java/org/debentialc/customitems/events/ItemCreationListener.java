package org.debentialc.customitems.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.debentialc.customitems.tools.inventory.ItemCreationManager;

public class ItemCreationListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!ItemCreationManager.isCreatingItem(player)) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancelar")) {
            ItemCreationManager.cancelItemCreation(player);
            return;
        }

        ItemCreationManager.processItemCreation(player, message);
    }
}