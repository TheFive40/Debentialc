package org.example.commands.items;
import org.example.tools.commands.BaseCommand;
import org.example.tools.commands.Command;
import org.example.tools.commands.CommandArgs;
import org.example.tools.permissions.Permissions;

import java.io.IOException;

public class MenuCommand extends BaseCommand {
    @Command(name = "menu", aliases = "menu", permission = Permissions.COMMAND + "menu")
    @Override
    public void onCommand(CommandArgs command) throws IOException {

    }
}
