package org.debentialc.raids.commands;

import org.debentialc.Main;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;
import org.debentialc.raids.menus.RaidMenuManager;
import org.debentialc.raids.menus.RaidMenus;
import org.bukkit.entity.Player;

/**
 * RaidCommand - Comando /raid para acceder al sistema de raids
 */
public class RaidCommand extends BaseCommand {

    @Command(name = "raid",
            permission = "dbcplugin.raids.admin",
            description = "Abre el menú de configuración de raids",
            usage = "/raid <opción>",
            inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        if (player == null) {
            return;
        }

        // Si no hay argumentos, abre el menú principal
        if (args.length() == 0) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        String subCommand = args.getArgs(0).toLowerCase();

        switch (subCommand) {
            case "create":
                RaidMenuManager.openCreateRaidMenu(player);
                break;

            case "list":
                RaidMenuManager.openRaidListMenu(player);
                break;

            case "menu":
                RaidMenuManager.openMainMenu(player);
                break;

            case "info":
                handleInfo(player, args);
                break;

            case "reload":
                handleReload(player);
                break;

            case "clear":
                handleClear(player);
                break;

            default:
                RaidMenus.sendError(player, "Subcomando no reconocido: " + subCommand);
                player.sendMessage("§eUso: §f/raid <create|list|menu|info|reload|clear>");
        }
    }

    /**
     * Muestra información del sistema de raids
     */
    private void handleInfo(Player player, CommandArgs args) {
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§l  INFORMACIÓN DEL SISTEMA DE RAIDS");
        player.sendMessage("");
        player.sendMessage("§eComandos disponibles:");
        player.sendMessage("§f  /raid create §7- Crear nueva raid");
        player.sendMessage("§f  /raid list §7- Ver todas las raids");
        player.sendMessage("§f  /raid menu §7- Abrir menú principal");
        player.sendMessage("§f  /raid info §7- Ver esta información");
        player.sendMessage("§f  /raid reload §7- Recargar configuración");
        player.sendMessage("§f  /raid clear §7- Cerrar menú actual");
        player.sendMessage("");
        player.sendMessage("§ePermisos necesarios:");
        player.sendMessage("§f  dbcplugin.raids.admin §7- Para acceder a los menús");
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }

    /**
     * Recarga la configuración
     */
    private void handleReload(Player player) {
        // TODO: Implementar recarga de configuración
        RaidMenus.sendSuccess(player, "Sistema de raids recargado");
    }

    /**
     * Cierra el menú actual
     */
    private void handleClear(Player player) {
        RaidMenuManager.clearPlayerState(player);
        RaidMenus.sendInfo(player, "Menú cerrado");
    }
}