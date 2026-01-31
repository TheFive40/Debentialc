package org.debentialc.boosters.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.debentialc.boosters.core.BoosterSettings;
import org.debentialc.boosters.managers.GlobalBoosterManager;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.boosters.models.GlobalBooster;
import org.debentialc.boosters.models.PersonalBooster;
import org.debentialc.boosters.storage.BoosterStorage;

import java.util.List;
import java.util.UUID;

public class BoosterCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("debentialc.booster.admin")) {
            sender.sendMessage("§cNo tienes permiso");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "global":
                return handleGlobalCommand(sender, args);
            case "personal":
                return handlePersonalCommand(sender, args);
            case "config":
                return handleConfigCommand(sender);
            case "info":
                return handleInfoCommand(sender);
            case "save":
                BoosterStorage.saveAllData();
                sender.sendMessage("§aDatos guardados");
                return true;
            case "load":
                BoosterStorage.loadAllData();
                sender.sendMessage("§aDatos cargados");
                return true;
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleGlobalCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /booster global <activate|deactivate|multiplier> [valor]");
            return true;
        }

        String action = args[1].toLowerCase();

        if (action.equals("activate")) {
            if (args.length < 3) {
                sender.sendMessage("§cUso: /booster global activate <multiplicador>");
                return true;
            }
            try {
                double multiplier = Double.parseDouble(args[2]);
                GlobalBoosterManager.activateBooster(multiplier, sender.getName());
                sender.sendMessage("§aBooster global activado con multiplicador: §6" + multiplier + "x");
                Bukkit.broadcastMessage("§e[BOOSTER] §aBooster Global Activado: §6" + multiplier + "x §apor " + sender.getName());
            } catch (NumberFormatException e) {
                sender.sendMessage("§cMultiplicador inválido");
            }
            return true;
        }

        if (action.equals("deactivate")) {
            GlobalBoosterManager.deactivateBooster();
            sender.sendMessage("§aBooster global desactivado");
            Bukkit.broadcastMessage("§e[BOOSTER] §cBooster Global Desactivado");
            return true;
        }

        if (action.equals("multiplier")) {
            GlobalBooster booster = GlobalBoosterManager.getActiveBooster();
            if (booster != null) {
                sender.sendMessage("§aMultiplicador actual: §6" + String.format("%.2f", booster.getMultiplier()) + "x");
                sender.sendMessage("§aTiempo restante: §6" + booster.getFormattedTime());
            } else {
                sender.sendMessage("§cNo hay booster global activo");
            }
            return true;
        }

        sender.sendMessage("§cAcción desconocida");
        return true;
    }

    private boolean handlePersonalCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /booster personal <add|activate|list> [parámetros]");
            return true;
        }

        String action = args[1].toLowerCase();

        if (action.equals("add")) {
            if (args.length < 4) {
                sender.sendMessage("§cUso: /booster personal add <player> <nivel>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage("§cJugador no encontrado");
                return true;
            }
            try {
                int level = Integer.parseInt(args[3]);
                if (level < 1 || level > 5) {
                    sender.sendMessage("§cNivel debe ser entre 1 y 5");
                    return true;
                }
                double mult = BoosterSettings.getPersonalBoosterMultiplier(level);
                PersonalBooster booster = new PersonalBooster(target.getUniqueId(), level, mult);
                PersonalBoosterManager.addBooster(booster);
                sender.sendMessage("§aBooster personal añadido a §6" + target.getName());
                target.sendMessage("§e§lHas recibido un booster personal nivel §6" + level);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cNivel inválido");
            }
            return true;
        }

        if (action.equals("activate")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cDebes ser un jugador");
                return true;
            }
            Player player = (Player) sender;
            List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());
            if (boosters.isEmpty()) {
                player.sendMessage("§cNo tienes boosters personales");
                return true;
            }
            if (args.length < 3) {
                player.sendMessage("§cUso: /booster personal activate <index>");
                listPlayerBoosters(player, boosters);
                return true;
            }
            try {
                int index = Integer.parseInt(args[2]);
                PersonalBoosterManager.activateBooster(player.getUniqueId(), index);
                player.sendMessage("§aBooster activado");
            } catch (NumberFormatException e) {
                player.sendMessage("§cÍndice inválido");
            }
            return true;
        }

        if (action.equals("list")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cDebes ser un jugador");
                return true;
            }
            Player player = (Player) sender;
            List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());
            if (boosters.isEmpty()) {
                player.sendMessage("§cNo tienes boosters personales");
                return true;
            }
            listPlayerBoosters(player, boosters);
            return true;
        }

        sender.sendMessage("§cAcción desconocida");
        return true;
    }

    private boolean handleConfigCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDebes ser un jugador");
            return true;
        }
        Player player = (Player) sender;
        player.openInventory(BoosterConfigMenu.createMainMenu());
        return true;
    }

    private boolean handleInfoCommand(CommandSender sender) {
        sender.sendMessage("§6=== Información de Boosters ===");
        sender.sendMessage("§aBooster Global:");
        GlobalBooster global = GlobalBoosterManager.getActiveBooster();
        if (global != null) {
            sender.sendMessage("  §e- Activo: §6" + global.getMultiplier() + "x");
            sender.sendMessage("  §e- Tiempo: §6" + global.getFormattedTime());
            sender.sendMessage("  §e- Activado por: §6" + global.getActivatedBy());
        } else {
            sender.sendMessage("  §cInactivo");
        }
        return true;
    }

    private void listPlayerBoosters(Player player, List<PersonalBooster> boosters) {
        player.sendMessage("§6=== Tus Boosters Personales ===");
        for (int i = 0; i < boosters.size(); i++) {
            PersonalBooster booster = boosters.get(i);
            String status = booster.isActive() ? "§aActivo" : "§cInactivo";
            player.sendMessage(String.format("  §6[%d] §a%s §6- +%d%% %s",
                    i, booster.getLevelName(), booster.getPercentageBonus(), status));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Ayuda de Boosters ===");
        sender.sendMessage("§a/booster global activate <multiplicador>");
        sender.sendMessage("§a/booster global deactivate");
        sender.sendMessage("§a/booster global multiplier");
        sender.sendMessage("§a/booster personal add <player> <nivel>");
        sender.sendMessage("§a/booster personal activate [index]");
        sender.sendMessage("§a/booster personal list");
        sender.sendMessage("§a/booster config");
        sender.sendMessage("§a/booster info");
    }
}