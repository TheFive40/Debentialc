package org.debentialc.boosters.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.debentialc.boosters.core.BoosterParser;
import org.debentialc.boosters.core.BoosterSettings;
import org.debentialc.boosters.managers.GlobalBoosterManager;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.boosters.models.GlobalBooster;
import org.debentialc.boosters.models.PersonalBooster;
import org.debentialc.boosters.storage.BoosterStorage;
import org.debentialc.service.CC;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.customitems.tools.permissions.Permissions;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;

import java.io.IOException;
import java.util.List;

public class BoosterCommand extends BaseCommand {

    @Command(name = "booster", aliases = {"booster", "boosters", "boost"}, permission = Permissions.COMMAND + "booster")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        String arg0 = command.getArgs(0);

        switch (arg0.toLowerCase()) {
            case "global":
                handleGlobalCommand(command);
                break;

            case "personal":
                handlePersonalCommand(command);
                break;

            case "info":
                handleInfoCommand(command);
                break;

            case "save":
                BoosterStorage.saveAllData();
                command.getSender().sendMessage(CC.translate("&aDatos guardados exitosamente"));
                break;

            case "load":
                BoosterStorage.loadAllData();
                command.getSender().sendMessage(CC.translate("&aDatos cargados exitosamente"));
                break;

            case "help":
                sendHelp(player);
                break;

            default:
                command.getSender().sendMessage(CC.translate("&cSubcomando desconocido: " + arg0));
                sendHelp(player);
                break;
        }
    }

    private void handleGlobalCommand(CommandArgs command) {
        if (command.length() < 2) {
            command.getSender().sendMessage(CC.translate("&cUso: /booster global <activate|deactivate|info>"));
            return;
        }

        String action = command.getArgs(1).toLowerCase();

        switch (action) {
            case "activate":
                if (command.length() < 3) {
                    command.getSender().sendMessage(CC.translate("&cUso: /booster global activate <porcentaje> [tiempo]"));
                    command.getSender().sendMessage(CC.translate("&7Ejemplo: /booster global activate 50% 1h"));
                    command.getSender().sendMessage(CC.translate("&7Ejemplo: /booster global activate 150% 2h30m"));
                    command.getSender().sendMessage(CC.translate("&7Si no especificas tiempo, usa la duración por defecto"));
                    return;
                }

                try {
                    String percentageStr = command.getArgs(2);
                    double multiplier = BoosterParser.parsePercentageToMultiplier(percentageStr);

                    long duration = BoosterSettings.getGlobalBoosterDuration();

                    if (command.length() >= 4) {
                        String timeStr = command.getArgs(3);
                        duration = BoosterParser.parseTimeToSeconds(timeStr);
                    }

                    GlobalBoosterManager.activateBooster(multiplier, command.getSender().getName(), duration);

                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(multiplier);
                    String timeDisplay = BoosterParser.formatSecondsToTime(duration);

                    command.getSender().sendMessage(CC.translate("&aBooster global activado:"));
                    command.getSender().sendMessage(CC.translate("  &6Bonus: &a+" + percentDisplay));
                    command.getSender().sendMessage(CC.translate("  &6Multiplicador: &ax" + String.format("%.2f", multiplier)));
                    command.getSender().sendMessage(CC.translate("  &6Duración: &a" + timeDisplay));

                    Bukkit.broadcastMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    Bukkit.broadcastMessage(CC.translate("&6&l⚡ BOOSTER GLOBAL ACTIVADO ⚡"));
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(CC.translate("  &eBono: &a+" + percentDisplay));
                    Bukkit.broadcastMessage(CC.translate("  &eDuración: &f" + timeDisplay));
                    Bukkit.broadcastMessage(CC.translate("  &eActivado por: &6" + command.getSender().getName()));
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

                } catch (IllegalArgumentException e) {
                    command.getSender().sendMessage(CC.translate("&c✗ Error: " + e.getMessage()));
                    command.getSender().sendMessage(CC.translate("&7Formato: /booster global activate <porcentaje> [tiempo]"));
                    command.getSender().sendMessage(CC.translate("&7Ejemplos:"));
                    command.getSender().sendMessage(CC.translate("&f  /booster global activate 50%"));
                    command.getSender().sendMessage(CC.translate("&f  /booster global activate 150% 1h"));
                    command.getSender().sendMessage(CC.translate("&f  /booster global activate 200% 2h30m"));
                }
                break;

            case "deactivate":
                if (GlobalBoosterManager.isBoosterActive()) {
                    GlobalBoosterManager.deactivateBooster();
                    command.getSender().sendMessage(CC.translate("&aBooster global desactivado"));
                    Bukkit.broadcastMessage(CC.translate("&e[BOOSTER] &cBooster Global Desactivado"));
                } else {
                    command.getSender().sendMessage(CC.translate("&cNo hay booster global activo"));
                }
                break;

            case "info":
            case "multiplier":
                GlobalBooster booster = GlobalBoosterManager.getActiveBooster();
                if (booster != null) {
                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(booster.getMultiplier());

                    command.getSender().sendMessage(CC.translate("&6=== Booster Global ==="));
                    command.getSender().sendMessage(CC.translate("&aBono: &6+" + percentDisplay));
                    command.getSender().sendMessage(CC.translate("&aMultiplicador: &6x" + String.format("%.2f", booster.getMultiplier())));
                    command.getSender().sendMessage(CC.translate("&aTiempo restante: &6" + booster.getFormattedTime()));
                    command.getSender().sendMessage(CC.translate("&aActivado por: &6" + booster.getActivatedBy()));
                } else {
                    command.getSender().sendMessage(CC.translate("&cNo hay booster global activo"));
                }
                break;

            default:
                command.getSender().sendMessage(CC.translate("&cAcción desconocida: " + action));
                break;
        }
    }

    private void handlePersonalCommand(CommandArgs command) {
        if (command.length() < 2) {
            command.getSender().sendMessage(CC.translate("&cUso: /booster personal <add|activate|list>"));
            return;
        }

        String action = command.getArgs(1).toLowerCase();

        switch (action) {
            case "add":
                if (command.length() < 4) {
                    command.getSender().sendMessage(CC.translate("&cUso: /booster personal add <jugador> <nivel>"));
                    command.getSender().sendMessage(CC.translate("&7Niveles: 1-5"));
                    return;
                }

                Player target = Bukkit.getPlayer(command.getArgs(2));
                if (target == null) {
                    command.getSender().sendMessage(CC.translate("&cJugador no encontrado: " + command.getArgs(2)));
                    return;
                }

                try {
                    int level = Integer.parseInt(command.getArgs(3));

                    if (level < 1 || level > 5) {
                        command.getSender().sendMessage(CC.translate("&cNivel debe ser entre 1 y 5"));
                        return;
                    }

                    double mult = BoosterSettings.getPersonalBoosterMultiplier(level);
                    PersonalBooster booster = new PersonalBooster(target.getUniqueId(), level, mult);
                    PersonalBoosterManager.addBooster(booster);

                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(mult);

                    command.getSender().sendMessage(CC.translate("&aBooster nivel &6" + level + " &aañadido a &6" + target.getName()));
                    command.getSender().sendMessage(CC.translate("  &7Bonus: &a+" + percentDisplay));

                    target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    target.sendMessage(CC.translate("&6&l⚡ BOOSTER RECIBIDO ⚡"));
                    target.sendMessage("");
                    target.sendMessage(CC.translate("  &eNivel: &6" + level));
                    target.sendMessage(CC.translate("  &eBono: &a+" + percentDisplay));
                    target.sendMessage("");
                    target.sendMessage(CC.translate("&7Usa &e/booster personal list &7para ver tus boosters"));
                    target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

                } catch (NumberFormatException e) {
                    command.getSender().sendMessage(CC.translate("&cNivel inválido"));
                }
                break;

            case "activate":
                if (!command.isPlayer()) {
                    command.getSender().sendMessage(CC.translate("&cDebes ser un jugador"));
                    return;
                }

                Player player = command.getPlayer();
                List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());

                if (boosters.isEmpty()) {
                    player.sendMessage(CC.translate("&cNo tienes boosters personales"));
                    return;
                }

                if (command.length() < 3) {
                    player.sendMessage(CC.translate("&cUso: /booster personal activate <índice>"));
                    listPlayerBoosters(player, boosters);
                    return;
                }

                try {
                    int index = Integer.parseInt(command.getArgs(2));

                    if (index < 0 || index >= boosters.size()) {
                        player.sendMessage(CC.translate("&cÍndice inválido: 0-" + (boosters.size() - 1)));
                        listPlayerBoosters(player, boosters);
                        return;
                    }

                    PersonalBooster boosterToActivate = boosters.get(index);
                    PersonalBoosterManager.activateBooster(player.getUniqueId(), index);

                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(boosterToActivate.getMultiplier());
                    String timeDisplay = BoosterParser.formatSecondsToTime(BoosterSettings.getPersonalBoosterDuration());

                    player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    player.sendMessage(CC.translate("&b&l⚡ BOOSTER ACTIVADO ⚡"));
                    player.sendMessage("");
                    player.sendMessage(CC.translate("  &eNivel: &6" + boosterToActivate.getLevelName()));
                    player.sendMessage(CC.translate("  &eBono: &a+" + percentDisplay));
                    player.sendMessage(CC.translate("  &eDuración: &f" + timeDisplay));
                    player.sendMessage("");
                    player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

                } catch (NumberFormatException e) {
                    player.sendMessage(CC.translate("&cÍndice inválido"));
                }
                break;

            case "list":
                if (!command.isPlayer()) {
                    command.getSender().sendMessage(CC.translate("&cDebes ser un jugador"));
                    return;
                }

                Player player2 = command.getPlayer();
                List<PersonalBooster> boosters2 = PersonalBoosterManager.getPlayerBoosters(player2.getUniqueId());

                if (boosters2.isEmpty()) {
                    player2.sendMessage(CC.translate("&cNo tienes boosters personales"));
                    return;
                }

                listPlayerBoosters(player2, boosters2);
                break;

            default:
                command.getSender().sendMessage(CC.translate("&cAcción desconocida: " + action));
                break;
        }
    }

    private void handleConfigCommand(CommandArgs command) {
        if (!command.isPlayer()) {
            command.getSender().sendMessage(CC.translate("&cDebes ser un jugador"));
            return;
        }}

    private void handleInfoCommand(CommandArgs command) {
        command.getSender().sendMessage(CC.translate("&6=== Información de Boosters ==="));
        command.getSender().sendMessage("");

        command.getSender().sendMessage(CC.translate("&aBooster Global:"));
        GlobalBooster global = GlobalBoosterManager.getActiveBooster();
        if (global != null) {
            String percentDisplay = BoosterParser.formatMultiplierAsPercentage(global.getMultiplier());
            command.getSender().sendMessage(CC.translate("  &e- Bono: &a+" + percentDisplay));
            command.getSender().sendMessage(CC.translate("  &e- Multiplicador: &ax" + String.format("%.2f", global.getMultiplier())));
            command.getSender().sendMessage(CC.translate("  &e- Tiempo: &6" + global.getFormattedTime()));
            command.getSender().sendMessage(CC.translate("  &e- Activado por: &6" + global.getActivatedBy()));
        } else {
            command.getSender().sendMessage(CC.translate("  &cInactivo"));
        }

        command.getSender().sendMessage("");

        if (command.isPlayer()) {
            Player player = command.getPlayer();
            command.getSender().sendMessage(CC.translate("&aTu Booster Personal:"));

            PersonalBooster personal = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
            if (personal != null) {
                String percentDisplay = BoosterParser.formatMultiplierAsPercentage(personal.getMultiplier());
                command.getSender().sendMessage(CC.translate("  &e- Nivel: &6" + personal.getLevelName()));
                command.getSender().sendMessage(CC.translate("  &e- Bono: &a+" + percentDisplay));
                command.getSender().sendMessage(CC.translate("  &e- Multiplicador: &ax" + String.format("%.2f", personal.getMultiplier())));
            } else {
                command.getSender().sendMessage(CC.translate("  &cInactivo"));
            }
        }

        command.getSender().sendMessage("");
    }

    private void listPlayerBoosters(Player player, List<PersonalBooster> boosters) {
        player.sendMessage(CC.translate("&6=== Tus Boosters Personales ==="));

        for (int i = 0; i < boosters.size(); i++) {
            PersonalBooster booster = boosters.get(i);
            String status = booster.isActive() ? "&aActivo" : "&7Disponible";
            String percentDisplay = BoosterParser.formatMultiplierAsPercentage(booster.getMultiplier());

            player.sendMessage(CC.translate(String.format("  &6[%d] &e%s &7- &a+%s &7(%s)",
                    i,
                    booster.getLevelName(),
                    percentDisplay,
                    status)));
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&7Usa &e/booster personal activate <índice> &7para activar"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&6=== Sistema de Boosters ==="));
        player.sendMessage("");

        player.sendMessage(CC.translate("&e&lBoosters Globales:"));
        player.sendMessage(CC.translate("&a/booster global activate <porcentaje> [tiempo]"));
        player.sendMessage(CC.translate("  &7Ejemplo: /booster global activate 50% 1h"));
        player.sendMessage(CC.translate("  &7Ejemplo: /booster global activate 150% 2h30m"));
        player.sendMessage(CC.translate("&a/booster global deactivate"));
        player.sendMessage(CC.translate("&a/booster global info"));

        player.sendMessage("");

        player.sendMessage(CC.translate("&e&lBoosters Personales:"));
        player.sendMessage(CC.translate("&a/booster personal add <jugador> <nivel>"));
        player.sendMessage(CC.translate("&a/booster personal activate <índice>"));
        player.sendMessage(CC.translate("&a/booster personal list"));

        player.sendMessage("");

        player.sendMessage(CC.translate("&e&lOtros:"));
        player.sendMessage(CC.translate("&a/booster config"));
        player.sendMessage(CC.translate("&a/booster info"));

        player.sendMessage("");

        player.sendMessage(CC.translate("&7Formatos de tiempo: 1d 2h 30m 45s"));
        player.sendMessage(CC.translate("&7d=días, h=horas, m=minutos, s=segundos"));
    }
}