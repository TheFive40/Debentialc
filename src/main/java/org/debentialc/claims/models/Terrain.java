package org.debentialc.claims.models;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Terrain {

    public enum MemberRole {
        BUILD, BREAK, CONTAINERS, ALL
    }

    private String id;
    private int chunks;
    private double price;
    private UUID owner;
    private String ownerName;
    private Location origin;
    private boolean committed;
    private Map<UUID, List<MemberRole>> members;

    public Terrain(String id, int chunks) {
        this.id = id;
        this.chunks = chunks;
        this.price = 0;
        this.owner = null;
        this.ownerName = null;
        this.origin = null;
        this.committed = false;
        this.members = new HashMap<UUID, List<MemberRole>>();
    }

    public String getId() { return id; }

    public int getChunks() { return chunks; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Location getOrigin() { return origin; }
    public void setOrigin(Location origin) { this.origin = origin; }

    public boolean isCommitted() { return committed; }
    public void setCommitted(boolean committed) { this.committed = committed; }

    public Map<UUID, List<MemberRole>> getMembers() { return members; }

    public boolean hasOwner() { return owner != null; }

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public boolean hasMemberRole(UUID uuid, MemberRole role) {
        if (!members.containsKey(uuid)) return false;
        List<MemberRole> roles = members.get(uuid);
        return roles.contains(MemberRole.ALL) || roles.contains(role);
    }

    public void addMemberRole(UUID uuid, String playerName, MemberRole role) {
        if (!members.containsKey(uuid)) {
            members.put(uuid, new ArrayList<MemberRole>());
        }
        if (!members.get(uuid).contains(role)) {
            members.get(uuid).add(role);
        }
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public int getSizeInBlocks() {
        return chunks * 16;
    }

    public boolean isInsideTerrain(Location loc) {
        if (origin == null) return false;
        if (!loc.getWorld().getName().equals(origin.getWorld().getName())) return false;
        int ox = origin.getBlockX();
        int oz = origin.getBlockZ();
        int size = getSizeInBlocks();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        return x >= ox && x < ox + size && z >= oz && z < oz + size;
    }

    public Location getSignLocation() {
        if (origin == null) return null;
        return new Location(origin.getWorld(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
    }
}