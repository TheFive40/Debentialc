package org.debentialc.raids.models;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

/**
 * Punto de Aparición - Define dónde y qué NPCs aparecen
 */
@Getter
@Setter
public class SpawnPoint {

    private Location location;
    private String npcName;
    private int npcTab;
    private int quantity;
    private int aliveCount;

    private String displayName;

    public SpawnPoint(Location location, String npcName, int npcTab, int quantity) {
        this.location = location;
        this.npcName = npcName;
        this.npcTab = npcTab;
        this.quantity = quantity;
        this.aliveCount = quantity;
        this.displayName = String.format("Spawn: %s (%dx)", npcName, quantity);
    }

    public boolean allDefeated() {
        return aliveCount <= 0;
    }

    public void decrementAliveCount() {
        if (aliveCount > 0) {
            aliveCount--;
        }
    }

    public void resetAliveCount() {
        aliveCount = quantity;
    }

    public int getRemainingEnemies() {
        return Math.max(0, aliveCount);
    }

    @Override
    public String toString() {
        return String.format("SpawnPoint{location=%s, npc='%s', tab=%d, quantity=%d, alive=%d}",
                location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ(),
                npcName, npcTab, quantity, aliveCount);
    }
}