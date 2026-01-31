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

/**
 * Comando principal del sistema de boosters
 * VERSIÓN CORREGIDA: Mejor manejo de errores y validaciones
 */
public class BoosterCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("debentialc.booster.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando");
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
                sender.sendMessage("§aDatos guardados exitosamente");
                return true;

            case "load":
                BoosterStorage.loadAllData();
                sender.sendMessage("§aDatos cargados exitosamente");
                return true;

            case "help":
                sendHelp(sender);
                return true;

            default:
                sender.sendMessage("§cSubcomando desconocido: " + subcommand);
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Maneja los comandos de booster global
     */
    private boolean handleGlobalCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /booster global <activate|deactivate|multiplier>");
            return true;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "activate":
                if (args.length < 3) {
                    sender.sendMessage("§cUso: /booster global activate <multiplicador>");
                    return true;
                }

                try {
                    double multiplier = Double.parseDouble(args[2]);

                    if (multiplier <= 0) {
                        sender.sendMessage("§cEl multiplicador debe ser mayor a 0");
                        return true;
                    }

                    GlobalBoosterManager.activateBooster(multiplier, sender.getName());
                    sender.sendMessage("§aBooster global activado con multiplicador: §6" + multiplier + "x");
                    Bukkit.broadcastMessage("§e[BOOSTER] §aBooster Global Activado: §6" + multiplier + "x §apor " + sender.getName());

                } catch (NumberFormatException e) {
                    sender.sendMessage("§cMultiplicador inválido. Debe ser un número");
                }
                return true;

            case "deactivate":
                if (GlobalBoosterManager.isBoosterActive()) {
                    GlobalBoosterManager.deactivateBooster();
                    sender.sendMessage("§aBooster global desactivado");
                    Bukkit.broadcastMessage("§e[BOOSTER] §cBooster Global Desactivado");
                } else {
                    sender.sendMessage("§cNo hay ningún booster global activo");
                }
                return true;

            case "multiplier":
            case "info":
                GlobalBooster booster = GlobalBoosterManager.getActiveBooster();
                if (booster != null) {
                    sender.sendMessage("§6=== Booster Global ===");
                    sender.sendMessage("§aMultiplicador: §6" + String.format("%.2f", booster.getMultiplier()) + "x");
                    sender.sendMessage("§aTiempo restante: §6" + booster.getFormattedTime());
                    sender.sendMessage("§aActivado por: §6" + booster.getActivatedBy());
                } else {
                    sender.sendMessage("§cNo hay booster global activo");
                }
                return true;

            default:
                sender.sendMessage("§cAcción desconocida: " + action);
                sender.sendMessage("§7Acciones: activate, deactivate, multiplier");
                return true;
        }
    }

    /**
     * Maneja los comandos de booster personal
     */
    private boolean handlePersonalCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /booster personal <add|activate|list>");
            return true;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 4) {
                    sender.sendMessage("§cUso: /booster personal add <jugador> <nivel>");
                    sender.sendMessage("§7Niveles disponibles: 1-5");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("§cJugador no encontrado: " + args[2]);
                    return true;
                }

                try {
                    int level = Integer.parseInt(args[3]);

                    if (level < 1 || level > 5) {
                        sender.sendMessage("§cEl nivel debe ser entre 1 y 5");
                        return true;
                    }

                    double mult = BoosterSettings.getPersonalBoosterMultiplier(level);
                    PersonalBooster booster = new PersonalBooster(target.getUniqueId(), level, mult);
                    PersonalBoosterManager.addBooster(booster);

                    sender.sendMessage("§aBooster personal nivel §6" + level + " §aañadido a §6" + target.getName());
                    target.sendMessage("§e§l¡Has recibido un booster personal nivel §6" + level + "!");

                } catch (NumberFormatException e) {
                    sender.sendMessage("§cNivel inválido. Debe ser un número entre 1 y 5");
                }
                return true;

            case "activate":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cDebes ser un jugador para activar boosters personales");
                    return true;
                }

                Player player = (Player) sender;
                List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());

                if (boosters.isEmpty()) {
                    player.sendMessage("§cNo tienes boosters personales disponibles");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage("§cUso: /booster personal activate <índice>");
                    listPlayerBoosters(player, boosters);
                    return true;
                }

                try {
                    int index = Integer.parseInt(args[2]);

                    if (index < 0 || index >= boosters.size()) {
                        player.sendMessage("§cÍndice inválido. Usa un número entre 0 y " + (boosters.size() - 1));
                        listPlayerBoosters(player, boosters);
                        return true;
                    }

                    PersonalBoosterManager.activateBooster(player.getUniqueId(), index);
                    player.sendMessage("§aBooster personal activado exitosamente");

                } catch (NumberFormatException e) {
                    player.sendMessage("§cÍndice inválido. Debe ser un número");
                }
                return true;

            case "list":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cDebes ser un jugador para ver tus boosters");
                    return true;
                }

                Player player2 = (Player) sender;
                List<PersonalBooster> boosters2 = PersonalBoosterManager.getPlayerBoosters(player2.getUniqueId());

                if (boosters2.isEmpty()) {
                    player2.sendMessage("§cNo tienes boosters personales");
                    return true;
                }

                listPlayerBoosters(player2, boosters2);
                return true;

            default:
                sender.sendMessage("§cAcción desconocida: " + action);
                sender.sendMessage("§7Acciones: add, activate, list");
                return true;
        }
    }

    /**
     * Abre el menú de configuración
     */
    private boolean handleConfigCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDebes ser un jugador para abrir el menú de configuración");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(org.debentialc.boosters.commands.BoosterConfigMenu.createMainMenu());
        return true;
    }

    /**
     * Muestra información de boosters activos
     */
    private boolean handleInfoCommand(CommandSender sender) {
        sender.sendMessage("§6=== Información de Boosters ===");
        sender.sendMessage("");

        // Info de booster global
        sender.sendMessage("§aBooster Global:");
        GlobalBooster global = GlobalBoosterManager.getActiveBooster();
        if (global != null) {
            sender.sendMessage("  §e- Activo: §6" + global.getMultiplier() + "x");
            sender.sendMessage("  §e- Tiempo restante: §6" + global.getFormattedTime());
            sender.sendMessage("  §e- Activado por: §6" + global.getActivatedBy());
        } else {
            sender.sendMessage("  §cInactivo");
        }

        sender.sendMessage("");

        // Info de booster personal (si es un jugador)
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage("§aTu Booster Personal:");

            PersonalBooster personal = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
            if (personal != null) {
                sender.sendMessage("  §e- Nivel: §6" + personal.getLevelName());
                sender.sendMessage("  §e- Multiplicador: §6" + personal.getMultiplier() + "x");
                sender.sendMessage("  §e- Bonus: §6+" + personal.getPercentageBonus() + "%");
            } else {
                sender.sendMessage("  §cInactivo");
            }
        }

        sender.sendMessage("");
        return true;
    }

    /**
     * Lista los boosters personales de un jugador
     */
    private void listPlayerBoosters(Player player, List<PersonalBooster> boosters) {
        player.sendMessage("§6=== Tus Boosters Personales ===");

        for (int i = 0; i < boosters.size(); i++) {
            PersonalBooster booster = boosters.get(i);
            String status = booster.isActive() ? "§aActivo" : "§7Disponible";

            player.sendMessage(String.format("  §6[%d] §e%s §7- §6+%d%% §7(%s)",
                    i,
                    booster.getLevelName(),
                    booster.getPercentageBonus(),
                    status));
        }

        player.sendMessage("");
        player.sendMessage("§7Usa §e/booster personal activate <índice> §7para activar");
    }

    /**
     * Muestra el menú de ayuda
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Sistema de Boosters - Ayuda ===");
        sender.sendMessage("");

        sender.sendMessage("§e§lBoosters Globales:");
        sender.sendMessage("§a/booster global activate <multiplicador>");
        sender.sendMessage("§7  Activa un booster global");
        sender.sendMessage("§a/booster global deactivate");
        sender.sendMessage("§7  Desactiva el booster global");
        sender.sendMessage("§a/booster global multiplier");
        sender.sendMessage("§7  Muestra info del booster global");

        sender.sendMessage("");

        sender.sendMessage("§e§lBoosters Personales:");
        sender.sendMessage("§a/booster personal add <jugador> <nivel>");
        sender.sendMessage("§7  Da un booster personal a un jugador");
        sender.sendMessage("§a/booster personal activate <índice>");
        sender.sendMessage("§7  Activa uno de tus boosters");
        sender.sendMessage("§a/booster personal list");
        sender.sendMessage("§7  Lista tus boosters disponibles");

        sender.sendMessage("");

        sender.sendMessage("§e§lOtros:");
        sender.sendMessage("§a/booster config");
        sender.sendMessage("§7  Abre el menú de configuración");
        sender.sendMessage("§a/booster info");
        sender.sendMessage("§7  Muestra información de boosters activos");
        sender.sendMessage("§a/booster save/load");
        sender.sendMessage("§7  Guarda o carga datos manualmente");

        sender.sendMessage("");
    }
}