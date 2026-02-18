package org.debentialc.claims.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
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
    @Setter
    private long activatedAt;
    @Setter
    private long lastPaymentAt;
    @Setter
    private long nextPaymentAt;
    @Setter
    private long gracePeriodEndsAt;

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

    public ContractOrigin getOrigin() { return origin; }

    public long getCreatedAt() { return createdAt; }

    public long getActivatedAt() { return activatedAt; }

    public long getLastPaymentAt() { return lastPaymentAt; }

    public long getNextPaymentAt() { return nextPaymentAt; }

    public long getGracePeriodEndsAt() { return gracePeriodEndsAt; }
}