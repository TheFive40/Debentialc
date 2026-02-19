package org.debentialc.claims.models;

import java.util.UUID;

public class LeaseContract {

    public enum ContractStatus {
        PENDING_OWNER,
        PENDING_TENANT,
        AWAITING_SUBTERRAIN,
        ACTIVE,
        GRACE_PERIOD,
        EXPIRED,
        CANCELLED
    }

    public enum ContractOrigin {
        OWNER_OFFER,
        TENANT_REQUEST
    }

    private String contractId;
    private String parentTerrainId;
    private String subTerrainId;
    private UUID ownerId;
    private String ownerName;
    private UUID tenantId;
    private String tenantName;
    private int chunks;
    private double pricePerCycle;
    private int cycleDays;
    private ContractStatus status;
    private ContractOrigin origin;
    private long createdAt;
    private long activatedAt;
    private long lastPaymentAt;
    private long nextPaymentAt;
    private long gracePeriodEndsAt;

    private int pendingMoveX1;
    private int pendingMoveZ1;
    private int pendingMoveX2;
    private int pendingMoveZ2;
    private String pendingMoveWorld;
    private boolean hasPendingMove;

    public LeaseContract(String contractId, String parentTerrainId, UUID ownerId, String ownerName,
                         UUID tenantId, String tenantName, int chunks, double pricePerCycle,
                         int cycleDays, ContractOrigin origin) {
        this.contractId = contractId;
        this.parentTerrainId = parentTerrainId;
        this.subTerrainId = null;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.chunks = chunks;
        this.pricePerCycle = pricePerCycle;
        this.cycleDays = cycleDays;
        this.origin = origin;
        this.createdAt = System.currentTimeMillis();
        this.activatedAt = 0;
        this.lastPaymentAt = 0;
        this.nextPaymentAt = 0;
        this.gracePeriodEndsAt = 0;
        this.hasPendingMove = false;
        this.pendingMoveX1 = 0;
        this.pendingMoveZ1 = 0;
        this.pendingMoveX2 = 0;
        this.pendingMoveZ2 = 0;
        this.pendingMoveWorld = "";
        if (origin == ContractOrigin.OWNER_OFFER) {
            this.status = ContractStatus.PENDING_TENANT;
        } else {
            this.status = ContractStatus.PENDING_OWNER;
        }
    }

    public void activate() {
        this.status = ContractStatus.ACTIVE;
        this.activatedAt = System.currentTimeMillis();
        this.lastPaymentAt = System.currentTimeMillis();
        this.nextPaymentAt = System.currentTimeMillis() + (long) cycleDays * 24 * 60 * 60 * 1000L;
    }

    public void renewPayment() {
        this.lastPaymentAt = System.currentTimeMillis();
        this.nextPaymentAt = System.currentTimeMillis() + (long) cycleDays * 24 * 60 * 60 * 1000L;
    }

    public void startGracePeriod() {
        this.status = ContractStatus.GRACE_PERIOD;
        this.gracePeriodEndsAt = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000L;
    }

    public boolean isGraceExpired() {
        return gracePeriodEndsAt > 0 && System.currentTimeMillis() > gracePeriodEndsAt;
    }

    public boolean isPaymentDue() {
        return nextPaymentAt > 0 && System.currentTimeMillis() > nextPaymentAt;
    }

    public long getDaysUntilPayment() {
        if (nextPaymentAt <= 0) return cycleDays;
        long diff = nextPaymentAt - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return diff / (24 * 60 * 60 * 1000L);
    }

    public long getDaysUntilGraceEnd() {
        if (gracePeriodEndsAt <= 0) return 3;
        long diff = gracePeriodEndsAt - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return diff / (24 * 60 * 60 * 1000L);
    }

    public void requestMove(String world, int x1, int z1, int x2, int z2) {
        this.pendingMoveWorld = world;
        this.pendingMoveX1 = x1;
        this.pendingMoveZ1 = z1;
        this.pendingMoveX2 = x2;
        this.pendingMoveZ2 = z2;
        this.hasPendingMove = true;
    }

    public void clearPendingMove() {
        this.hasPendingMove = false;
        this.pendingMoveWorld = "";
        this.pendingMoveX1 = 0;
        this.pendingMoveZ1 = 0;
        this.pendingMoveX2 = 0;
        this.pendingMoveZ2 = 0;
    }

    public String getContractId() { return contractId; }
    public String getParentTerrainId() { return parentTerrainId; }
    public String getSubTerrainId() { return subTerrainId; }
    public void setSubTerrainId(String subTerrainId) { this.subTerrainId = subTerrainId; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public UUID getTenantId() { return tenantId; }
    public String getTenantName() { return tenantName; }
    public int getChunks() { return chunks; }
    public double getPricePerCycle() { return pricePerCycle; }
    public int getCycleDays() { return cycleDays; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public ContractOrigin getOrigin() { return origin; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long v) { this.createdAt = v; }
    public long getActivatedAt() { return activatedAt; }
    public void setActivatedAt(long v) { this.activatedAt = v; }
    public long getLastPaymentAt() { return lastPaymentAt; }
    public void setLastPaymentAt(long v) { this.lastPaymentAt = v; }
    public long getNextPaymentAt() { return nextPaymentAt; }
    public void setNextPaymentAt(long v) { this.nextPaymentAt = v; }
    public long getGracePeriodEndsAt() { return gracePeriodEndsAt; }
    public void setGracePeriodEndsAt(long v) { this.gracePeriodEndsAt = v; }
    public boolean isHasPendingMove() { return hasPendingMove; }
    public void setHasPendingMove(boolean v) { this.hasPendingMove = v; }
    public int getPendingMoveX1() { return pendingMoveX1; }
    public void setPendingMoveX1(int v) { this.pendingMoveX1 = v; }
    public int getPendingMoveZ1() { return pendingMoveZ1; }
    public void setPendingMoveZ1(int v) { this.pendingMoveZ1 = v; }
    public int getPendingMoveX2() { return pendingMoveX2; }
    public void setPendingMoveX2(int v) { this.pendingMoveX2 = v; }
    public int getPendingMoveZ2() { return pendingMoveZ2; }
    public void setPendingMoveZ2(int v) { this.pendingMoveZ2 = v; }
    public String getPendingMoveWorld() { return pendingMoveWorld; }
    public void setPendingMoveWorld(String v) { this.pendingMoveWorld = v; }
}