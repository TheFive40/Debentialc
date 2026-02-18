package org.debentialc.raids.commands;

import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;
import org.debentialc.raids.menus.RaidMainMenu;
import org.debentialc.raids.menus.RaidListMenu;
import org.debentialc.raids.menus.RaidChatInputManager;
import org.debentialc.raids.managers.RaidStorageManager;
import org.debentialc.raids.managers.CooldownManager;
import org.debentialc.service.CC;
import org.bukkit.entity.Player;

/**
 * RaidCommand - Comando /raid para acceder al sistema de raids
 * Ahora abre menús visuales (SmartInventory) en lugar de menús de chat
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

        if (args.length() == 0) {
            RaidMainMenu.createMainMenu().open(player);
            return;
        }

        String subCommand = args.getArgs(0).toLowerCase();

        switch (subCommand) {
            case "create":
                RaidChatInputManager.startCreateRaidInput(player);
                break;

            case "list":
                RaidListMenu.createRaidListMenu(1).open(player);
                break;

            case "menu":
                RaidMainMenu.createMainMenu().open(player);
                break;

            case "info":
                sendInfoHelp(player);
                break;

            case "reload":
                RaidStorageManager.loadAllRaids();
                CooldownManager.loadCooldowns();
                player.sendMessage(CC.translate("&a✓ Sistema de raids recargado"));
                player.sendMessage(CC.translate("&7Raids cargadas y cooldowns restaurados"));
                break;

            case "save":
                RaidStorageManager.saveAllRaids();
                RaidStorageManager.saveRaidSystemData();
                CooldownManager.saveCooldowns();
                player.sendMessage(CC.translate("&a✓ Raids y cooldowns guardados"));
                break;

            default:
                player.sendMessage(CC.translate("&c✗ Subcomando no reconocido: " + subCommand));
                player.sendMessage(CC.translate("&eUso: &f/raid <create|list|menu|info|reload|save>"));
        }
    }

    private void sendInfoHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  SISTEMA DE RAIDS"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&f  /raid &7- Abrir menú visual"));
        player.sendMessage(CC.translate("&f  /raid create &7- Crear nueva raid"));
        player.sendMessage(CC.translate("&f  /raid list &7- Ver todas las raids"));
        player.sendMessage(CC.translate("&f  /raid menu &7- Menú principal"));
        player.sendMessage(CC.translate("&f  /raid info &7- Esta información"));
        player.sendMessage(CC.translate("&f  /raid reload &7- Recargar datos"));
        player.sendMessage(CC.translate("&f  /raid save &7- Guardar todo"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }
}