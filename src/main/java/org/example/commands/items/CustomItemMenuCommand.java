package org.example.commands.items;

import org.example.tools.commands.BaseCommand;
import org.example.tools.commands.Command;
import org.example.tools.commands.CommandArgs;
import org.example.tools.inventory.CustomItemMenus;
import org.example.tools.permissions.Permissions;

import java.io.IOException;

public class CustomItemMenuCommand extends BaseCommand {
    @Command(name = "cimenu", aliases = {"cimenu", "cim"}, permission = Permissions.COMMAND + "ci")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        CustomItemMenus.createMainMenu().open(command.getPlayer());
    }
}