package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.ci.CustomItem;
import org.example.commands.items.CustomItemCommand;
import org.example.tools.storage.CustomItemStorage;

import java.util.HashMap;
import java.util.UUID;

public class ItemCreationManager {
    private static final HashMap<UUID, Boolean> playersCreatingItem = new HashMap<>();
    private static CustomItemStorage itemStorage = new CustomItemStorage();

    public static void startItemCreation(Player player) {
        playersCreatingItem.put(player.getUniqueId(), true);
        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&c&l» CREAR ITEM"));
        player.sendMessage(CC.translate("&8 ━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&7Escribe el ID del item"));
        player.sendMessage(CC.translate("&7(Escribe &c'cancelar'&7 para abortar)"));
        player.sendMessage(CC.translate("&8"));
    }

    public static boolean isCreatingItem(Player player) {
        return playersCreatingItem.getOrDefault(player.getUniqueId(), false);
    }

    public static void processItemCreation(Player player, String itemName) {
        ItemStack item = player.getItemInHand();

        if (item == null || item.getTypeId() == 0) {
            player.sendMessage(CC.translate("&c✗ Debes sostener un item en la mano"));
            startItemCreation(player);
            return;
        }

        if (item.getItemMeta() == null) {
            player.sendMessage(CC.translate("&c✗ Este item no tiene metadatos"));
            startItemCreation(player);
            return;
        }

        if (itemName.isEmpty()) {
            player.sendMessage(CC.translate("&c✗ El ID no puede estar vacío"));
            startItemCreation(player);
            return;
        }

        String itemId = itemName.toLowerCase().replace(" ", "_");
        if (CustomItemCommand.items.containsKey(itemId)) {
            player.sendMessage(CC.translate("&c✗ Ya existe un item con ese ID"));
            startItemCreation(player);
            return;
        }

        CustomItem customItem = new CustomItem()
                .setId(itemId)
                .setMaterial(item.getTypeId())
                .setLore(item.getItemMeta().getLore())
                .setDisplayName(item.getItemMeta().getDisplayName());

        CustomItemCommand.items.put(itemId, customItem);
        itemStorage.saveItem(customItem);

        player.sendMessage(CC.translate("&a✓ Item creado: &f" + itemId));
        finishItemCreation(player);
    }

    public static void cancelItemCreation(Player player) {
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        finishItemCreation(player);
    }

    private static void finishItemCreation(Player player) {
        playersCreatingItem.put(player.getUniqueId(), false);
    }
}