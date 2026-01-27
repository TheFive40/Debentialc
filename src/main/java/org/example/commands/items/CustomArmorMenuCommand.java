package org.example.commands.items;

import org.example.tools.commands.BaseCommand;
import org.example.tools.commands.Command;
import org.example.tools.commands.CommandArgs;
import org.example.tools.inventory.CustomArmorMenus;
import org.example.tools.permissions.Permissions;

import java.io.IOException;

public class CustomArmorMenuCommand extends BaseCommand {
    @Command(name = "camenu", aliases = {"camenu", "cam"}, permission = Permissions.COMMAND + "ci")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        CustomArmorMenus.createMainMenu().open(command.getPlayer());
    }
}