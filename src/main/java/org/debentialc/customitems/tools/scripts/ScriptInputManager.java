package org.debentialc.customitems.tools.scripts;

import org.bukkit.entity.Player;
import org.debentialc.service.CC;

import java.util.HashMap;
import java.util.UUID;

/**
 * Gestiona el input de URLs de scripts desde el chat
 */
public class ScriptInputManager {

    public static class ScriptInputState {
        public String itemId;
        public String action; // "add" o "update"

        public ScriptInputState(String itemId, String action) {
            this.itemId = itemId;
            this.action = action;
        }
    }

    private static final HashMap<UUID, ScriptInputState> playersInputting = new HashMap<>();

    public static void startScriptInput(Player player, String itemId, String action) {
        playersInputting.put(player.getUniqueId(), new ScriptInputState(itemId, action));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&5&l  Agregar Script"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Ingresa la URL del script"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Soportado:"));
        player.sendMessage(CC.translate("&f    • GitHub (raw)"));
        player.sendMessage(CC.translate("&f    • Pastebin (raw)"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Ejemplo:"));
        player.sendMessage(CC.translate("&fgithub.com/user/repo/blob/main/script.js"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Escribe &c'cancelar' &7para abortar"));
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    public static boolean isInputtingScript(Player player) {
        return playersInputting.containsKey(player.getUniqueId());
    }

    public static void processScriptInput(Player player, String input) {
        ScriptInputState state = playersInputting.get(player.getUniqueId());
        if (state == null) return;

        String url = input.trim();

        if (url.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ URL vacía"));
            player.sendMessage("");
            startScriptInput(player, state.itemId, state.action);
            return;
        }

        // Validar formato básico de URL
        if (!isValidUrl(url)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ URL inválida"));
            player.sendMessage(CC.translate("&7Debe ser una URL de GitHub o Pastebin"));
            player.sendMessage("");
            startScriptInput(player, state.itemId, state.action);
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&7Descargando script..."));

        // Descargar script
        ScriptManager manager = ScriptManager.getInstance();
        String scriptContent = manager.downloadScript(url);

        if (scriptContent == null || scriptContent.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Error al descargar el script"));
            player.sendMessage(CC.translate("&7Verifica la URL y que sea accesible"));
            player.sendMessage("");
            finishScriptInput(player);
            return;
        }

        player.sendMessage(CC.translate("&a✓ Descargado"));
        player.sendMessage(CC.translate("&7Validando..."));

        // Validar script
        if (!manager.validateScript(scriptContent)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ El script contiene errores de sintaxis"));
            player.sendMessage(CC.translate("&7No se puede agregar un script inválido"));
            player.sendMessage("");
            finishScriptInput(player);
            return;
        }

        player.sendMessage(CC.translate("&a✓ Válido"));
        player.sendMessage(CC.translate("&7Guardando..."));

        // Guardar script
        if (!manager.saveScript(state.itemId, scriptContent, url)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Error al guardar el script"));
            player.sendMessage("");
            finishScriptInput(player);
            return;
        }

        player.sendMessage(CC.translate("&a✓ Guardado"));
        player.sendMessage(CC.translate("&7Cargando en memoria..."));

        // Cargar script en memoria
        if (!manager.loadScript(state.itemId)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Error al cargar el script"));
            player.sendMessage("");
            finishScriptInput(player);
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ ¡Script agregado exitosamente!"));
        player.sendMessage(CC.translate("&7Item: &f" + state.itemId));
        player.sendMessage(CC.translate("&7Archivo: &f" + state.itemId + ".js"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7El script se ejecutará al usar el item"));
        player.sendMessage("");

        String itemId = state.itemId;
        finishScriptInput(player);

        // Reabrir menú
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(
                org.debentialc.Main.instance,
                () -> {
                    org.debentialc.customitems.tools.inventory.ScriptManagementMenu.createScriptMenu(itemId).open(player);
                },
                20L
        );
    }

    public static void cancelScriptInput(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        player.sendMessage("");
        finishScriptInput(player);
    }

    private static void finishScriptInput(Player player) {
        playersInputting.remove(player.getUniqueId());
    }

    /**
     * Valida si una URL tiene un formato básico correcto
     */
    private static boolean isValidUrl(String url) {
        return url.matches("^(https?://)?(www\\.)?(github\\.com|raw\\.githubusercontent\\.com|pastebin\\.com).*") ||
                url.matches("^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*");
    }
}