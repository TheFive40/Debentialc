package org.example.tools.pastebin;

import org.bukkit.entity.Player;
import org.example.tools.CC;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Gestiona la entrada de URLs de pastebin para editar lore
 * VERSIÓN MEJORADA: Mensajes estéticos y limpios
 */
public class PastebinLoreManager {

    public static class PastebinLoreState {
        public String itemId;
        public String type; // "item" o "armor"

        public PastebinLoreState(String itemId, String type) {
            this.itemId = itemId;
            this.type = type;
        }
    }

    private static final HashMap<UUID, PastebinLoreState> playersInputting = new HashMap<>();

    /**
     * Inicia la entrada de URL de pastebin para editar lore
     */
    public static void startPastebinInput(Player player, String itemId, String type) {
        playersInputting.put(player.getUniqueId(), new PastebinLoreState(itemId, type));

        player.closeInventory();
        sendCleanMessage(player, "PASTEBIN LORE", itemId, type);
    }

    /**
     * Verifica si un jugador está esperando URL de pastebin
     */
    public static boolean isInputtingPastebin(Player player) {
        return playersInputting.containsKey(player.getUniqueId());
    }

    /**
     * Procesa la URL de pastebin ingresada
     */
    public static void processPastebinInput(Player player, String input) {
        PastebinLoreState state = playersInputting.get(player.getUniqueId());
        if (state == null) return;

        // Validar que sea una URL válida
        if (!input.contains("pastebin.com")) {
            player.sendMessage(CC.translate("&c✗ URL de pastebin inválida"));
            player.sendMessage(CC.translate("&7Usa: https://pastebin.com/xxxxx"));
            startPastebinInput(player, state.itemId, state.type);
            return;
        }

        // Descargar contenido de pastebin
        player.sendMessage(CC.translate("&f⏳ Procesando..."));

        List<String> loreLines = PastebinReader.getFromPastebin(input);

        if (loreLines == null || loreLines.isEmpty()) {
            player.sendMessage(CC.translate("&c✗ Error al descargar pastebin"));
            player.sendMessage(CC.translate("&7Verifica que la URL sea correcta"));
            startPastebinInput(player, state.itemId, state.type);
            return;
        }

        // Aplicar el lore según el tipo
        if ("item".equals(state.type)) {
            applyLoreToItem(player, state.itemId, loreLines);
        } else if ("armor".equals(state.type)) {
            applyLoreToArmor(player, state.itemId, loreLines);
        }

        player.sendMessage(CC.translate("&a✓ Lore actualizado"));
        player.sendMessage(CC.translate("&7Líneas: &f" + loreLines.size()));

        String type = state.type;
        String itemId = state.itemId;
        finishPastebinInput(player);

        // Reabrir el menú de edición
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.example.Main.instance, () -> {
            if ("item".equals(type)) {
                org.example.tools.inventory.CustomItemMenus.openEditItemMenu(itemId).open(player);
            } else {
                org.example.tools.inventory.CustomArmorMenus.openEditArmorMenu(itemId).open(player);
            }
        }, 1L);
    }

    /**
     * Aplica el lore a un item custom
     */
    private static void applyLoreToItem(Player player, String itemId, List<String> loreLines) {
        if (!org.example.commands.items.CustomItemCommand.items.containsKey(itemId)) {
            player.sendMessage(CC.translate("&c✗ Item no encontrado"));
            return;
        }

        org.example.tools.ci.CustomItem item = org.example.commands.items.CustomItemCommand.items.get(itemId);

        // Aplicar color a cada línea
        java.util.List<String> coloredLore = new java.util.ArrayList<>();
        for (String line : loreLines) {
            coloredLore.add(CC.translate(line));
        }

        item.setLore(coloredLore);
        org.example.commands.items.CustomItemCommand.items.put(itemId, item);

        // Guardar en BD
        org.example.tools.storage.CustomItemStorage storage = new org.example.tools.storage.CustomItemStorage();
        storage.saveItem(item);
    }

    /**
     * Aplica el lore a una armadura custom
     */
    private static void applyLoreToArmor(Player player, String armorId, List<String> loreLines) {
        if (!org.example.commands.items.RegisterItem.items.containsKey(armorId)) {
            player.sendMessage(CC.translate("&c✗ Armadura no encontrada"));
            return;
        }

        org.example.tools.ci.CustomArmor armor = org.example.commands.items.RegisterItem.items.get(armorId);

        // Aplicar color a cada línea
        java.util.List<String> coloredLore = new java.util.ArrayList<>();
        for (String line : loreLines) {
            coloredLore.add(CC.translate(line));
        }

        armor.setLore(coloredLore);
        org.example.commands.items.RegisterItem.items.put(armorId, armor);

        // Guardar en BD
        org.example.tools.storage.CustomArmorStorage storage = new org.example.tools.storage.CustomArmorStorage();
        storage.saveArmor(armor);
    }

    /**
     * Cancela la entrada de pastebin
     */
    public static void cancelPastebinInput(Player player) {
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        finishPastebinInput(player);
    }

    /**
     * Finaliza la entrada de pastebin
     */
    private static void finishPastebinInput(Player player) {
        playersInputting.remove(player.getUniqueId());
    }

    /**
     * Envía un mensaje limpio y estético
     */
    private static void sendCleanMessage(Player player, String title, String itemId, String type) {
        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&3 " + title));
        player.sendMessage(CC.translate("&8 ─────────────────────────"));
        player.sendMessage(CC.translate("&7 • ID: &f" + itemId));
        player.sendMessage(CC.translate("&7 • Tipo: &f" + type.toUpperCase()));
        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&7 Ingresa la URL del pastebin:"));
        player.sendMessage(CC.translate("&f https://pastebin.com/xxxxx"));
        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&7 Escribe 'cancelar' para abortar"));
        player.sendMessage(CC.translate("&8"));
    }
}