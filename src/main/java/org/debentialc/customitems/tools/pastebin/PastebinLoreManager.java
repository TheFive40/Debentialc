package org.debentialc.customitems.tools.pastebin;

import org.bukkit.entity.Player;
import org.debentialc.customitems.commands.CustomItemCommand;
import org.debentialc.customitems.commands.RegisterItem;
import org.debentialc.customitems.tools.CC;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

    public static void startPastebinInput(Player player, String itemId, String type) {
        playersInputting.put(player.getUniqueId(), new PastebinLoreState(itemId, type));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&3&l  Pastebin Lore Editor"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Ingresa la URL de Pastebin"));
        player.sendMessage(CC.translate("&7  Formatos válidos:"));
        player.sendMessage(CC.translate("&f  • &bpastebin.com/xxxxx"));
        player.sendMessage(CC.translate("&f  • &bpastebin.com/raw/xxxxx"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Escribe &c'cancelar' &7para abortar"));
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    public static boolean isInputtingPastebin(Player player) {
        return playersInputting.containsKey(player.getUniqueId());
    }

    public static void processPastebinInput(Player player, String input) {
        PastebinLoreState state = playersInputting.get(player.getUniqueId());
        if (state == null) return;

        // Validar formato URL
        String url = input.trim();
        if (!isValidPastebinUrl(url)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ URL inválida"));
            player.sendMessage(CC.translate("&7Usa: &fpastebin.com/xxxxx"));
            player.sendMessage("");
            startPastebinInput(player, state.itemId, state.type);
            return;
        }

        // Convertir a formato raw si es necesario
        String rawUrl = convertToRawUrl(url);

        // Descargar contenido
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Descargando..."));

        List<String> lines = PastebinReader.downloadPastebinContent(rawUrl);

        if (lines == null || lines.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Error al descargar"));
            player.sendMessage(CC.translate("&7Verifica la URL"));
            player.sendMessage("");
            finishPastebinInput(player);
            return;
        }

        // Aplicar lore según el tipo
        boolean success = false;
        if ("item".equals(state.type)) {
            success = applyLoreToItem(player, state.itemId, lines);
        } else if ("armor".equals(state.type)) {
            success = applyLoreToArmor(player, state.itemId, lines);
        }

        if (success) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Lore actualizado"));
            player.sendMessage(CC.translate("&7Líneas: &f" + lines.size()));
            player.sendMessage("");
        } else {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Error al aplicar lore"));
            player.sendMessage("");
        }

        String type = state.type;
        String itemId = state.itemId;
        finishPastebinInput(player);

        // Reabrir menú después de 1 segundo
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(
                org.debentialc.Main.instance,
                () -> {
                    if ("item".equals(type)) {
                        org.debentialc.customitems.tools.inventory.CustomItemMenus.openEditItemMenu(itemId).open(player);
                    } else {
                        org.debentialc.customitems.tools.inventory.CustomArmorMenus.openEditArmorMenu(itemId).open(player);
                    }
                },
                20L
        );
    }

    private static boolean applyLoreToItem(Player player, String itemId, List<String> lines) {
        if (!CustomItemCommand.items.containsKey(itemId)) {
            return false;
        }

        org.debentialc.customitems.tools.ci.CustomItem item = CustomItemCommand.items.get(itemId);

        // Traducir códigos de color
        List<String> translatedLines = new java.util.ArrayList<>();
        for (String line : lines) {
            translatedLines.add(CC.translate(line));
        }

        item.setLore(translatedLines);

        // Guardar en BD
        org.debentialc.customitems.tools.storage.CustomItemStorage storage = new org.debentialc.customitems.tools.storage.CustomItemStorage();
        storage.saveItem(item);

        return true;
    }

    private static boolean applyLoreToArmor(Player player, String armorId, List<String> lines) {
        if (!RegisterItem.items.containsKey(armorId)) {
            return false;
        }

        org.debentialc.customitems.tools.ci.CustomArmor armor = RegisterItem.items.get(armorId);

        // Traducir códigos de color
        List<String> translatedLines = new java.util.ArrayList<>();
        for (String line : lines) {
            translatedLines.add(CC.translate(line));
        }

        armor.setLore(translatedLines);

        // Guardar en BD
        org.debentialc.customitems.tools.storage.CustomArmorStorage storage = new org.debentialc.customitems.tools.storage.CustomArmorStorage();
        storage.saveArmor(armor);

        return true;
    }

    private static boolean isValidPastebinUrl(String url) {
        return url.matches("^(https?://)?(www\\.)?pastebin\\.com/(raw/)?[a-zA-Z0-9]+$") ||
                url.matches("^[a-zA-Z0-9]+$");
    }

    private static String convertToRawUrl(String url) {
        // Si es solo el código
        if (url.matches("^[a-zA-Z0-9]+$")) {
            return "https://pastebin.com/raw/" + url;
        }

        // Si ya es raw
        if (url.contains("/raw/")) {
            if (!url.startsWith("http")) {
                return "https://" + url;
            }
            return url;
        }

        // Convertir a raw
        url = url.replace("https://", "").replace("http://", "").replace("www.", "");
        String code = url.replace("pastebin.com/", "");
        return "https://pastebin.com/raw/" + code;
    }

    public static void cancelPastebinInput(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        player.sendMessage("");
        finishPastebinInput(player);
    }

    private static void finishPastebinInput(Player player) {
        playersInputting.remove(player.getUniqueId());
    }
}