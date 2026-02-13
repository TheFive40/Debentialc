package org.debentialc.raids.models;

import lombok.Getter;
import lombok.Setter;
import java.util.*;

/**
 * Modelo de Oleada (Wave) - Cada raid tiene múltiples oleadas
 */
@Getter
@Setter
public class Wave {

    private int waveNumber;
    private List<SpawnPoint> spawnPoints;
    private List<WaveReward> rewards;

    private WaveStatus status;

    private String description;

    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.spawnPoints = new ArrayList<>();
        this.rewards = new ArrayList<>();
        this.status = WaveStatus.PENDING;
    }

    public void addSpawnPoint(SpawnPoint spawnPoint) {
        spawnPoints.add(spawnPoint);
    }

    public void addReward(WaveReward reward) {
        rewards.add(reward);
    }

    public int getTotalEnemies() {
        return spawnPoints.stream().mapToInt(SpawnPoint::getQuantity).sum();
    }

    public List<SpawnPoint> getActiveSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }

    public boolean hasRewards() {
        return !rewards.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Wave{number=%d, spawnPoints=%d, totalEnemies=%d}",
                waveNumber, spawnPoints.size(), getTotalEnemies());
    }
}