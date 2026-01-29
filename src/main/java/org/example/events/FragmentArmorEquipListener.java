package org.example.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.example.tools.fragments.FragmentBonusIntegration;

/**
 * Listener para manejar eventos relacionados con armaduras con fragmentos
 *
 * NOTA: La detección de armaduras equipadas/removidas se maneja completamente
 * a través de FragmentBonusIntegration.applyFragmentBonuses() que se ejecuta
 * cada segundo y compara estados automáticamente.
 *
 * Este listener solo maneja la limpieza del tracking cuando el jugador se desconecta.
 */
public class FragmentArmorEquipListener implements Listener {

    /**
     * Limpiar el tracking cuando el jugador se desconecta
     * No es necesario remover bonus manualmente ya que el jugador está desconectándose
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Limpiar solo el tracking (Map) para liberar memoria
        // Los bonus del DBC se limpiarán automáticamente al desconectarse
        FragmentBonusIntegration.clearPlayerTracking(player.getUniqueId());
    }
}