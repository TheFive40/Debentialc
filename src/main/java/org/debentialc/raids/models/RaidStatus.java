package org.debentialc.raids.models;

/**
 * Estado de una Raid
 */
public enum RaidStatus {
    IDLE("Inactivo"),
    IN_PROGRESS("En Progreso"),
    WAVE_ACTIVE("Oleada Activa"),
    WAVE_COOLDOWN("Esperando Siguiente"),
    COMPLETED("Completada"),
    FAILED("Fallida"),
    PAUSED("Pausada");

    private final String displayName;

    RaidStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
