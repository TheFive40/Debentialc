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
 * Sistema de aplicación de bonus para fragmentos
 * Usa addBonusAttribute exactamente como CustomManager
 */
public class FragmentBonusIntegration {

    // Tracking de bonus activos por jugador
    // UUID -> hash -> {stat: value}
    private static final Map<UUID, Map<String, Map<String, Integer>>> activeFragmentBonuses = new HashMap<>();

    /**
     * Aplica las bonificaciones de armaduras con fragmentos al jugador
     * Se llama cada tick desde Main.armorTask()
     */
    public static void applyFragmentBonuses(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        UUID playerId = player.getUniqueId();
        Map<String, Map<String, Integer>> currentBonuses = new HashMap<>();

        for (ItemStack armor : armorContents) {
            if (armor == null || armor.getTypeId() == 0) continue;

            if (!CustomizedArmor.isCustomized(armor)) continue;

            Map<String, Integer> attributes = CustomizedArmor.getAttributes(armor);
            String hash = CustomizedArmor.getHash(armor);

            if (hash == null || attributes.isEmpty()) continue;

            currentBonuses.put(hash, new HashMap<>(attributes));
        }

        Map<String, Map<String, Integer>> previousBonuses = activeFragmentBonuses.get(playerId);

        if (previousBonuses != null && bonusesAreEqual(previousBonuses, currentBonuses)) {
            return;
        }

        if (previousBonuses != null) {
            for (String hash : previousBonuses.keySet()) {
                if (!currentBonuses.containsKey(hash)) {
                    removeBonusFromPlayer(player, hash);
                }
            }
        }

        for (Map.Entry<String, Map<String, Integer>> entry : currentBonuses.entrySet()) {
            String hash = entry.getKey();
            Map<String, Integer> stats = entry.getValue();

            if (previousBonuses == null || !previousBonuses.containsKey(hash) ||
                    !statsAreEqual(previousBonuses.get(hash), stats)) {

                if (previousBonuses != null && previousBonuses.containsKey(hash)) {
                    removeBonusFromPlayer(player, hash);
                }

                applyBonusToPlayer(player, hash, stats);
            }
        }

        activeFragmentBonuses.put(playerId, currentBonuses);
    }

    private static void applyBonusToPlayer(Player player, String itemId, Map<String, Integer> stats) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            HashMap<String, String> operations = new HashMap<>();
            for (String stat : stats.keySet()) {
                operations.put(stat, "+");
            }

            HashMap<String, Double> valueByStat = new HashMap<>();
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                valueByStat.put(entry.getKey(), entry.getValue().doubleValue());
            }

            valueByStat.forEach((k, v) -> {
                String operation = operations.get(k);
                Set<String> bonuses = (!playerArmorBonus.containsKey(player.getUniqueId())) ?
                        new HashSet<>() : playerArmorBonus.get(player.getUniqueId());

                bonuses.add(itemId);
                playerArmorBonus.put(player.getUniqueId(), bonuses);

                try {
                    idbcPlayer.addBonusAttribute(General.BONUS_STATS.get(k.toUpperCase()), itemId,
                            operation, v);
                } catch (NullPointerException ignored) {
                }
            });
        } catch (Exception e) {
        }
    }

    /**
     * Remueve bonus usando EXACTAMENTE el mismo código que CustomManager.removeBonusFromPlayer()
     */
    private static void removeBonusFromPlayer(Player player, String itemId) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            for (String stat : General.BONUS_STATS.values()) {
                try {
                    idbcPlayer.removeBonusAttribute(stat, itemId);
                } catch (Exception ignored) {
                }
            }

            if (playerArmorBonus.containsKey(player.getUniqueId())) {
                Set<String> bonuses = playerArmorBonus.get(player.getUniqueId());
                bonuses.remove(itemId);
                playerArmorBonus.put(player.getUniqueId(), bonuses);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Compara si dos mapas de bonus son iguales
     */
    private static boolean bonusesAreEqual(Map<String, Map<String, Integer>> map1,
                                           Map<String, Map<String, Integer>> map2) {
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
    private static boolean statsAreEqual(Map<String, Integer> stats1, Map<String, Integer> stats2) {
        if (stats1.size() != stats2.size()) return false;

        for (Map.Entry<String, Integer> entry : stats1.entrySet()) {
            Integer value2 = stats2.get(entry.getKey());
            if (value2 == null || !value2.equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Limpia el tracking de un jugador específico
     */
    public static void clearPlayerTracking(UUID playerId) {
        activeFragmentBonuses.remove(playerId);
    }
}