package org.debentialc.raids.managers;

import org.debentialc.raids.models.Party;
import org.debentialc.raids.models.PartyStatus;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PartyManager - Gestor de parties
 * Responsable de crear, manejar y disolver parties
 */
public class PartyManager {

    private static final Map<String, Party> parties = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerToParty = new ConcurrentHashMap<>();
    private static int partyCounter = 0;

    /**
     * Crea una nueva party
     */
    public static Party createParty(UUID leaderUuid, int maxSize) {
        String partyId = "party_" + (++partyCounter) + "_" + System.currentTimeMillis();
        Party party = new Party(partyId, leaderUuid, maxSize);

        parties.put(partyId, party);
        playerToParty.put(leaderUuid, partyId);

        System.out.println("[Raids] Party creada: " + partyId + " - Líder: " + leaderUuid);
        return party;
    }

    /**
     * Obtiene una party por ID
     */
    public static Party getPartyById(String partyId) {
        return parties.get(partyId);
    }

    /**
     * Obtiene la party de un jugador
     */
    public static Party getPlayerParty(UUID playerId) {
        String partyId = playerToParty.get(playerId);
        if (partyId == null) {
            return null;
        }
        return parties.get(partyId);
    }

    /**
     * Obtiene todas las parties activas
     */
    public static Collection<Party> getAllParties() {
        return new ArrayList<>(parties.values());
    }

    /**
     * Agrega un jugador a una party
     */
    public static boolean joinParty(UUID playerId, Party party) {
        if (party == null || party.isFull()) {
            return false;
        }

        // Verificar que no está en otra party
        if (playerToParty.containsKey(playerId)) {
            return false;
        }

        party.addMember(playerId, false);
        playerToParty.put(playerId, party.getPartyId());

        System.out.println("[Raids] Jugador unido a party: " + playerId + " - " + party.getPartyId());
        return true;
    }

    /**
     * Remueve un jugador de una party
     */
    public static void leaveParty(UUID playerId) {
        String partyId = playerToParty.remove(playerId);
        if (partyId == null) {
            return;
        }

        Party party = parties.get(partyId);
        if (party != null) {
            party.removeMember(playerId);
            System.out.println("[Raids] Jugador salió de party: " + playerId + " - " + partyId);

            // Si la party está vacía o sin líder, disolverla
            if (party.getMemberCount() == 0 || !party.getActivePlayers().contains(party.getLeader())) {
                dissolveParty(partyId);
            }
        }
    }

    /**
     * Verifica si un jugador está en una party
     */
    public static boolean isPlayerInParty(UUID playerId) {
        return playerToParty.containsKey(playerId);
    }

    /**
     * Verifica si un jugador es líder de su party
     */
    public static boolean isPartyLeader(UUID playerId) {
        Party party = getPlayerParty(playerId);
        return party != null && party.isLeader(playerId);
    }

    /**
     * Obtiene los miembros de una party
     */
    public static List<UUID> getPartyMembers(Party party) {
        return party != null ? party.getActivePlayers() : new ArrayList<>();
    }

    /**
     * Verifica si una party puede iniciar una raid (mín 2 jugadores)
     */
    public static boolean canStartRaid(Party party) {
        return party != null && party.canStartRaid();
    }
    /**
     * Obtiene el tamaño de una party
     */
    public static int getPartySize(Party party) {
        return party != null ? party.getMemberCount() : 0;
    }

    /**
     * Obtiene los espacios disponibles en una party
     */
    public static int getPartyAvailableSpots(Party party) {
        if (party == null) {
            return 0;
        }
        return party.getMaxSize() - party.getMemberCount();
    }

    /**
     * Verifica si una party está llena
     */
    public static boolean isPartyFull(Party party) {
        return party != null && party.isFull();
    }

    /**
     * Disuelve una party
     */
    public static void dissolveParty(String partyId) {
        Party party = parties.remove(partyId);
        if (party != null) {
            // Remover todos los mapeos de jugadores
            for (UUID memberId : party.getActivePlayers()) {
                playerToParty.remove(memberId);
            }
            System.out.println("[Raids] Party disuelta: " + partyId);
        }
    }

    /**
     * Disuelve la party de un jugador
     */
    public static void dissolvePlayerParty(UUID playerId) {
        String partyId = playerToParty.get(playerId);
        if (partyId != null) {
            dissolveParty(partyId);
        }
    }

    /**
     * Cambia el estado de una party
     */
    public static void setPartyStatus(Party party, PartyStatus status) {
        if (party != null) {
            party.setStatus(status);
            System.out.println("[Raids] Estado de party actualizado: " + party.getPartyId() + " - " + status);
        }
    }

    /**
     * Obtiene información de una party
     */
    public static String getPartyInfo(Party party) {
        if (party == null) {
            return "Party no encontrada";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Información de Party ===\n");
        sb.append("§eID: §f").append(party.getPartyId()).append("\n");
        sb.append("§eLíder: §f").append(party.getLeader()).append("\n");
        sb.append("§eMiembros: §f").append(party.getMemberCount()).append("/").append(party.getMaxSize()).append("\n");
        sb.append("§eEstado: §f").append(party.getStatus().getDisplayName()).append("\n");
        sb.append("§eAntiguedad: §f").append(party.getAgeSeconds()).append("s\n");
        sb.append("§ePodrá iniciar raid: §f").append(canStartRaid(party) ? "✓" : "✗ (mín 2)");

        return sb.toString();
    }

    /**
     * Lista todos los miembros de una party
     */
    public static String listPartyMembers(Party party) {
        if (party == null) {
            return "Party no encontrada";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Miembros de Party ===\n");

        int index = 1;
        for (UUID memberId : party.getActivePlayers()) {
            String leader = party.isLeader(memberId) ? "§e[LÍDER]" : "";
            sb.append(String.format("§e[%d] §f%s %s\n", index++, memberId, leader));
        }

        return sb.toString();
    }

    /**
     * Obtiene el total de parties activas
     */
    public static int getTotalParties() {
        return parties.size();
    }

    /**
     * Obtiene el total de jugadores en parties
     */
    public static int getTotalPlayersInParties() {
        int total = 0;
        for (Party party : parties.values()) {
            total += party.getMemberCount();
        }
        return total;
    }

    /**
     * Verifica si un jugador es miembro de una party específica
     */
    public static boolean isMemberOfParty(UUID playerId, Party party) {
        return party != null && party.hasMember(playerId);
    }

    /**
     * Obtiene todas las parties en estado WAITING
     */
    public static List<Party> getWaitingParties() {
        List<Party> waiting = new ArrayList<>();
        for (Party party : parties.values()) {
            if (party.getStatus() == PartyStatus.WAITING) {
                waiting.add(party);
            }
        }
        return waiting;
    }

    /**
     * Obtiene todas las parties en raid
     */
    public static List<Party> getPartiesInRaid() {
        List<Party> inRaid = new ArrayList<>();
        for (Party party : parties.values()) {
            if (party.getStatus() == PartyStatus.IN_RAID) {
                inRaid.add(party);
            }
        }
        return inRaid;
    }

    /**
     * Limpia todas las parties
     */
    public static void clearAllParties() {
        parties.clear();
        playerToParty.clear();
        System.out.println("[Raids] Todas las parties han sido limpiadas");
    }

    /**
     * Obtiene todos los IDs de parties
     */
    public static Set<String> getAllPartyIds() {
        return new HashSet<>(parties.keySet());
    }
}