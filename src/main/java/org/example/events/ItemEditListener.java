package org.example.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.example.tools.inventory.ItemEditManager;
import org.example.tools.inventory.ArmorEditManager;

public class ItemEditListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();

        // Verificar si está editando un item
        if (ItemEditManager.isEditingItem(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ItemEditManager.cancelItemEdit(player);
            } else {
                ItemEditManager.processItemEdit(player, message);
            }
            return;
        }

        // Verificar si está editando una armadura
        if (ArmorEditManager.isEditingArmor(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ArmorEditManager.cancelArmorEdit(player);
            } else {
                ArmorEditManager.processArmorEdit(player, message);
            }
            return;
        }
    }
}