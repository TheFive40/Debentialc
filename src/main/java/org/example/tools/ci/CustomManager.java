package org.example.tools.ci;

import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.example.Main;
import org.example.commands.items.CustomItemCommand;
import org.example.commands.items.RegisterItem;
import org.example.tools.General;

import java.util.*;
import static org.example.events.CustomArmor.playerArmorBonus;

public class CustomManager {

    /**
     * Aplica bonificaciones de armadura al jugador
     */
    public static void applyArmorBonus(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack armor : inventory.getArmorContents()) {
            RegisterItem item = new RegisterItem();
            if (armor == null) continue;
            if (armor.getTypeId() == Material.AIR.getId()) continue;
            CustomArmor ci = item.toItemCustom(armor);
            if (ci != null) {
                applyBonusToPlayer(player, ci.getId(), ci.getValueByStat(), ci.getOperation());
            }
        }
    }

    /**
     * Aplica bonificaciones de items en mano al jugador
     */
    public static void applyHandItemBonus(Player player) {
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null || itemInHand.getTypeId() == Material.AIR.getId()) {
            return;
        }

        CustomItemCommand itemCmd = new CustomItemCommand();
        CustomItem ci = itemCmd.toItemCustom(itemInHand);

        if (ci != null && ci.isActive()) {
            applyBonusToPlayer(player, ci.getId(), ci.getValueByStat(), ci.getOperation());
        }
    }

    /**
     * Método general para aplicar bonus a un jugador
     */
    private static void applyBonusToPlayer(Player player, String itemId,
                                           HashMap<String, Double> stats,
                                           HashMap<String, String> operations) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            stats.forEach((k, v) -> {
                String operation = operations.get(k);
                Set<String> bonuses = (!playerArmorBonus.containsKey(player.getUniqueId())) ?
                        new HashSet<>() : playerArmorBonus.get(player.getUniqueId());

                bonuses.add(itemId);
                playerArmorBonus.put(player.getUniqueId(), bonuses);

                try {
                    idbcPlayer.addBonusAttribute(General.STATS_MAP.get(k.toUpperCase()), itemId,
                            operation, v);
                } catch (NullPointerException ignored) {
                }
            });
        } catch (Exception e) {
            // Silent fail - CustomNPC might not be initialized
        }
    }

    /**
     * Tarea de efectos (regeneración, etc)
     */
    public static void effectsTask() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player onlinePlayer : Main.instance.getServer().getOnlinePlayers()) {
                    // Efectos de armadura
                    for (ItemStack armorContent : onlinePlayer.getInventory().getArmorContents()) {
                        if (armorContent == null || armorContent.getTypeId() == Material.AIR.getId()) continue;

                        RegisterItem registerItem = new RegisterItem();
                        CustomArmor ca = registerItem.toItemCustom(armorContent);
                        if (ca == null) continue;

                        applyEffects(onlinePlayer, ca.getEffects());
                    }

                    // Efectos de item en mano
                    ItemStack itemInHand = onlinePlayer.getItemInHand();
                    if (itemInHand != null && itemInHand.getTypeId() != Material.AIR.getId()) {
                        CustomItemCommand itemCmd = new CustomItemCommand();
                        CustomItem ci = itemCmd.toItemCustom(itemInHand);
                        if (ci != null && ci.isActive()) {
                            applyEffects(onlinePlayer, ci.getEffects());
                        }
                    }
                }
            }
        };
        runnable.runTaskTimer(Main.instance, 1, 1);
    }

    /**
     * Aplica efectos especiales al jugador
     */
    private static void applyEffects(Player player, HashMap<String, Double> effects) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            effects.forEach((k, v) -> {
                try {
                    if (k.equalsIgnoreCase("HEALTHREGEN")) {
                        int newHP = (int) (idbcPlayer.getBody() + (v * idbcPlayer.getBody()));
                        idbcPlayer.setHP(newHP);
                    } else if (k.equalsIgnoreCase("KIREGEN")) {
                        int newKi = (int) (idbcPlayer.getKi() + (v * idbcPlayer.getKi()));
                        idbcPlayer.setKi(newKi);
                    } else if (k.equalsIgnoreCase("STAMINAREGEN")) {
                        int newStamina = (int) (idbcPlayer.getStamina() + (v * idbcPlayer.getStamina()));
                        idbcPlayer.setStamina(newStamina);
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception e) {
            // Silent fail
        }
    }

    /**
     * Tarea principal que gestiona armaduras e items en mano
     */
    public static void armorTask() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Main.instance.getServer().getOnlinePlayers()) {
                    List<String> currentItemIds = new ArrayList<>();
                    RegisterItem registerItem = new RegisterItem();
                    CustomItemCommand itemCmd = new CustomItemCommand();

                    // Detectar armaduras actuales
                    for (ItemStack armorContent : player.getInventory().getArmorContents()) {
                        if (armorContent == null) continue;
                        if (armorContent.getTypeId() == Material.AIR.getId()) continue;
                        if (registerItem.isCustom(armorContent)) {
                            CustomArmor ci = registerItem.toItemCustom(armorContent);
                            if (ci != null) {
                                currentItemIds.add(ci.getId());
                            }
                        }
                    }

                    // Detectar item en mano
                    ItemStack itemInHand = player.getItemInHand();
                    if (itemInHand != null && itemInHand.getTypeId() != Material.AIR.getId()) {
                        if (itemCmd.isCustom(itemInHand)) {
                            CustomItem ci = itemCmd.toItemCustom(itemInHand);
                            if (ci != null && ci.isActive()) {
                                currentItemIds.add(ci.getId());
                            }
                        }
                    }

                    // Aplicar bonificaciones
                    applyArmorBonus(player);
                    applyHandItemBonus(player);

                    // Remover bonificaciones inactivas
                    if (playerArmorBonus.containsKey(player.getUniqueId())) {
                        Set<String> activeBonus = new HashSet<>(playerArmorBonus.get(player.getUniqueId()));
                        for (String bonus : activeBonus) {
                            if (!currentItemIds.contains(bonus)) {
                                try {
                                    IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());
                                    idbcPlayer.removeBonusAttribute("strength", bonus);
                                    idbcPlayer.removeBonusAttribute("constitution", bonus);
                                    idbcPlayer.removeBonusAttribute("dexterity", bonus);
                                    idbcPlayer.removeBonusAttribute("willpower", bonus);
                                    idbcPlayer.removeBonusAttribute("mind", bonus);

                                    Set<String> remaining = playerArmorBonus.get(player.getUniqueId());
                                    remaining.remove(bonus);
                                    playerArmorBonus.put(player.getUniqueId(), remaining);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            }
        };
        runnable.runTaskTimer(Main.instance, 20L, 20L);
    }
}