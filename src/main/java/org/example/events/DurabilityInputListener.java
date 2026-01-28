package org.example.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.example.tools.inventory.DurabilityInputManager;

public class DurabilityInputListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!DurabilityInputManager.isInputtingDurability(player)) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancelar")) {
            DurabilityInputManager.cancelDurabilityInput(player);
            return;
        }

        DurabilityInputManager.processDurabilityInput(player, message);
    }
}