package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.example.tools.CC;

import java.util.HashMap;
import java.util.UUID;

/**
 * Gestiona el input de valores de bonificación desde el chat
 */
public class BonusInputManager {

    public static class BonusInputState {
        public String itemId;
        public String stat;
        public String operation;
        public String type; // "item" o "armor"

        public BonusInputState(String itemId, String stat, String operation, String type) {
            this.itemId = itemId;
            this.stat = stat;
            this.operation = operation;
            this.type = type;
        }
    }

    private static final HashMap<UUID, BonusInputState> playersInputting = new HashMap<>();

    /**
     * Inicia el input de valor para un bonus
     */
    public static void startBonusInput(Player player, String itemId, String stat,
                                       String operation, String type) {
        playersInputting.put(player.getUniqueId(), new BonusInputState(itemId, stat, operation, type));

        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&a BONUS"));
        player.sendMessage(CC.translate("&8 ─────────────────────────"));
        player.sendMessage(CC.translate("&7 • Stat: &f" + stat.toUpperCase()));
        player.sendMessage(CC.translate("&7 • Operación: &f" + operation));
        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&7 Ingresa el valor (ej: 10, 15.5):"));
        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&7 Escribe 'cancelar' para abortar"));
        player.sendMessage(CC.translate("&8"));
    }

    /**
     * Verifica si un jugador está esperando input de bonus
     */
    public static boolean isInputtingBonus(Player player) {
        return playersInputting.containsKey(player.getUniqueId());
    }

    /**
     * Procesa el valor ingresado por el jugador
     */
    public static void processBonusInput(Player player, String input) {
        BonusInputState state = playersInputting.get(player.getUniqueId());
        if (state == null) return;

        // Validar que sea un número
        double value;
        try {
            value = Double.parseDouble(input);
            if (value < 0) {
                player.sendMessage(CC.translate("&c✗ El valor no puede ser negativo"));
                startBonusInput(player, state.itemId, state.stat, state.operation, state.type);
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&c✗ Número inválido (usa: 10 o 15.5)"));
            startBonusInput(player, state.itemId, state.stat, state.operation, state.type);
            return;
        }

        // Aplicar el bonus según el tipo
        if ("item".equals(state.type)) {
            org.example.tools.ci.CustomManager.applyBonusToItemFromChat(player,
                    state.itemId, state.stat, state.operation, value);
        } else if ("armor".equals(state.type)) {
            org.example.tools.ci.CustomManager.applyBonusToArmorFromChat(player,
                    state.itemId, state.stat, state.operation, value);
        }

        player.sendMessage(CC.translate("&a✓ Bonus aplicado"));
        player.sendMessage(CC.translate("&7 " + state.stat + " " + state.operation + value));

        String type = state.type;
        String itemId = state.itemId;
        finishBonusInput(player);
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.example.Main.instance, () -> {
            if ("item".equals(type)) {
                org.example.tools.inventory.CustomItemMenus.openEditItemMenu(itemId).open(player);
            } else {
                org.example.tools.inventory.CustomArmorMenus.openEditArmorMenu(itemId).open(player);
            }
        }, 1L);
    }

    /**
     * Cancela el input de bonus
     */
    public static void cancelBonusInput(Player player) {
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        finishBonusInput(player);
    }

    /**
     * Finaliza el input de bonus
     */
    private static void finishBonusInput(Player player) {
        playersInputting.remove(player.getUniqueId());
    }
}