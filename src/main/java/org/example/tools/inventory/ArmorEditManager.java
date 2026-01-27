package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.ci.CustomArmor;
import org.example.commands.items.RegisterItem;

import java.util.HashMap;
import java.util.UUID;

/**
 * Gestiona la edición de armaduras (renombre, lore, etc) por chat
 */
public class ArmorEditManager {

    public static class ArmorEditState {
        public String armorId;
        public String editType; // "rename", "addline", "setline"
        public int lineNumber; // Para setline

        public ArmorEditState(String armorId, String editType) {
            this.armorId = armorId;
            this.editType = editType;
        }

        public ArmorEditState(String armorId, String editType, int lineNumber) {
            this.armorId = armorId;
            this.editType = editType;
            this.lineNumber = lineNumber;
        }
    }

    private static final HashMap<UUID, ArmorEditState> playersEditing = new HashMap<>();

    /**
     * Inicia la edición de una armadura
     */
    public static void startArmorEdit(Player player, String armorId, String editType) {
        playersEditing.put(player.getUniqueId(), new ArmorEditState(armorId, editType));

        player.closeInventory();
        player.sendMessage(CC.translate("&b&l┌─────────────────────────────────┐"));
        player.sendMessage(CC.translate("&b&l│  &a&lEDITAR ARMADURA CUSTOM       &b&l│"));
        player.sendMessage(CC.translate("&b&l├─────────────────────────────────┤"));
        player.sendMessage(CC.translate("&b&l│ &7Armadura: &f" + armorId + "          &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &7Tipo: &f" + editType.toUpperCase() + "                    &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &c                               &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &7Escribe el valor en el chat    &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &c(Escribe 'cancelar' para abortar)&b&l│"));
        player.sendMessage(CC.translate("&b&l└─────────────────────────────────┘"));
    }

    public static void startArmorEditLine(Player player, String armorId, int lineNumber) {
        ArmorEditState state = new ArmorEditState(armorId, "setline", lineNumber);
        playersEditing.put(player.getUniqueId(), state);

        player.closeInventory();
        player.sendMessage(CC.translate("&b&l┌─────────────────────────────────┐"));
        player.sendMessage(CC.translate("&b&l│  &a&lEDITAR LORE DE ARMADURA     &b&l│"));
        player.sendMessage(CC.translate("&b&l├─────────────────────────────────┤"));
        player.sendMessage(CC.translate("&b&l│ &7Armadura: &f" + armorId + "          &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &7Línea: &f" + lineNumber + "                      &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &c                               &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &7Escribe el nuevo texto         &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &c(Escribe 'cancelar' para abortar)&b&l│"));
        player.sendMessage(CC.translate("&b&l└─────────────────────────────────┘"));
    }

    /**
     * Verifica si un jugador está editando una armadura
     */
    public static boolean isEditingArmor(Player player) {
        return playersEditing.containsKey(player.getUniqueId());
    }

    /**
     * Procesa el input de edición
     */
    public static void processArmorEdit(Player player, String input) {
        ArmorEditState state = playersEditing.get(player.getUniqueId());
        if (state == null) return;

        if (!RegisterItem.items.containsKey(state.armorId)) {
            player.sendMessage(CC.translate("&c✗ Armadura no encontrada"));
            finishArmorEdit(player);
            return;
        }

        CustomArmor armor = RegisterItem.items.get(state.armorId);

        switch (state.editType.toLowerCase()) {
            case "rename":
                armor.setDisplayName(CC.translate(input));
                RegisterItem.items.put(state.armorId, armor);
                player.sendMessage(CC.translate("&a✓ Nombre actualizado: &f" + input));
                break;

            case "addline":
                java.util.List<String> lore = armor.getLore();
                if (lore == null) {
                    lore = new java.util.ArrayList<>();
                }
                lore.add(CC.translate(input));
                armor.setLore(lore);
                RegisterItem.items.put(state.armorId, armor);
                player.sendMessage(CC.translate("&a✓ Línea de lore agregada"));
                break;

            case "setline":
                lore = armor.getLore();
                if (lore == null || state.lineNumber > lore.size() || state.lineNumber < 1) {
                    player.sendMessage(CC.translate("&c✗ Número de línea inválido"));
                    finishArmorEdit(player);
                    return;
                }
                lore.set(state.lineNumber - 1, CC.translate(input));
                armor.setLore(lore);
                RegisterItem.items.put(state.armorId, armor);
                player.sendMessage(CC.translate("&a✓ Línea " + state.lineNumber + " actualizada"));
                break;
        }

        finishArmorEdit(player);
    }

    /**
     * Cancela la edición
     */
    public static void cancelArmorEdit(Player player) {
        player.sendMessage(CC.translate("&c✗ Edición cancelada"));
        finishArmorEdit(player);
    }

    /**
     * Finaliza la edición
     */
    private static void finishArmorEdit(Player player) {
        playersEditing.remove(player.getUniqueId());
    }

    /**
     * Da una armadura custom al jugador
     */
    public static void giveCustomArmor(Player player, String armorId) {
        if (!RegisterItem.items.containsKey(armorId)) {
            player.sendMessage(CC.translate("&c✗ Armadura no encontrada"));
            return;
        }

        CustomArmor customArmor = RegisterItem.items.get(armorId);
        ItemStack itemStack = new ItemStack(customArmor.getMaterial());
        org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName(customArmor.getDisplayName());
        if (customArmor.getLore() != null) {
            meta.setLore(customArmor.getLore());
        }
        itemStack.setItemMeta(meta);

        // Dar la armadura al jugador
        if (player.getInventory().firstEmpty() == -1) {
            // Inventario lleno, dropearlo en el suelo
            player.getWorld().dropItem(player.getLocation(), itemStack);
            player.sendMessage(CC.translate("&a✓ Armadura dada (Inventario lleno, dropeada en el suelo)"));
        } else {
            player.getInventory().addItem(itemStack);
            player.sendMessage(CC.translate("&a✓ Armadura dada al inventario: &f" + customArmor.getDisplayName()));
        }
    }
}