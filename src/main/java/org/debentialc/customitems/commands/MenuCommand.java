package org.debentialc.customitems.commands;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;
import org.debentialc.customitems.tools.permissions.Permissions;

import java.io.IOException;

public class MenuCommand extends BaseCommand {
    @Command(name = "menu", aliases = "menu", permission = Permissions.COMMAND + "menu")
    @Override
    public void onCommand(CommandArgs command) throws IOException {

    }
}
