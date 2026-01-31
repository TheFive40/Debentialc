package org.debentialc.customitems.commands;

import org.debentialc.customitems.tools.commands.BaseCommand;
import org.debentialc.customitems.tools.commands.Command;
import org.debentialc.customitems.tools.commands.CommandArgs;
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