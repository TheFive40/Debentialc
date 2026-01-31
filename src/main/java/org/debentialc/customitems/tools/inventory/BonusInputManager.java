package org.debentialc.customitems.tools.inventory;

import org.bukkit.entity.Player;
import org.debentialc.service.CC;

import java.util.HashMap;
import java.util.UUID;

public class BonusInputManager {

    public static class BonusInputState {
        public String itemId;
        public String stat;
        public String operation;
        public String type;

        public BonusInputState(String itemId, String stat, String operation, String type) {
            this.itemId = itemId;
            this.stat = stat;
            this.operation = operation;
            this.type = type;
        }
    }

    private static final HashMap<UUID, BonusInputState> playersInputting = new HashMap<>();

    public static void startBonusInput(Player player, String itemId, String stat,
                                       String operation, String type) {
        playersInputting.put(player.getUniqueId(), new BonusInputState(itemId, stat, operation, type));

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&3&l  Ingresa Valor del Bonus"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Stat: &b" + stat.toUpperCase()));
        player.sendMessage(CC.translate("&7  Operación: &b" + operation));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Ejemplo: &f10 &7o &f15.5"));
        player.sendMessage(CC.translate("&7  Escribe &c'cancelar' &7para abortar"));
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    public static boolean isInputtingBonus(Player player) {
        return playersInputting.containsKey(player.getUniqueId());
    }

    public static void processBonusInput(Player player, String input) {
        BonusInputState state = playersInputting.get(player.getUniqueId());
        if (state == null) return;

        double value;
        try {
            value = Double.parseDouble(input);
            if (value < 0) {
                player.sendMessage("");
                player.sendMessage(CC.translate("&c✗ El valor no puede ser negativo"));
                player.sendMessage("");
                startBonusInput(player, state.itemId, state.stat, state.operation, state.type);
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Número inválido"));
            player.sendMessage("");
            startBonusInput(player, state.itemId, state.stat, state.operation, state.type);
            return;
        }

        if ("item".equals(state.type)) {
            org.debentialc.customitems.tools.ci.CustomManager.applyBonusToItemFromChat(player,
                    state.itemId, state.stat, state.operation, value);
        } else if ("armor".equals(state.type)) {
            org.debentialc.customitems.tools.ci.CustomManager.applyBonusToArmorFromChat(player,
                    state.itemId, state.stat, state.operation, value);
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Bonus aplicado"));
        player.sendMessage(CC.translate("&7" + state.stat.toUpperCase() + " " + state.operation + value));
        player.sendMessage("");

        String type = state.type;
        String itemId = state.itemId;
        finishBonusInput(player);
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.debentialc.Main.instance, () -> {
            if ("item".equals(type)) {
                org.debentialc.customitems.tools.inventory.CustomItemMenus.openEditItemMenu(itemId).open(player);
            } else {
                org.debentialc.customitems.tools.inventory.CustomArmorMenus.openEditArmorMenu(itemId).open(player);
            }
        }, 1L);
    }

    public static void cancelBonusInput(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        player.sendMessage("");
        finishBonusInput(player);
    }

    private static void finishBonusInput(Player player) {
        playersInputting.remove(player.getUniqueId());
    }
}