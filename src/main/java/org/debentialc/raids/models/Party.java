package org.debentialc.raids.models;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import java.util.*;

/**
 * Party - Grupo de jugadores que hacen una raid juntos
 */
@Getter
@Setter
public class Party {

    private String partyId;
    private UUID leaderUuid;
    private Map<UUID, PartyMember> members;

    private int maxSize;
    private long createdAt;

    private PartyStatus status;

    public Party(String partyId, UUID leaderUuid, int maxSize) {
        this.partyId = partyId;
        this.leaderUuid = leaderUuid;
        this.maxSize = maxSize;
        this.members = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
        this.status = PartyStatus.WAITING;

        addMember(leaderUuid, true);
    }

    public void addMember(UUID playerUuid, boolean isLeader) {
        if (isFull()) {
            return;
        }

        members.put(playerUuid, new PartyMember(playerUuid, isLeader));
    }

    public void removeMember(UUID playerUuid) {
        members.remove(playerUuid);
    }

    public boolean hasMember(UUID playerUuid) {
        return members.containsKey(playerUuid);
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public boolean canStartRaid() {
        return members.size() >= 2;
    }

    public int getMemberCount() {
        return members.size();
    }

    public UUID getLeader() {
        return leaderUuid;
    }

    public boolean isLeader(UUID playerUuid) {
        return leaderUuid.equals(playerUuid);
    }

    public List<UUID> getActivePlayers() {
        return new ArrayList<>(members.keySet());
    }

    public long getAgeSeconds() {
        return (System.currentTimeMillis() - createdAt) / 1000;
    }

    @Override
    public String toString() {
        return String.format("Party{id='%s', leader=%s, members=%d/%d, status=%s}",
                partyId, leaderUuid, members.size(), maxSize, status);
    }
}

/**
 * Miembro de Party
 */
@Getter
@Setter
class PartyMember {
    private UUID playerUuid;
    private boolean isLeader;
    private long joinedAt;

    public PartyMember(UUID playerUuid, boolean isLeader) {
        this.playerUuid = playerUuid;
        this.isLeader = isLeader;
        this.joinedAt = System.currentTimeMillis();
    }
}