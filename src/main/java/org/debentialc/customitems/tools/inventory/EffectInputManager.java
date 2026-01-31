package org.debentialc.customitems.tools.inventory;

import org.bukkit.entity.Player;
import org.debentialc.service.CC;

import java.util.HashMap;
import java.util.UUID;

public class EffectInputManager {

    public static class EffectInputState {
        public String itemId;
        public String effectType;
        public String type;

        public EffectInputState(String itemId, String effectType, String type) {
            this.itemId = itemId;
            this.effectType = effectType;
            this.type = type;
        }
    }

    private static final HashMap<UUID, EffectInputState> playersInputting = new HashMap<>();

    public static void startEffectInput(Player player, String itemId, String effectType, String type) {
        playersInputting.put(player.getUniqueId(), new EffectInputState(itemId, effectType, type));

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&3&l  Ingresa Valor del Efecto"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Efecto: &b" + effectType));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Rango: &f0.0 - 1.0"));
        player.sendMessage(CC.translate("&7  Ejemplo: &f0.5 &7= 50%"));
        player.sendMessage(CC.translate("&7  Escribe &c'cancelar' &7para abortar"));
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    public static boolean isInputtingEffect(Player player) {
        return playersInputting.containsKey(player.getUniqueId());
    }

    public static void processEffectInput(Player player, String input) {
        EffectInputState state = playersInputting.get(player.getUniqueId());
        if (state == null) return;

        double value;
        try {
            value = Double.parseDouble(input);
            if (value < 0 || value > 1) {
                player.sendMessage("");
                player.sendMessage(CC.translate("&c✗ Valor fuera de rango (0-1)"));
                player.sendMessage("");
                startEffectInput(player, state.itemId, state.effectType, state.type);
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Número inválido"));
            player.sendMessage("");
            startEffectInput(player, state.itemId, state.effectType, state.type);
            return;
        }

        if ("item".equals(state.type)) {
            org.debentialc.customitems.tools.ci.CustomManager.applyEffectToItemFromChat(player,
                    state.itemId, state.effectType, value);
        } else if ("armor".equals(state.type)) {
            org.debentialc.customitems.tools.ci.CustomManager.applyEffectToArmorFromChat(player,
                    state.itemId, state.effectType, value);
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Efecto aplicado"));
        player.sendMessage(CC.translate("&7" + state.effectType + ": " + (value * 100) + "%"));
        player.sendMessage("");

        String type = state.type;
        String itemId = state.itemId;
        finishEffectInput(player);
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.debentialc.Main.instance, () -> {
            if ("item".equals(type)) {
                org.debentialc.customitems.tools.inventory.CustomItemMenus.openEditItemMenu(itemId).open(player);
            } else {
                org.debentialc.customitems.tools.inventory.CustomArmorMenus.openEditArmorMenu(itemId).open(player);
            }
        }, 1L);
    }

    public static void cancelEffectInput(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        player.sendMessage("");
        finishEffectInput(player);
    }

    private static void finishEffectInput(Player player) {
        playersInputting.remove(player.getUniqueId());
    }
}