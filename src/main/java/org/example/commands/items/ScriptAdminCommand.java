package org.example.commands.items;

import org.bukkit.entity.Player;
import org.example.tools.CC;
import org.example.tools.commands.BaseCommand;
import org.example.tools.commands.Command;
import org.example.tools.commands.CommandArgs;
import org.example.tools.permissions.Permissions;
import org.example.tools.scripts.ScriptManager;

import java.io.IOException;
import java.util.Map;

/**
 * Comando de administración para el sistema de scripts
 */
public class ScriptAdminCommand extends BaseCommand {

    @Command(name = "scriptadmin", aliases = {"scriptadmin", "sadmin"}, permission = Permissions.COMMAND + "scriptadmin")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        String arg0 = command.getArgs(0);

        switch (arg0.toLowerCase()) {
            case "list":
                listScripts(player);
                break;

            case "reload":
                if (command.length() < 2) {
                    reloadAll(player);
                } else {
                    reloadScript(player, command.getArgs(1));
                }
                break;

            case "delete":
                if (command.length() < 2) {
                    player.sendMessage(CC.translate("&cUso: /sadmin delete <itemId>"));
                    return;
                }
                deleteScript(player, command.getArgs(1));
                break;

            case "info":
                if (command.length() < 2) {
                    player.sendMessage(CC.translate("&cUso: /sadmin info <itemId>"));
                    return;
                }
                showInfo(player, command.getArgs(1));
                break;

            default:
                sendHelp(player);
                break;
        }
    }

    private void listScripts(Player player) {
        Map<String, ScriptManager.ScriptMetadata> scripts = ScriptManager.getInstance().getAllScripts();

        if (scripts.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&7No hay scripts cargados"));
            player.sendMessage("");
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&5&lScripts Cargados"));
        player.sendMessage(CC.translate("&7Total: &f" + scripts.size()));
        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");

        for (Map.Entry<String, ScriptManager.ScriptMetadata> entry : scripts.entrySet()) {
            String itemId = entry.getKey();
            ScriptManager.ScriptMetadata metadata = entry.getValue();

            player.sendMessage(CC.translate("&b• &f" + itemId));
            player.sendMessage(CC.translate("  &7Archivo: &f" + metadata.getFileName()));
            player.sendMessage("");
        }

        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    private void reloadAll(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Recargando todos los scripts..."));

        ScriptManager.getInstance().reloadAllScripts();

        int total = ScriptManager.getInstance().getAllScripts().size();

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Scripts recargados"));
        player.sendMessage(CC.translate("&7Total: &f" + total));
        player.sendMessage("");
    }

    private void reloadScript(Player player, String itemId) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Recargando script de &f" + itemId + "&7..."));

        boolean success = ScriptManager.getInstance().reloadScript(itemId);

        if (success) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Script recargado"));
            player.sendMessage("");
        } else {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Error al recargar"));
            player.sendMessage(CC.translate("&7Verifica que el archivo exista"));
            player.sendMessage("");
        }
    }

    private void deleteScript(Player player, String itemId) {
        if (!ScriptManager.getInstance().hasScript(itemId)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ No existe script para: &f" + itemId));
            player.sendMessage("");
            return;
        }

        boolean success = ScriptManager.getInstance().deleteScript(itemId);

        if (success) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Script eliminado"));
            player.sendMessage(CC.translate("&7Item: &f" + itemId));
            player.sendMessage("");
        } else {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Error al eliminar"));
            player.sendMessage("");
        }
    }

    private void showInfo(Player player, String itemId) {
        if (!ScriptManager.getInstance().hasScript(itemId)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ No existe script para: &f" + itemId));
            player.sendMessage("");
            return;
        }

        ScriptManager.ScriptMetadata metadata = ScriptManager.getInstance().getMetadata(itemId);

        player.sendMessage("");
        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&5&lInfo del Script"));
        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&7Item: &f" + itemId));
        player.sendMessage(CC.translate("&7Archivo: &f" + itemId + ".js"));

        if (metadata != null) {
            player.sendMessage(CC.translate("&7URL: &f" + metadata.getSourceUrl()));

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String date = sdf.format(new java.util.Date(metadata.getDownloadDate()));
            player.sendMessage(CC.translate("&7Descargado: &f" + date));
        }

        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&5&lScript Admin - Ayuda"));
        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&e/sadmin list"));
        player.sendMessage(CC.translate("&7  Lista todos los scripts"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/sadmin reload [itemId]"));
        player.sendMessage(CC.translate("&7  Recarga script(s)"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/sadmin delete <itemId>"));
        player.sendMessage(CC.translate("&7  Elimina un script"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/sadmin info <itemId>"));
        player.sendMessage(CC.translate("&7  Muestra info del script"));
        player.sendMessage(CC.translate("&5&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }
}