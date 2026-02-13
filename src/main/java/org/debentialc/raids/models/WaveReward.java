package org.debentialc.raids.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Recompensa de Oleada - Comandos que se ejecutan al completar una oleada
 */
@Getter
@Setter
public class WaveReward {

    private String command;
    private int probability;

    private String description;
    private int executionOrder;

    public WaveReward(String command, int probability) {
        this.command = command;
        this.probability = Math.max(0, Math.min(100, probability));
        this.executionOrder = 0;
    }

    public boolean shouldExecute() {
        return Math.random() * 100 < probability;
    }

    public String getCommand() {
        return command;
    }

    public void setProbability(int probability) {
        this.probability = Math.max(0, Math.min(100, probability));
    }

    @Override
    public String toString() {
        return String.format("WaveReward{command='%s', probability=%d%%}", command, probability);
    }
}