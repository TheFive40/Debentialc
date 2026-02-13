package org.debentialc.raids.models;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import java.util.*;

/**
 * Modelo de Raid - Contiene toda la información de una raid
 */
@Getter
@Setter
public class Raid {

    private String raidId;
    private String raidName;
    private String description;

    private Location arenaSpawnPoint;
    private Location playerSpawnPoint;

    private List<Wave> waves;

    private RaidStatus status;

    private long cooldownSeconds;

    private int minPlayers;
    private int maxPlayers;

    private boolean enabled;
    private long createdAt;
    private String createdBy;

    public Raid(String raidId, String raidName) {
        this.raidId = raidId;
        this.raidName = raidName;
        this.waves = new ArrayList<>();
        this.status = RaidStatus.IDLE;
        this.minPlayers = 2;
        this.maxPlayers = 5;
        this.cooldownSeconds = 3600;
        this.enabled = true;
        this.createdAt = System.currentTimeMillis();
    }

    public Wave getWaveByIndex(int index) {
        if (index >= 0 && index < waves.size()) {
            return waves.get(index);
        }
        return null;
    }

    public void addWave(Wave wave) {
        waves.add(wave);
    }

    public int getTotalWaves() {
        return waves.size();
    }

    public boolean isConfigured() {
        return arenaSpawnPoint != null &&
                playerSpawnPoint != null &&
                !waves.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Raid{id='%s', name='%s', waves=%d, status=%s}",
                raidId, raidName, waves.size(), status);
    }
}