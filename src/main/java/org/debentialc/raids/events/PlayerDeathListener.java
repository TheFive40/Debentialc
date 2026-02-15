package org.debentialc.raids.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.debentialc.raids.managers.RaidSessionManager;
import org.debentialc.raids.managers.RaidSoundManager;
import org.debentialc.raids.managers.RaidTitleManager;
import org.debentialc.raids.effects.RaidEffects;
import org.debentialc.raids.models.RaidSession;

/**
 * PlayerDeathListener - Escucha muertes de jugadores en raids
 * Maneja el evento cuando un jugador muere durante una raid
 */
public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();

        RaidSession session = RaidSessionManager.getPlayerSession(deadPlayer.getUniqueId());

        if (session == null) {
            return;
        }

        RaidEffects.playerDeathEffect(deadPlayer, deadPlayer.getLocation());
        RaidSoundManager.playPlayerDeathSound(deadPlayer);
        RaidTitleManager.showPlayerDeath(deadPlayer);

        RaidSessionManager.playerDied(deadPlayer.getUniqueId());

        deadPlayer.sendMessage("");
        deadPlayer.sendMessage("§c§lHAS MUERTO EN LA RAID");
        deadPlayer.sendMessage("§7No puedes regresar hasta que termine");
        deadPlayer.sendMessage("");

        informOtherPlayers(deadPlayer, session);
    }

    /**
     * Informa a los otros jugadores que uno de ellos murió
     */
    private void informOtherPlayers(Player deadPlayer, RaidSession session) {
        for (java.util.UUID playerId : session.getActivePlayers()) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null && !player.equals(deadPlayer)) {
                player.sendMessage("");
                player.sendMessage(String.format("§c§l%s ha sido derrotado!", deadPlayer.getName()));
                player.sendMessage(String.format("§7Jugadores activos: %d", session.getActivePlayers().size()));
                player.sendMessage("");

                RaidSoundManager.playAlertSound(player);
            }
        }
    }
}