package org.debentialc.claims.storage;

import org.debentialc.Main;
import org.debentialc.claims.models.LeaseContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class LeaseStorage {

    private final File folder;

    public LeaseStorage() {
        folder = new File(Main.instance.getDataFolder(), "claims" + File.separator + "leases");
        folder.mkdirs();
    }

    public void saveContract(LeaseContract c) {
        File file = new File(folder, c.getContractId() + ".dat");
        try {
            Properties p = new Properties();
            p.setProperty("contractId", c.getContractId());
            p.setProperty("parentTerrainId", c.getParentTerrainId());
            p.setProperty("subTerrainId", c.getSubTerrainId() != null ? c.getSubTerrainId() : "");
            p.setProperty("ownerId", c.getOwnerId().toString());
            p.setProperty("ownerName", c.getOwnerName());
            p.setProperty("tenantId", c.getTenantId().toString());
            p.setProperty("tenantName", c.getTenantName());
            p.setProperty("chunks", String.valueOf(c.getChunks()));
            p.setProperty("pricePerCycle", String.valueOf(c.getPricePerCycle()));
            p.setProperty("cycleDays", String.valueOf(c.getCycleDays()));
            p.setProperty("status", c.getStatus().name());
            p.setProperty("origin", c.getOrigin().name());
            p.setProperty("createdAt", String.valueOf(c.getCreatedAt()));
            p.setProperty("activatedAt", String.valueOf(c.getActivatedAt()));
            p.setProperty("lastPaymentAt", String.valueOf(c.getLastPaymentAt()));
            p.setProperty("nextPaymentAt", String.valueOf(c.getNextPaymentAt()));
            p.setProperty("gracePeriodEndsAt", String.valueOf(c.getGracePeriodEndsAt()));
            p.setProperty("hasPendingMove", String.valueOf(c.isHasPendingMove()));
            p.setProperty("pendingMoveX1", String.valueOf(c.getPendingMoveX1()));
            p.setProperty("pendingMoveZ1", String.valueOf(c.getPendingMoveZ1()));
            p.setProperty("pendingMoveX2", String.valueOf(c.getPendingMoveX2()));
            p.setProperty("pendingMoveZ2", String.valueOf(c.getPendingMoveZ2()));
            p.setProperty("pendingMoveWorld", c.getPendingMoveWorld() != null ? c.getPendingMoveWorld() : "");
            FileOutputStream fos = new FileOutputStream(file);
            p.store(fos, null);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LeaseContract> loadAll() {
        List<LeaseContract> list = new ArrayList<LeaseContract>();
        File[] files = folder.listFiles();
        if (files == null) return list;
        for (File file : files) {
            if (!file.getName().endsWith(".dat")) continue;
            try {
                Properties p = new Properties();
                FileInputStream fis = new FileInputStream(file);
                p.load(fis);
                fis.close();

                LeaseContract c = new LeaseContract(
                        p.getProperty("contractId"),
                        p.getProperty("parentTerrainId"),
                        UUID.fromString(p.getProperty("ownerId")),
                        p.getProperty("ownerName"),
                        UUID.fromString(p.getProperty("tenantId")),
                        p.getProperty("tenantName"),
                        Integer.parseInt(p.getProperty("chunks", "1")),
                        Double.parseDouble(p.getProperty("pricePerCycle", "0")),
                        Integer.parseInt(p.getProperty("cycleDays", "3")),
                        LeaseContract.ContractOrigin.valueOf(p.getProperty("origin", "OWNER_OFFER"))
                );
                String sub = p.getProperty("subTerrainId", "");
                if (!sub.isEmpty()) c.setSubTerrainId(sub);
                c.setStatus(LeaseContract.ContractStatus.valueOf(p.getProperty("status", "PENDING_TENANT")));
                c.setCreatedAt(Long.parseLong(p.getProperty("createdAt", "0")));
                c.setActivatedAt(Long.parseLong(p.getProperty("activatedAt", "0")));
                c.setLastPaymentAt(Long.parseLong(p.getProperty("lastPaymentAt", "0")));
                c.setNextPaymentAt(Long.parseLong(p.getProperty("nextPaymentAt", "0")));
                c.setGracePeriodEndsAt(Long.parseLong(p.getProperty("gracePeriodEndsAt", "0")));
                c.setHasPendingMove(Boolean.parseBoolean(p.getProperty("hasPendingMove", "false")));
                c.setPendingMoveX1(Integer.parseInt(p.getProperty("pendingMoveX1", "0")));
                c.setPendingMoveZ1(Integer.parseInt(p.getProperty("pendingMoveZ1", "0")));
                c.setPendingMoveX2(Integer.parseInt(p.getProperty("pendingMoveX2", "0")));
                c.setPendingMoveZ2(Integer.parseInt(p.getProperty("pendingMoveZ2", "0")));
                c.setPendingMoveWorld(p.getProperty("pendingMoveWorld", ""));
                list.add(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public void deleteContract(String contractId) {
        File f = new File(folder, contractId + ".dat");
        if (f.exists()) f.delete();
    }
}