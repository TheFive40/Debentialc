package org.debentialc.raids.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.debentialc.raids.menus.RaidChatInputManager;

/**
 * RaidMenuChatListener - Escucha inputs de chat para los menús de raids
 *
 * IMPORTANTE: Usa prioridad HIGHEST para cancelar el evento ANTES de que
 * otros plugins lo procesen. Si usamos LOWEST, otros plugins con prioridad
 * NORMAL o HIGH ya habrán procesado el mensaje antes de que lo cancelemos.
 */
public class RaidMenuChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!RaidChatInputManager.isInputting(player)) {
            return;
        }

        String message = event.getMessage().trim();

        event.setCancelled(true);
        event.getRecipients().clear();

        if (message.equalsIgnoreCase("cancelar") || message.equalsIgnoreCase("cancel")) {
            RaidChatInputManager.cancelInput(player);
            return;
        }

        RaidChatInputManager.processInput(player, message);
    }
}