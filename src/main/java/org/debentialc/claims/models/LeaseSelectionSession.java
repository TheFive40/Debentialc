package org.debentialc.claims.models;

import org.bukkit.Location;

public class LeaseSelectionSession {

    public enum SessionMode {
        ASSIGN,
        MOVE
    }

    private final String contractId;
    private final SessionMode mode;
    private Location pos1;
    private Location pos2;

    public LeaseSelectionSession(String contractId, SessionMode mode) {
        this.contractId = contractId;
        this.mode = mode;
        this.pos1 = null;
        this.pos2 = null;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public String getContractId() { return contractId; }
    public SessionMode getMode() { return mode; }
    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }
    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }
}