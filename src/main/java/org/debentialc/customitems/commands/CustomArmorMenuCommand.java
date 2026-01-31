package org.debentialc.customitems.commands;

import org.debentialc.customitems.tools.commands.BaseCommand;
import org.debentialc.customitems.tools.commands.Command;
import org.debentialc.customitems.tools.commands.CommandArgs;
import org.debentialc.customitems.tools.inventory.CustomArmorMenus;
import org.debentialc.customitems.tools.permissions.Permissions;

import java.io.IOException;

public class CustomArmorMenuCommand extends BaseCommand {
    @Command(name = "camenu", aliases = {"camenu", "cam"}, permission = Permissions.COMMAND + "ci")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        CustomArmorMenus.createMainMenu().open(command.getPlayer());
    }
}