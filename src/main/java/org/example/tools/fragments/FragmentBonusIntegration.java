package org.example.tools.fragments;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.example.tools.ci.CustomManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Integra las armaduras personalizadas con el sistema de bonificaciones existente
 */
public class FragmentBonusIntegration {

    /**
     * Aplica las bonificaciones de armaduras personalizadas al jugador
     * Este método debe ser llamado desde CustomManager.applyArmorBonus()
     */
    public static void applyFragmentBonuses(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        // Procesar cada pieza de armadura
        for (ItemStack armor : armorContents) {
            if (armor == null || armor.getTypeId() == 0) continue;

            // Verificar si es armadura personalizada
            if (!CustomizedArmor.isCustomized(armor)) continue;

            // Obtener atributos
            Map<String, Integer> attributes = CustomizedArmor.getAttributes(armor);
            String hash = CustomizedArmor.getHash(armor);

            if (hash == null || attributes.isEmpty()) continue;

            // Convertir a formato compatible con CustomManager
            HashMap<String, Double> bonusStats = new HashMap<>();
            HashMap<String, String> operations = new HashMap<>();

            for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
                String stat = entry.getKey().toLowerCase();
                double value = entry.getValue().doubleValue();

                bonusStats.put(stat, value);
                operations.put(stat, "+"); // Operación aditiva
            }

            // Aplicar usando el sistema existente
            applyBonusToPlayer(player, hash, bonusStats, operations);
        }
    }

    /**
     * Remueve las bonificaciones de una armadura personalizada
     */
    public static void removeFragmentBonuses(Player player, String hash) {
        CustomManager.removeBonusFromPlayer(player, hash);
    }

    /**
     * Aplica bonificaciones al jugador usando el sistema existente
     * Adaptado de CustomManager.applyBonusToPlayer()
     */
    private static void applyBonusToPlayer(Player player, String itemId,
                                           HashMap<String, Double> stats,
                                           HashMap<String, String> operations) {
        try {
            noppes.npcs.api.entity.IDBCPlayer idbcPlayer =
                    org.example.tools.General.getDBCPlayer(player.getName());

            stats.forEach((k, v) -> {
                String operation = operations.get(k);
                java.util.Set<String> bonuses =
                        (!org.example.events.CustomArmor.playerArmorBonus.containsKey(player.getUniqueId())) ?
                                new java.util.HashSet<>() :
                                org.example.events.CustomArmor.playerArmorBonus.get(player.getUniqueId());

                bonuses.add(itemId);
                org.example.events.CustomArmor.playerArmorBonus.put(player.getUniqueId(), bonuses);

                try {
                    idbcPlayer.addBonusAttribute(
                            org.example.tools.General.BONUS_STATS.get(k.toUpperCase()),
                            itemId,
                            operation,
                            v
                    );
                } catch (NullPointerException ignored) {
                }
            });
        } catch (Exception e) {
            // Silenciosamente ignorar errores de DBC
        }
    }
}