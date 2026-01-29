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

import static org.example.events.CustomArmor.playerArmorBonus;

/**
 * Integra las armaduras personalizadas con fragmentos al sistema de bonificaciones
 * USA EL MISMO FLUJO que CustomManager.applyBonusToPlayer()
 */
public class FragmentBonusIntegration {

    /**
     * Aplica las bonificaciones de armaduras con fragmentos al jugador
     * Este método debe ser llamado desde CustomManager.applyArmorBonus()
     * DESPUÉS de procesar las armaduras normales
     */
    public static void applyFragmentBonuses(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        // Procesar cada pieza de armadura
        for (ItemStack armor : armorContents) {
            if (armor == null || armor.getTypeId() == 0) continue;

            // Solo procesar armaduras con fragmentos (custom)
            if (!CustomizedArmor.isCustomized(armor)) continue;

            // Obtener atributos de la armadura
            Map<String, Integer> attributes = CustomizedArmor.getAttributes(armor);
            String hash = CustomizedArmor.getHash(armor);

            if (hash == null || attributes.isEmpty()) continue;

            // Convertir a formato HashMap<String, Double> para compatibilidad
            HashMap<String, Double> bonusStats = new HashMap<>();
            HashMap<String, String> operations = new HashMap<>();

            for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
                String stat = entry.getKey(); // Ya viene en mayúsculas (STR, CON, DEX, etc)
                double value = entry.getValue().doubleValue();

                bonusStats.put(stat, value);
                operations.put(stat, "+"); // Siempre suma aditiva
            }

            // Aplicar bonus usando el mismo método que CustomManager
            applyBonusToPlayer(player, hash, bonusStats, operations);
        }
    }

    /**
     * Remueve las bonificaciones de una armadura con fragmentos
     * USA EL MISMO MÉTODO que CustomManager.removeBonusFromPlayer()
     */
    public static void removeFragmentBonuses(Player player, String hash) {
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
            // Silenciosamente ignorar errores
        }
    }

    /**
     * Aplica bonificaciones al jugador
     * COPIA EXACTA del método privado CustomManager.applyBonusToPlayer()
     *
     * @param player Jugador
     * @param itemId Identificador único (hash de la armadura)
     * @param stats Mapa de stats con valores (STR -> 500.0)
     * @param operations Mapa de operaciones (STR -> "+")
     */
    private static void applyBonusToPlayer(Player player, String itemId,
                                           HashMap<String, Double> stats,
                                           HashMap<String, String> operations) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            stats.forEach((k, v) -> {
                String operation = operations.get(k);

                // Obtener o crear el Set de bonus activos para este jugador
                Set<String> bonuses = (!playerArmorBonus.containsKey(player.getUniqueId())) ?
                        new HashSet<>() : playerArmorBonus.get(player.getUniqueId());

                // Agregar este itemId a los bonus activos
                bonuses.add(itemId);
                playerArmorBonus.put(player.getUniqueId(), bonuses);

                try {
                    // Aplicar el bonus al atributo
                    // General.BONUS_STATS mapea: STR -> "strength", CON -> "constitution", etc
                    idbcPlayer.addBonusAttribute(
                            General.BONUS_STATS.get(k.toUpperCase()),
                            itemId,
                            operation,
                            v
                    );
                } catch (NullPointerException ignored) {
                    // Stat no existe en BONUS_STATS, ignorar
                }
            });
        } catch (Exception e) {
            // Silenciosamente ignorar errores de DBC
        }
    }
}