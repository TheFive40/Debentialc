package org.debentialc.raids.models;

/**
 * Estado de una Wave (Oleada)
 */
public enum WaveStatus {
    PENDING("Pendiente"),
    ACTIVE("Activa"),
    IN_PROGRESS("En Progreso"),
    COMPLETED("Completada"),
    FAILED("Fallida");

    private final String displayName;

    WaveStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

