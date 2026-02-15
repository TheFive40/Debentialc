package org.debentialc.raids.commands;

import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;
import org.debentialc.raids.managers.PartyManager;
import org.debentialc.raids.managers.RaidManager;
import org.debentialc.raids.managers.RaidSessionManager;
import org.debentialc.raids.models.Party;
import org.debentialc.raids.models.Raid;
import org.debentialc.raids.models.PartyStatus;
import org.debentialc.service.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * PartyCommand - Comando /party para gestionar parties
 * Adaptado al nuevo sistema de menús visuales
 */
public class PartyCommand extends BaseCommand {

    @Command(name = "party",
            description = "Gestiona tu party",
            usage = "/party <create|invite|leave|start|info|members>",
            inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();
        if (player == null) {
            return;
        }

        if (args.length() == 0) {
            sendPartyHelp(player);
            return;
        }

        String subCommand = args.getArgs(0).toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player);
                break;

            case "invite":
                if (args.length() < 2) {
                    sendError(player, "Uso: /party invite <jugador>");
                    return;
                }
                handleInvite(player, args.getArgs(1));
                break;

            case "leave":
                handleLeave(player);
                break;

            case "start":
                if (args.length() < 2) {
                    sendError(player, "Uso: /party start <raid>");
                    return;
                }
                handleStart(player, args.getArgs(1));
                break;

            case "info":
                handleInfo(player);
                break;

            case "members":
                handleMembers(player);
                break;

            case "disband":
                handleDisband(player);
                break;

            default:
                sendError(player, "Subcomando no válido: " + subCommand);
                sendPartyHelp(player);
        }
    }

    private void handleCreate(Player player) {
        Party existingParty = PartyManager.getPlayerParty(player.getUniqueId());
        if (existingParty != null) {
            sendError(player, "Ya estás en una party");
            return;
        }

        Party party = PartyManager.createParty(player.getUniqueId(), 5);
        sendSuccess(player, "Party creada. Invita jugadores con: /party invite <jugador>");
    }

    private void handleInvite(Player player, String playerName) {
        Party party = PartyManager.getPlayerParty(player.getUniqueId());

        if (party == null) {
            sendError(player, "No estás en una party");
            return;
        }

        if (!party.isLeader(player.getUniqueId())) {
            sendError(player, "Solo el líder puede invitar jugadores");
            return;
        }

        if (party.isFull()) {
            sendError(player, "La party está llena (máximo 5)");
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sendError(player, "Jugador no encontrado: " + playerName);
            return;
        }

        if (PartyManager.isPlayerInParty(target.getUniqueId())) {
            sendError(player, target.getName() + " ya está en una party");
            return;
        }

        boolean joined = PartyManager.joinParty(target.getUniqueId(), party);

        if (joined) {
            sendSuccess(player, target.getName() + " se unió a la party");
            sendSuccess(target, "Te has unido a la party de " + player.getName());
        } else {
            sendError(player, "No se pudo agregar a " + target.getName());
        }
    }

    private void handleLeave(Player player) {
        Party party = PartyManager.getPlayerParty(player.getUniqueId());

        if (party == null) {
            sendError(player, "No estás en una party");
            return;
        }

        PartyManager.leaveParty(player.getUniqueId());
        sendSuccess(player, "Has abandonado la party");
    }

    private void handleStart(Player player, String raidName) {
        Party party = PartyManager.getPlayerParty(player.getUniqueId());

        if (party == null) {
            sendError(player, "No estás en una party");
            return;
        }

        if (!party.isLeader(player.getUniqueId())) {
            sendError(player, "Solo el líder puede iniciar la raid");
            return;
        }

        if (!PartyManager.canStartRaid(party)) {
            sendError(player, "Se necesitan al menos 2 jugadores (actual: " + party.getMemberCount() + ")");
            return;
        }

        Raid raid = RaidManager.getRaidByName(raidName);
        if (raid == null) {
            sendError(player, "Raid no encontrada: " + raidName);
            return;
        }

        if (!RaidManager.isRaidReadyToPlay(raid.getRaidId())) {
            sendError(player, "La raid no está configurada correctamente");
            return;
        }

        if (RaidSessionManager.hasActiveSession(raid.getRaidId())) {
            sendError(player, "Otra party ya está haciendo esta raid. Espera a que terminen.");
            return;
        }

        RaidSessionManager.createRaidSession(raid, party);
        PartyManager.setPartyStatus(party, PartyStatus.IN_RAID);

        sendSuccess(player, "Raid iniciada: " + raid.getRaidName());

        for (java.util.UUID memberId : party.getActivePlayers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                sendInfo(member, "La raid ha comenzado. ¡Que empiece la aventura!");
            }
        }
    }

    private void handleInfo(Player player) {
        Party party = PartyManager.getPlayerParty(player.getUniqueId());

        if (party == null) {
            sendError(player, "No estás en una party");
            return;
        }

        player.sendMessage(PartyManager.getPartyInfo(party));
    }

    private void handleMembers(Player player) {
        Party party = PartyManager.getPlayerParty(player.getUniqueId());

        if (party == null) {
            sendError(player, "No estás en una party");
            return;
        }

        player.sendMessage(PartyManager.listPartyMembers(party));
    }

    private void handleDisband(Player player) {
        Party party = PartyManager.getPlayerParty(player.getUniqueId());

        if (party == null) {
            sendError(player, "No estás en una party");
            return;
        }

        if (!party.isLeader(player.getUniqueId())) {
            sendError(player, "Solo el líder puede disolver la party");
            return;
        }

        PartyManager.dissolveParty(party.getPartyId());
        sendSuccess(player, "Party disuelta");
    }

    private void sendPartyHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  COMANDOS DE PARTY"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&f  /party create &7- Crear nueva party"));
        player.sendMessage(CC.translate("&f  /party invite <jugador> &7- Invitar jugador"));
        player.sendMessage(CC.translate("&f  /party leave &7- Abandonar party"));
        player.sendMessage(CC.translate("&f  /party start <raid> &7- Iniciar raid (solo líder)"));
        player.sendMessage(CC.translate("&f  /party info &7- Ver información de party"));
        player.sendMessage(CC.translate("&f  /party members &7- Ver miembros"));
        player.sendMessage(CC.translate("&f  /party disband &7- Disolver party (solo líder)"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }


    private void sendError(Player player, String message) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ &f" + message));
        player.sendMessage("");
    }

    private void sendSuccess(Player player, String message) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ &f" + message));
        player.sendMessage("");
    }

    private void sendInfo(Player player, String message) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&bℹ &f" + message));
        player.sendMessage("");
    }
}