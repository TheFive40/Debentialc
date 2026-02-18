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

    public void saveContract(LeaseContract contract) {
        File file = new File(folder, contract.getContractId() + ".dat");
        try {
            Properties props = new Properties();
            props.setProperty("contractId", contract.getContractId());
            props.setProperty("parentTerrainId", contract.getParentTerrainId());
            props.setProperty("subTerrainId", contract.getSubTerrainId() != null ? contract.getSubTerrainId() : "");
            props.setProperty("ownerId", contract.getOwnerId().toString());
            props.setProperty("ownerName", contract.getOwnerName());
            props.setProperty("tenantId", contract.getTenantId().toString());
            props.setProperty("tenantName", contract.getTenantName());
            props.setProperty("chunks", String.valueOf(contract.getChunks()));
            props.setProperty("pricePerCycle", String.valueOf(contract.getPricePerCycle()));
            props.setProperty("cycleDays", String.valueOf(contract.getCycleDays()));
            props.setProperty("status", contract.getStatus().name());
            props.setProperty("origin", contract.getOrigin().name());
            props.setProperty("createdAt", String.valueOf(contract.getCreatedAt()));
            props.setProperty("activatedAt", String.valueOf(contract.getActivatedAt()));
            props.setProperty("lastPaymentAt", String.valueOf(contract.getLastPaymentAt()));
            props.setProperty("nextPaymentAt", String.valueOf(contract.getNextPaymentAt()));
            props.setProperty("gracePeriodEndsAt", String.valueOf(contract.getGracePeriodEndsAt()));

            FileOutputStream fos = new FileOutputStream(file);
            props.store(fos, null);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LeaseContract> loadAll() {
        List<LeaseContract> contracts = new ArrayList<LeaseContract>();
        File[] files = folder.listFiles();
        if (files == null) return contracts;

        for (File file : files) {
            if (!file.getName().endsWith(".dat")) continue;
            try {
                Properties props = new Properties();
                FileInputStream fis = new FileInputStream(file);
                props.load(fis);
                fis.close();

                UUID ownerId = UUID.fromString(props.getProperty("ownerId"));
                String ownerName = props.getProperty("ownerName");
                UUID tenantId = UUID.fromString(props.getProperty("tenantId"));
                String tenantName = props.getProperty("tenantName");
                int chunks = Integer.parseInt(props.getProperty("chunks", "1"));
                double pricePerCycle = Double.parseDouble(props.getProperty("pricePerCycle", "0"));
                int cycleDays = Integer.parseInt(props.getProperty("cycleDays", "3"));
                LeaseContract.ContractOrigin origin = LeaseContract.ContractOrigin.valueOf(props.getProperty("origin", "OWNER_OFFER"));

                LeaseContract contract = new LeaseContract(
                        props.getProperty("contractId"),
                        props.getProperty("parentTerrainId"),
                        ownerId, ownerName, tenantId, tenantName,
                        chunks, pricePerCycle, cycleDays, origin
                );

                String subTerrainId = props.getProperty("subTerrainId", "");
                if (!subTerrainId.isEmpty()) {
                    contract.setSubTerrainId(subTerrainId);
                }

                contract.setStatus(LeaseContract.ContractStatus.valueOf(props.getProperty("status", "PENDING_TENANT")));
                contract.setCreatedAt(Long.parseLong(props.getProperty("createdAt", "0")));
                contract.setActivatedAt(Long.parseLong(props.getProperty("activatedAt", "0")));
                contract.setLastPaymentAt(Long.parseLong(props.getProperty("lastPaymentAt", "0")));
                contract.setNextPaymentAt(Long.parseLong(props.getProperty("nextPaymentAt", "0")));
                contract.setGracePeriodEndsAt(Long.parseLong(props.getProperty("gracePeriodEndsAt", "0")));

                contracts.add(contract);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contracts;
    }

    public void deleteContract(String contractId) {
        File file = new File(folder, contractId + ".dat");
        if (file.exists()) file.delete();
    }
}