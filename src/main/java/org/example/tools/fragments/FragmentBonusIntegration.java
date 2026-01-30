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
 * Sistema de bonus para fragmentos con soporte CORRECTO de multiplicadores
 *
 * MANEJO DE MULTIPLICADORES:
 * - En la armadura se guarda: 115 (valor escalado * 100)
 * - Al DBC se envía: 1.15 (dividiendo por 100)
 * - El usuario ve en el lore: "15%"
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
     *
     * CONVERSIÓN DE MULTIPLICADORES:
     * - Si operación es "*" y el valor es 115, se envía 1.15 al DBC
     * - Si operación es "+" y el valor es 500, se envía 500 al DBC
     */
    private static void applyBonus(Player player, String hash, Map<String, Integer> stats, Map<String, String> operations) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                String stat = entry.getKey().toUpperCase(); // STR, CON, DEX, etc.
                int storedValue = entry.getValue();

                String operation = operations.getOrDefault(stat, "+");

                String bonusStat = General.BONUS_STATS.get(stat);
                if (bonusStat != null) {
                    double valueToSend;

                    if (operation.equals("*")) {
                        // MULTIPLICADOR: Dividir por 100
                        // 115 -> 1.15
                        valueToSend = storedValue / 100.0;
                    } else {
                        // ADITIVO/SUSTRACTIVO: Usar valor directo
                        valueToSend = (double) storedValue;
                    }

                    idbcPlayer.addBonusAttribute(bonusStat, hash, operation, valueToSend);
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