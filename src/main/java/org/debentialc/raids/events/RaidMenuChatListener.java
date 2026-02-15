package org.debentialc.raids.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.debentialc.raids.menus.RaidChatInputManager;

/**
 * RaidMenuChatListener - Escucha inputs de chat para los menús de raids
 * Funciona igual que los listeners del sistema de custom items
 */
public class RaidMenuChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();

        // Verificar si el jugador está en un flujo de input de raids
        if (!RaidChatInputManager.isInputting(player)) {
            return;
        }

        // Cancelar el mensaje del chat
        event.setCancelled(true);

        // Verificar cancelación
        if (message.equalsIgnoreCase("cancelar") || message.equalsIgnoreCase("cancel")) {
            RaidChatInputManager.cancelInput(player);
            return;
        }

        // Procesar el input
        RaidChatInputManager.processInput(player, message);
    }
}