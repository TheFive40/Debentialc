package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.ci.CustomItem;
import org.example.commands.items.CustomItemCommand;
import org.example.tools.storage.CustomItemStorage;

import java.util.HashMap;
import java.util.UUID;

public class ItemEditManager {

    public static class ItemEditState {
        public String itemId;
        public String editType;
        public int lineNumber;

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

    public static void startItemEdit(Player player, String itemId, String editType) {
        playersEditing.put(player.getUniqueId(), new ItemEditState(itemId, editType));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&c&l  Editar Item"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Tipo: &f" + editType.toUpperCase()));
        player.sendMessage(CC.translate("&7  Ingresa el nuevo valor"));
        player.sendMessage(CC.translate("&7  Escribe &c'cancelar' &7para abortar"));
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    public static boolean isEditingItem(Player player) {
        return playersEditing.containsKey(player.getUniqueId());
    }

    public static void processItemEdit(Player player, String input) {
        ItemEditState state = playersEditing.get(player.getUniqueId());
        if (state == null) return;

        if (!CustomItemCommand.items.containsKey(state.itemId)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Item no encontrado"));
            player.sendMessage("");
            finishItemEdit(player);
            return;
        }

        CustomItem item = CustomItemCommand.items.get(state.itemId);
        CustomItemStorage storage = new CustomItemStorage();

        switch (state.editType.toLowerCase()) {
            case "rename":
                item.setDisplayName(CC.translate(input));
                storage.saveItem(item);
                player.sendMessage("");
                player.sendMessage(CC.translate("&a✓ Nombre actualizado"));
                player.sendMessage("");
                break;

            case "addline":
                java.util.List<String> lore = item.getLore();
                if (lore == null) {
                    lore = new java.util.ArrayList<>();
                }
                lore.add(CC.translate(input));
                item.setLore(lore);
                storage.saveItem(item);
                player.sendMessage("");
                player.sendMessage(CC.translate("&a✓ Línea agregada"));
                player.sendMessage("");
                break;

            case "setline":
                lore = item.getLore();
                if (lore == null || state.lineNumber > lore.size() || state.lineNumber < 1) {
                    player.sendMessage("");
                    player.sendMessage(CC.translate("&c✗ Número de línea inválido"));
                    player.sendMessage("");
                    finishItemEdit(player);
                    return;
                }
                lore.set(state.lineNumber - 1, CC.translate(input));
                item.setLore(lore);
                storage.saveItem(item);
                player.sendMessage("");
                player.sendMessage(CC.translate("&a✓ Línea actualizada"));
                player.sendMessage("");
                break;
        }

        finishItemEdit(player);
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.example.Main.instance, () -> {
            CustomItemMenus.openEditItemMenu(state.itemId).open(player);
        }, 1L);
    }

    public static void cancelItemEdit(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        player.sendMessage("");
        finishItemEdit(player);
    }

    private static void finishItemEdit(Player player) {
        playersEditing.remove(player.getUniqueId());
    }

    public static void giveCustomItem(Player player, String itemId) {
        if (!CustomItemCommand.items.containsKey(itemId)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Item no encontrado"));
            player.sendMessage("");
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

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), itemStack);
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Item entregado (soltado)"));
            player.sendMessage("");
        } else {
            player.getInventory().addItem(itemStack);
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Item entregado"));
            player.sendMessage("");
        }
    }
}