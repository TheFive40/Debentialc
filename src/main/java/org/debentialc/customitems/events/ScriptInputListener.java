package org.debentialc.customitems.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.debentialc.customitems.tools.scripts.ScriptInputManager;

/**
 * Listener para capturar el input de URLs de scripts desde el chat
 */
public class ScriptInputListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!ScriptInputManager.isInputtingScript(player)) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancelar")) {
            ScriptInputManager.cancelScriptInput(player);
            return;
        }

        ScriptInputManager.processScriptInput(player, message);
    }
}