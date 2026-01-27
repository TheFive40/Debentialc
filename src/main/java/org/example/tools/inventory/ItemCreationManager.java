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

    /**
     * Inicia el proceso de creación de item para un jugador
     */
    public static void startItemCreation(Player player) {
        playersCreatingItem.put(player.getUniqueId(), true);
        player.sendMessage(CC.translate("&e&l┌─────────────────────────────┐"));
        player.sendMessage(CC.translate("&e&l│  &b&lCREACIÓN DE ITEM CUSTOM   &e&l│"));
        player.sendMessage(CC.translate("&e&l├─────────────────────────────┤"));
        player.sendMessage(CC.translate("&e&l│ &7Escribe el nombre del item &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &7que sostienes en la mano   &e&l│"));
        player.sendMessage(CC.translate("&e&l│ &c(Escribe 'cancelar' para abortar)&e&l│"));
        player.sendMessage(CC.translate("&e&l└─────────────────────────────┘"));
    }

    /**
     * Verifica si un jugador está en proceso de crear un item
     */
    public static boolean isCreatingItem(Player player) {
        return playersCreatingItem.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Procesa el nombre ingresado por el jugador
     */
    public static void processItemCreation(Player player, String itemName) {
        ItemStack item = player.getItemInHand();

        // Validaciones
        if (item == null || item.getTypeId() == 0) {
            player.sendMessage(CC.translate("&c✗ Debes sostener un item en la mano"));
            startItemCreation(player);
            return;
        }

        if (item.getItemMeta() == null) {
            player.sendMessage(CC.translate("&c✗ Este item no tiene metadatos (nombre/lore)"));
            startItemCreation(player);
            return;
        }

        if (itemName.isEmpty()) {
            player.sendMessage(CC.translate("&c✗ El nombre del item no puede estar vacío"));
            startItemCreation(player);
            return;
        }

        // Verificar que el ID no exista
        String itemId = itemName.toLowerCase().replace(" ", "_");
        if (CustomItemCommand.items.containsKey(itemId)) {
            player.sendMessage(CC.translate("&c✗ Ya existe un item con el ID: &f" + itemId));
            startItemCreation(player);
            return;
        }

        // Crear el item custom
        CustomItem customItem = new CustomItem()
                .setId(itemId)
                .setMaterial(item.getTypeId())
                .setLore(item.getItemMeta().getLore())
                .setDisplayName(item.getItemMeta().getDisplayName());

        CustomItemCommand.items.put(itemId, customItem);
        itemStorage.saveItem(customItem);

        // Mensaje de confirmación
        player.sendMessage(CC.translate("&a✓ Item creado correctamente"));
        player.sendMessage(CC.translate("&7ID: &f" + itemId));
        player.sendMessage(CC.translate("&7Nombre: &f" + customItem.getDisplayName()));

        finishItemCreation(player);
    }

    /**
     * Cancela el proceso de creación
     */
    public static void cancelItemCreation(Player player) {
        player.sendMessage(CC.translate("&c✗ Creación de item cancelada"));
        finishItemCreation(player);
    }

    /**
     * Finaliza el proceso de creación
     */
    private static void finishItemCreation(Player player) {
        playersCreatingItem.put(player.getUniqueId(), false);
    }
}