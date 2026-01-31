package org.debentialc.customitems.commands;

import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;
import org.debentialc.customitems.tools.inventory.CustomItemMenus;
import org.debentialc.customitems.tools.permissions.Permissions;

import java.io.IOException;

public class CustomItemMenuCommand extends BaseCommand {
    @Command(name = "cimenu", aliases = {"cimenu", "cim"}, permission = Permissions.COMMAND + "ci")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        CustomItemMenus.createMainMenu().open(command.getPlayer());
    }
}