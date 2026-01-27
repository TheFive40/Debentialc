package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.example.tools.CC;

import java.util.HashMap;
import java.util.UUID;

public class EffectInputManager {

    public static class EffectInputState {
        public String itemId;
        public String effectType;
        public String type; // "item" o "armor"

        public EffectInputState(String itemId, String effectType, String type) {
            this.itemId = itemId;
            this.effectType = effectType;
            this.type = type;
        }
    }

    private static final HashMap<UUID, EffectInputState> playersInputting = new HashMap<>();

    public static void startEffectInput(Player player, String itemId, String effectType, String type) {
        playersInputting.put(player.getUniqueId(), new EffectInputState(itemId, effectType, type));

        player.sendMessage(CC.translate("&6&l┌─────────────────────────────────────┐"));
        player.sendMessage(CC.translate("&6&l│  &e&lINGRESA EL VALOR DEL EFECTO      &6&l│"));
        player.sendMessage(CC.translate("&6&l├─────────────────────────────────────┤"));
        player.sendMessage(CC.translate("&6&l│ &7Efecto: &f" + effectType + "                      &6&l│"));
        player.sendMessage(CC.translate("&6&l│ &c                                     &6&l│"));
        player.sendMessage(CC.translate("&6&l│ &7Escribe el valor (0.0 a 1.0)       &6&l│"));
        player.sendMessage(CC.translate("&6&l│ &7Ejemplo: 0.5 = 50%                 &6&l│"));
        player.sendMessage(CC.translate("&6&l│ &c(Escribe 'cancelar' para abortar) &6&l│"));
        player.sendMessage(CC.translate("&6&l└─────────────────────────────────────┘"));
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
                player.sendMessage(CC.translate("&c✗ El valor debe estar entre 0 y 1"));
                startEffectInput(player, state.itemId, state.effectType, state.type);
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&c✗ Debes ingresar un número válido (ejemplo: 0.5)"));
            startEffectInput(player, state.itemId, state.effectType, state.type);
            return;
        }

        if ("item".equals(state.type)) {
            org.example.tools.ci.CustomManager.applyEffectToItemFromChat(player,
                    state.itemId, state.effectType, value);
        } else if ("armor".equals(state.type)) {
            org.example.tools.ci.CustomManager.applyEffectToArmorFromChat(player,
                    state.itemId, state.effectType, value);
        }

        player.sendMessage(CC.translate("&a✓ Efecto aplicado correctamente"));
        player.sendMessage(CC.translate("&7Efecto: &f" + state.effectType + " " + (value * 100) + "%"));

        String type = state.type;
        String itemId = state.itemId;
        finishEffectInput(player);
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.example.Main.instance, () -> {
            if ("item".equals(type)) {
                org.example.tools.inventory.CustomItemMenus.openEditItemMenu(itemId).open(player);
            } else {
                org.example.tools.inventory.CustomArmorMenus.openEditArmorMenu(itemId).open(player);
            }
        }, 1L);
    }

    public static void cancelEffectInput(Player player) {
        player.sendMessage(CC.translate("&c✗ Entrada de efecto cancelada"));
        finishEffectInput(player);
    }

    private static void finishEffectInput(Player player) {
        playersInputting.remove(player.getUniqueId());
    }
}