package org.example.tools.fragments;

import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.General;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sistema SIMPLE de bonus para fragmentos
 * - Si tiene armadura con fragmentos → APLICA bonus
 * - Si ya no la tiene → QUITA bonus
 */
public class FragmentBonusIntegration {

    // Guarda qué hashes tiene cada jugador actualmente
    private static final Map<UUID, Set<String>> playerActiveHashes = new HashMap<>();

    /**
     * Aplica bonus de fragmentos - SE LLAMA CADA TICK
     */
    public static void applyFragmentBonuses(Player player) {
        UUID playerId = player.getUniqueId();

        // Hashes actuales equipados
        Set<String> currentHashes = new HashSet<>();

        // Revisar cada pieza de armadura equipada
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece == null || piece.getTypeId() == 0) continue;

            // ¿Es una armadura custom con hash?
            if (CustomizedArmor.isCustomized(piece)) {
                String hash = CustomizedArmor.getHash(piece);
                Map<String, Integer> attributes = CustomizedArmor.getAttributes(piece);
                Map<String, String> operations = CustomizedArmor.getOperations(piece);

                if (hash != null && !attributes.isEmpty()) {
                    currentHashes.add(hash);

                    // APLICAR BONUS con las operaciones correctas
                    applyBonus(player, hash, attributes, operations);
                }
            }
        }

        // Obtener hashes previos
        Set<String> previousHashes = playerActiveHashes.getOrDefault(playerId, new HashSet<>());

        // QUITAR BONUS de armaduras que ya no están equipadas
        for (String oldHash : previousHashes) {
            if (!currentHashes.contains(oldHash)) {
                // Esta armadura ya no está equipada - QUITAR BONUS
                removeBonus(player, oldHash);
            }
        }

        // Actualizar lista de hashes activos
        playerActiveHashes.put(playerId, currentHashes);
    }

    /**
     * APLICA los bonus al jugador
     */
    private static void applyBonus(Player player, String hash, Map<String, Integer> stats, Map<String, String> operations) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            // Por cada stat, aplicar bonus CON SU OPERACIÓN
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                String stat = entry.getKey().toUpperCase(); // STR, CON, DEX, etc.
                int value = entry.getValue();

                // Obtener la operación para este stat (default "+")
                String operation = operations.getOrDefault(stat, "+");

                String bonusStat = General.BONUS_STATS.get(stat);
                if (bonusStat != null) {
                    // ¡USAR LA OPERACIÓN CORRECTA!
                    idbcPlayer.addBonusAttribute(bonusStat, hash, operation, (double) value);
                }
            }
        } catch (Exception e) {
            // Ignorar errores
        }
    }

    /**
     * QUITA los bonus del jugador
     */
    private static void removeBonus(Player player, String hash) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            // Remover de TODOS los stats posibles
            for (String bonusStat : General.BONUS_STATS.values()) {
                idbcPlayer.removeBonusAttribute(bonusStat, hash);
            }
        } catch (Exception e) {
            // Ignorar errores
        }
    }

    /**
     * Limpia el tracking cuando el jugador se desconecta
     */
    public static void clearPlayerTracking(UUID playerId) {
        playerActiveHashes.remove(playerId);
    }
}