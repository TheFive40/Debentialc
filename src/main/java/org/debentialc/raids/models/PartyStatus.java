package org.debentialc.raids.models;

/**
 * Estado de una Party
 */
public enum PartyStatus {
    WAITING("Esperando"),
    IN_RAID("En Raid"),
    COMPLETED("Completada"),
    DISBANDED("Disuelta");

    private final String displayName;

    PartyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
