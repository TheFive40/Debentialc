package org.example.tools.fragments;

import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.example.tools.General;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.example.events.CustomArmor.playerArmorBonus;

/**
 * Integra las armaduras personalizadas con fragmentos al sistema de bonificaciones
 * USA EL MISMO FLUJO que CustomManager.applyBonusToPlayer()
 */
public class FragmentBonusIntegration {

    // Tracking de bonus activos por jugador
    // UUID -> hash -> {stat: value}
    private static final Map<UUID, Map<String, Map<String, Double>>> activeFragmentBonuses = new HashMap<>();

    /**
     * Aplica las bonificaciones de armaduras con fragmentos al jugador
     * Este método debe ser llamado desde CustomManager.armorTask()
     */
    public static void applyFragmentBonuses(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        UUID playerId = player.getUniqueId();
        Map<String, Map<String, Double>> currentBonuses = new HashMap<>();

        for (ItemStack armor : armorContents) {
            if (armor == null || armor.getTypeId() == 0) continue;

            if (!CustomizedArmor.isCustomized(armor)) continue;

            Map<String, Integer> attributes = CustomizedArmor.getAttributes(armor);
            String hash = CustomizedArmor.getHash(armor);

            if (hash == null || attributes.isEmpty()) continue;

            Map<String, Double> bonusStats = new HashMap<>();
            for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
                String stat = entry.getKey();
                double value = entry.getValue().doubleValue();
                bonusStats.put(stat, value);
            }

            currentBonuses.put(hash, bonusStats);
        }

        Map<String, Map<String, Double>> previousBonuses = activeFragmentBonuses.get(playerId);

        // PASO 3: Comparar - Si no hay cambios, salir inmediatamente
        if (previousBonuses != null && bonusesAreEqual(previousBonuses, currentBonuses)) {
            return; // No hay cambios, no hacer nada
        }

        if (previousBonuses != null) {
            for (String hash : previousBonuses.keySet()) {
                if (!currentBonuses.containsKey(hash)) {
                    Map<String, Double> statsToRemove = previousBonuses.get(hash);
                    removeBonusFromPlayer(player, hash, statsToRemove);
                }
            }
        }

        // PASO 5: Aplicar bonuses nuevos o actualizados
        for (Map.Entry<String, Map<String, Double>> entry : currentBonuses.entrySet()) {
            String hash = entry.getKey();
            Map<String, Double> stats = entry.getValue();

            // Solo aplicar si es nuevo o si cambió
            if (previousBonuses == null || !previousBonuses.containsKey(hash) ||
                    !statsAreEqual(previousBonuses.get(hash), stats)) {

                // Si existía antes con valores diferentes, removerlo primero
                if (previousBonuses != null && previousBonuses.containsKey(hash)) {
                    removeBonusFromPlayer(player, hash, previousBonuses.get(hash));
                }

                // Aplicar el nuevo bonus
                applyBonusToPlayer(player, hash, stats);
            }
        }

        // PASO 6: Actualizar tracking
        activeFragmentBonuses.put(playerId, currentBonuses);
    }

    /**
     * Aplica bonificaciones al jugador usando el MISMO sistema que CustomManager
     */
    private static void applyBonusToPlayer(Player player, String hash, Map<String, Double> stats) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            stats.forEach((stat, value) -> {
                // Añadir al tracking de bonus activos
                Set<String> bonuses = (!playerArmorBonus.containsKey(player.getUniqueId())) ?
                        new HashSet<>() : playerArmorBonus.get(player.getUniqueId());

                bonuses.add(hash);
                playerArmorBonus.put(player.getUniqueId(), bonuses);

                try {
                    // Aplicar bonus usando el API de DBC
                    idbcPlayer.addBonusAttribute(
                            General.BONUS_STATS.get(stat.toUpperCase()),
                            hash,
                            "+",  // Siempre suma aditiva para fragmentos
                            value
                    );
                } catch (NullPointerException ignored) {
                }
            });
        } catch (Exception e) {
            // Silenciar errores
        }
    }

    /**
     * Remueve bonificaciones del jugador usando el MISMO sistema que CustomManager
     */
    private static void removeBonusFromPlayer(Player player, String hash, Map<String, Double> stats) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            // Remover de todos los stats
            for (String stat : General.BONUS_STATS.values()) {
                try {
                    idbcPlayer.removeBonusAttribute(stat, hash);
                } catch (Exception ignored) {
                }
            }

            // Actualizar tracking de bonus
            if (playerArmorBonus.containsKey(player.getUniqueId())) {
                Set<String> bonuses = playerArmorBonus.get(player.getUniqueId());
                bonuses.remove(hash);
                playerArmorBonus.put(player.getUniqueId(), bonuses);
            }
        } catch (Exception e) {
            // Silenciar errores
        }
    }

    /**
     * Compara si dos mapas de bonus son iguales
     */
    private static boolean bonusesAreEqual(Map<String, Map<String, Double>> map1,
                                           Map<String, Map<String, Double>> map2) {
        if (map1.size() != map2.size()) return false;

        for (String key : map1.keySet()) {
            if (!map2.containsKey(key)) return false;
            if (!statsAreEqual(map1.get(key), map2.get(key))) return false;
        }

        return true;
    }

    /**
     * Compara si dos mapas de stats son iguales
     */
    private static boolean statsAreEqual(Map<String, Double> stats1, Map<String, Double> stats2) {
        if (stats1.size() != stats2.size()) return false;

        for (Map.Entry<String, Double> entry : stats1.entrySet()) {
            Double value2 = stats2.get(entry.getKey());
            if (value2 == null || !value2.equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Limpia el tracking de un jugador específico
     * Usado cuando el jugador se desconecta
     */
    public static void clearPlayerTracking(UUID playerId) {
        activeFragmentBonuses.remove(playerId);
    }
}