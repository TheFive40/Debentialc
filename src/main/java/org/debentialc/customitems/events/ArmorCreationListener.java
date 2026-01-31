package org.debentialc.customitems.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.debentialc.customitems.tools.inventory.ArmorCreationManager;

public class ArmorCreationListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!ArmorCreationManager.isCreatingArmor(player)) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancelar")) {
            ArmorCreationManager.cancelArmorCreation(player);
            return;
        }

        ArmorCreationManager.processArmorCreation(player, message);
    }
}