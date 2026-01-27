package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.ci.CustomItem;
import org.example.commands.items.CustomItemCommand;
import org.example.tools.storage.CustomItemStorage;

import java.util.HashMap;
import java.util.UUID;

/**
 * Gestiona la edición de items (renombre, lore, etc) por chat
 */
public class ItemEditManager {

    public static class ItemEditState {
        public String itemId;
        public String editType; // "rename", "addline", "setline"
        public int lineNumber; // Para setline

        public ItemEditState(String itemId, String editType) {
            this.itemId = itemId;
            this.editType = editType;
        }

        public ItemEditState(String itemId, String editType, int lineNumber) {
            this.itemId = itemId;
            this.editType = editType;
            this.lineNumber = lineNumber;
        }
    }

    private static final HashMap<UUID, ItemEditState> playersEditing = new HashMap<>();

    /**
     * Inicia la edición de un item
     */
    public static void startItemEdit(Player player, String itemId, String editType) {
        playersEditing.put(player.getUniqueId(), new ItemEditState(itemId, editType));

        player.closeInventory();
        player.sendMessage(CC.translate("&e&l┌─────────────────────────────────┐"));
        player.sendMessage(CC.translate("&e&l│  &b&lEDITAR ITEM CUSTOM           &e&l│"));
        player.sendMessage(CC.translate("&e&l├─────────────────────────────────┤"));
        player.sendMessage(CC.translate("&e&l│ &7Item: &f" + itemId + "                 &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &7Tipo: &f" + editType.toUpperCase() + "                    &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &c                               &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &7Escribe el valor en el chat    &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &c(Escribe 'cancelar' para abortar)&e&l│"));
        player.sendMessage(CC.translate("&e&l└─────────────────────────────────┘"));
    }

    public static void startItemEditLine(Player player, String itemId, int lineNumber) {
        ItemEditState state = new ItemEditState(itemId, "setline", lineNumber);
        playersEditing.put(player.getUniqueId(), state);

        player.closeInventory();
        player.sendMessage(CC.translate("&e&l┌─────────────────────────────────┐"));
        player.sendMessage(CC.translate("&e&l│  &b&lEDITAR LORE DEL ITEM        &e&l│"));
        player.sendMessage(CC.translate("&e&l├─────────────────────────────────┤"));
        player.sendMessage(CC.translate("&e&l│ &7Item: &f" + itemId + "                 &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &7Línea: &f" + lineNumber + "                      &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &c                               &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &7Escribe el nuevo texto         &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &c(Escribe 'cancelar' para abortar)&e&l│"));
        player.sendMessage(CC.translate("&e&l└─────────────────────────────────┘"));
    }

    /**
     * Verifica si un jugador está editando un item
     */
    public static boolean isEditingItem(Player player) {
        return playersEditing.containsKey(player.getUniqueId());
    }

    /**
     * Procesa el input de edición
     */
    public static void processItemEdit(Player player, String input) {
        ItemEditState state = playersEditing.get(player.getUniqueId());
        if (state == null) return;

        if (!CustomItemCommand.items.containsKey(state.itemId)) {
            player.sendMessage(CC.translate("&c✗ Item no encontrado"));
            finishItemEdit(player);
            return;
        }

        CustomItem item = CustomItemCommand.items.get(state.itemId);
        CustomItemStorage storage = new CustomItemStorage();

        switch (state.editType.toLowerCase()) {
            case "rename":
                item.setDisplayName(CC.translate(input));
                storage.saveItem(item);
                player.sendMessage(CC.translate("&a✓ Nombre actualizado: &f" + input));
                break;

            case "addline":
                java.util.List<String> lore = item.getLore();
                if (lore == null) {
                    lore = new java.util.ArrayList<>();
                }
                lore.add(CC.translate(input));
                item.setLore(lore);
                storage.saveItem(item);
                player.sendMessage(CC.translate("&a✓ Línea de lore agregada"));
                break;

            case "setline":
                lore = item.getLore();
                if (lore == null || state.lineNumber > lore.size() || state.lineNumber < 1) {
                    player.sendMessage(CC.translate("&c✗ Número de línea inválido"));
                    finishItemEdit(player);
                    return;
                }
                lore.set(state.lineNumber - 1, CC.translate(input));
                item.setLore(lore);
                storage.saveItem(item);
                player.sendMessage(CC.translate("&a✓ Línea " + state.lineNumber + " actualizada"));
                break;
        }

        finishItemEdit(player);
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.example.Main.instance, () -> {
            CustomItemMenus.openEditItemMenu(state.itemId).open(player);
        }, 1L);
    }

    /**
     * Cancela la edición
     */
    public static void cancelItemEdit(Player player) {
        player.sendMessage(CC.translate("&c✗ Edición cancelada"));
        finishItemEdit(player);
    }

    /**
     * Finaliza la edición
     */
    private static void finishItemEdit(Player player) {
        playersEditing.remove(player.getUniqueId());
    }

    /**
     * Da un item custom al jugador
     */
    public static void giveCustomItem(Player player, String itemId) {
        if (!CustomItemCommand.items.containsKey(itemId)) {
            player.sendMessage(CC.translate("&c✗ Item no encontrado"));
            return;
        }

        CustomItem customItem = CustomItemCommand.items.get(itemId);
        ItemStack itemStack = new ItemStack(customItem.getMaterial());
        org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName(customItem.getDisplayName());
        if (customItem.getLore() != null) {
            meta.setLore(customItem.getLore());
        }
        itemStack.setItemMeta(meta);

        // Dar el item al jugador
        if (player.getInventory().firstEmpty() == -1) {
            // Inventario lleno, dropearlo en el suelo
            player.getWorld().dropItem(player.getLocation(), itemStack);
            player.sendMessage(CC.translate("&a✓ Item dado (Inventario lleno, dropeado en el suelo)"));
        } else {
            player.getInventory().addItem(itemStack);
            player.sendMessage(CC.translate("&a✓ Item dado al inventario: &f" + customItem.getDisplayName()));
        }
    }
}