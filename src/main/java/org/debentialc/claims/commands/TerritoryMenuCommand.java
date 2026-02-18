package org.debentialc.claims.commands;

import org.bukkit.entity.Player;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.menus.TerritoryMenu;
import org.debentialc.service.CC;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;

import java.io.IOException;

public class TerritoryMenuCommand extends BaseCommand {

    @Command(name = "territory", aliases = {"tm"}, permission = ClaimsPermissions.TERRAIN_INFO)
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() >= 1) {
            String sub = command.getArgs(0).toLowerCase();
            if (!sub.equals("menu")) {
                player.sendMessage(CC.translate("&7Uso: &f/territory menu &7o &f/tm"));
                return;
            }
        }

        TerritoryMenu.createMainMenu(player).open(player);
    }
}