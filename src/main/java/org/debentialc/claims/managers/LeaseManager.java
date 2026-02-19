package org.debentialc.claims.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.debentialc.Main;
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.claims.models.Terrain;
import org.debentialc.claims.storage.LeaseStorage;
import org.debentialc.service.CC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaseManager {

    public static final int MIN_CHUNKS_TO_LEASE = 4;
    public static final int MIN_CYCLE_DAYS = 3;
    public static final int MAX_CYCLE_DAYS = 20;
    public static final int GRACE_PERIOD_DAYS = 3;

    private static final int RED_WOOL_ID = 35;
    private static final byte RED_WOOL_DATA = 14;
    public static final int PARENT_BORDER_MARGIN = 2;

    private static LeaseManager instance;

    private final Map<String, LeaseContract> contracts = new HashMap<String, LeaseContract>();
    private final LeaseStorage storage;
    private int taskId = -1;

    private LeaseManager() {
        storage = new LeaseStorage();
        for (LeaseContract contract : storage.loadAll()) {
            contracts.put(contract.getContractId(), contract);
        }
        startScheduler();
    }

    public static LeaseManager getInstance() {
        if (instance == null) instance = new LeaseManager();
        return instance;
    }

    private void startScheduler() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, new Runnable() {
            @Override
            public void run() {
                processCycles();
            }
        }, 20L * 60 * 5, 20L * 60 * 5);
    }

    private void processCycles() {
        List<LeaseContract> toProcess = new ArrayList<LeaseContract>(contracts.values());
        for (LeaseContract contract : toProcess) {
            if (contract.getStatus() == LeaseContract.ContractStatus.ACTIVE && contract.isPaymentDue()) processPayment(contract);
            else if (contract.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD && contract.isGraceExpired()) evictTenant(contract);
        }
    }

    private void processPayment(LeaseContract contract) {
        Economy economy = TerrainManager.getInstance().getEconomy();
        if (economy == null) return;
        if (economy.has(contract.getTenantName(), contract.getPricePerCycle())) {
            economy.withdrawPlayer(contract.getTenantName(), contract.getPricePerCycle());
            economy.depositPlayer(contract.getOwnerName(), contract.getPricePerCycle());
            contract.renewPayment();
            storage.saveContract(contract);
            notify(contract.getTenantName(), "&7[Contrato] Pago de &f$" + (int) contract.getPricePerCycle() + " &7debitado — &f" + contract.getSubTerrainId() + "&7.");
            notify(contract.getOwnerName(), "&7[Contrato] Recibiste &f$" + (int) contract.getPricePerCycle() + " &7de &f" + contract.getTenantName() + "&7.");
        } else {
            contract.startGracePeriod();
            storage.saveContract(contract);
            notify(contract.getTenantName(), "&c[Contrato] Sin fondos. Tienes &f" + GRACE_PERIOD_DAYS + " días &cpara pagar o desalojar.");
            notify(contract.getOwnerName(), "&c[Contrato] &f" + contract.getTenantName() + " &cno pudo pagar. Período de gracia iniciado.");
        }
    }

    private void evictTenant(LeaseContract contract) {
        if (contract.getSubTerrainId() != null) {
            removeSubBorders(contract.getSubTerrainId());
            TerrainManager.getInstance().deleteTerrain(contract.getSubTerrainId());
        }
        contract.setStatus(LeaseContract.ContractStatus.EXPIRED);
        storage.saveContract(contract);
        notify(contract.getTenantName(), "&c[Contrato] Período de gracia expirado. Desalojado de &f" + contract.getSubTerrainId() + "&c.");
        notify(contract.getOwnerName(), "&7[Contrato] &f" + contract.getTenantName() + " &7fue desalojado por falta de pago.");
    }

    private void notify(String playerName, String msg) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null) p.sendMessage(CC.translate(msg));
    }

    public String generateContractId(String tenantName, String parentTerrainId) {
        String base = "LEASE_" + tenantName.toUpperCase() + "_" + parentTerrainId.toUpperCase();
        if (!contracts.containsKey(base)) return base;
        int counter = 2;
        while (contracts.containsKey(base + "_" + counter)) counter++;
        return base + "_" + counter;
    }

    public String generateSubTerrainId(String parentId, String tenantName) {
        String base = parentId + "_" + tenantName.toUpperCase();
        if (TerrainManager.getInstance().getTerrain(base) == null) return base;
        int counter = 2;
        while (TerrainManager.getInstance().getTerrain(base + "_" + counter) != null) counter++;
        return base + "_" + counter;
    }

    public LeaseContract offerContract(Terrain parentTerrain, Player owner, UUID tenantId, String tenantName,
                                       int chunks, double price, int cycleDays) {
        if (parentTerrain.getChunks() < MIN_CHUNKS_TO_LEASE) return null;
        if (cycleDays < MIN_CYCLE_DAYS || cycleDays > MAX_CYCLE_DAYS) return null;
        if (chunks < 1 || chunks >= parentTerrain.getChunks()) return null;
        String contractId = generateContractId(tenantName, parentTerrain.getId());
        LeaseContract contract = new LeaseContract(contractId, parentTerrain.getId(),
                owner.getUniqueId(), owner.getName(), tenantId, tenantName,
                chunks, price, cycleDays, LeaseContract.ContractOrigin.OWNER_OFFER);
        contracts.put(contractId, contract);
        storage.saveContract(contract);
        return contract;
    }

    public LeaseContract requestContract(Terrain parentTerrain, Player tenant, UUID ownerId, String ownerName,
                                         int chunks, double price, int cycleDays) {
        if (parentTerrain.getChunks() < MIN_CHUNKS_TO_LEASE) return null;
        if (cycleDays < MIN_CYCLE_DAYS || cycleDays > MAX_CYCLE_DAYS) return null;
        if (chunks < 1 || chunks >= parentTerrain.getChunks()) return null;
        String contractId = generateContractId(tenant.getName(), parentTerrain.getId());
        LeaseContract contract = new LeaseContract(contractId, parentTerrain.getId(),
                ownerId, ownerName, tenant.getUniqueId(), tenant.getName(),
                chunks, price, cycleDays, LeaseContract.ContractOrigin.TENANT_REQUEST);
        contracts.put(contractId, contract);
        storage.saveContract(contract);
        return contract;
    }

    public LeaseContract findPendingByPlayerName(UUID acceptorId, String otherPlayerName) {
        String lower = otherPlayerName.toLowerCase();
        for (LeaseContract c : contracts.values()) {
            if (c.getStatus() == LeaseContract.ContractStatus.PENDING_TENANT
                    && c.getTenantId().equals(acceptorId)
                    && c.getOwnerName().toLowerCase().equals(lower)) return c;
            if (c.getStatus() == LeaseContract.ContractStatus.PENDING_OWNER
                    && c.getOwnerId().equals(acceptorId)
                    && c.getTenantName().toLowerCase().equals(lower)) return c;
        }
        return null;
    }

    public LeaseContract findAwaitingAssignByTenantName(UUID ownerId, String tenantName) {
        String lower = tenantName.toLowerCase();
        for (LeaseContract c : contracts.values()) {
            if (c.getStatus() == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN
                    && c.getOwnerId().equals(ownerId)
                    && c.getTenantName().toLowerCase().equals(lower)) return c;
        }
        return null;
    }

    public LeaseContract findActiveContractByTenantName(UUID ownerId, String tenantName) {
        String lower = tenantName.toLowerCase();
        for (LeaseContract c : contracts.values()) {
            if (c.getOwnerId().equals(ownerId)
                    && c.getTenantName().toLowerCase().equals(lower)
                    && (c.getStatus() == LeaseContract.ContractStatus.ACTIVE
                    || c.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD)) return c;
        }
        return null;
    }

    public LeaseContract findPendingMoveForTenant(UUID tenantId) {
        for (LeaseContract c : contracts.values()) {
            if (c.getTenantId().equals(tenantId) && c.isHasPendingMove()) return c;
        }
        return null;
    }

    public boolean acceptContract(LeaseContract contract, Player acceptor) {
        if (contract == null) return false;
        if (contract.getStatus() == LeaseContract.ContractStatus.PENDING_TENANT) {
            if (!contract.getTenantId().equals(acceptor.getUniqueId())) return false;
        } else if (contract.getStatus() == LeaseContract.ContractStatus.PENDING_OWNER) {
            if (!contract.getOwnerId().equals(acceptor.getUniqueId())) return false;
        } else {
            return false;
        }
        contract.setStatus(LeaseContract.ContractStatus.AWAITING_SUBTERRAIN);
        storage.saveContract(contract);
        return true;
    }

    private boolean isInsideParentWithMargin(Terrain parent, int ox, int oz, int size) {
        if (parent.getOrigin() == null) return false;
        int px = parent.getOrigin().getBlockX();
        int pz = parent.getOrigin().getBlockZ();
        int pSize = parent.getSizeInBlocks();
        int m = PARENT_BORDER_MARGIN;
        return ox >= (px + m) && (ox + size) <= (px + pSize - m)
                && oz >= (pz + m) && (oz + size) <= (pz + pSize - m);
    }

    private boolean subCollidesWithOthers(String excludeSubId, World world, int ox, int oz, int size) {
        for (LeaseContract other : contracts.values()) {
            if (other.getSubTerrainId() == null) continue;
            if (excludeSubId != null && excludeSubId.equals(other.getSubTerrainId())) continue;
            if (other.getStatus() != LeaseContract.ContractStatus.ACTIVE
                    && other.getStatus() != LeaseContract.ContractStatus.GRACE_PERIOD) continue;
            Terrain sub = TerrainManager.getInstance().getTerrain(other.getSubTerrainId());
            if (sub == null || !sub.isCommitted() || sub.getOrigin() == null) continue;
            if (!sub.getOrigin().getWorld().getName().equals(world.getName())) continue;
            int bx1 = sub.getOrigin().getBlockX();
            int bz1 = sub.getOrigin().getBlockZ();
            int bx2 = bx1 + sub.getSizeInBlocks();
            int bz2 = bz1 + sub.getSizeInBlocks();
            boolean overlapX = ox < bx2 && (ox + size) > bx1;
            boolean overlapZ = oz < bz2 && (oz + size) > bz1;
            if (overlapX && overlapZ) return true;
        }
        return false;
    }

    public String validateSelectionForAssign(LeaseContract contract, World world, int ox, int oz, int size) {
        Terrain parent = TerrainManager.getInstance().getTerrain(contract.getParentTerrainId());
        if (parent == null || !parent.isCommitted() || parent.getOrigin() == null)
            return "El terreno padre &f" + contract.getParentTerrainId() + " &7no existe o no está generado.";
        if (!isInsideParentWithMargin(parent, ox, oz, size))
            return "La selección no está completamente dentro del terreno &f" + parent.getId() + " &7(respetando el margen de borde de &f" + PARENT_BORDER_MARGIN + " bloques&7).";
        if (subCollidesWithOthers(null, world, ox, oz, size))
            return "La selección se superpone con un sub-terreno ya existente.";
        return null;
    }

    public String validateSelectionForMove(LeaseContract contract, World world, int ox, int oz, int size) {
        Terrain parent = TerrainManager.getInstance().getTerrain(contract.getParentTerrainId());
        if (parent == null || !parent.isCommitted() || parent.getOrigin() == null)
            return "El terreno padre no existe.";
        if (!isInsideParentWithMargin(parent, ox, oz, size))
            return "La nueva posición no está dentro del terreno &f" + parent.getId() + " &7respetando los bordes.";
        if (subCollidesWithOthers(contract.getSubTerrainId(), world, ox, oz, size))
            return "La nueva posición se superpone con otro sub-terreno existente.";
        Terrain currentSub = TerrainManager.getInstance().getTerrain(contract.getSubTerrainId());
        if (currentSub != null && currentSub.getOrigin() != null) {
            int curOx = currentSub.getOrigin().getBlockX();
            int curOz = currentSub.getOrigin().getBlockZ();
            if (curOx == ox && curOz == oz && currentSub.getOrigin().getWorld().getName().equals(world.getName()))
                return "El sub-terreno ya está en esa posición.";
        }
        return null;
    }

    public boolean assignSubTerrainAtPos(LeaseContract contract, Player owner, World world, int ox, int oz, int y) {
        if (contract.getStatus() != LeaseContract.ContractStatus.AWAITING_SUBTERRAIN) return false;
        if (!contract.getOwnerId().equals(owner.getUniqueId()) && !owner.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) return false;

        String subId = generateSubTerrainId(contract.getParentTerrainId(), contract.getTenantName());
        Terrain subTerrain = new Terrain(subId, contract.getChunks());
        subTerrain.setPrice(contract.getPricePerCycle());
        subTerrain.setOrigin(new Location(world, ox, y, oz));
        subTerrain.setCommitted(true);
        subTerrain.setOwner(contract.getTenantId());
        subTerrain.setOwnerName(contract.getTenantName());

        TerrainManager.getInstance().getAll().put(subId, subTerrain);
        TerrainManager.getInstance().save(subTerrain);

        int size = contract.getChunks() * 16;
        buildSubBorders(world, ox, oz, y, size);
        placeSubSign(subTerrain, world, ox, oz, y, contract);

        contract.setSubTerrainId(subId);

        Economy economy = TerrainManager.getInstance().getEconomy();
        if (economy != null && economy.has(contract.getTenantName(), contract.getPricePerCycle())) {
            economy.withdrawPlayer(contract.getTenantName(), contract.getPricePerCycle());
            economy.depositPlayer(contract.getOwnerName(), contract.getPricePerCycle());
        }

        contract.activate();
        storage.saveContract(contract);
        return true;
    }

    public void requestMoveWithSelection(LeaseContract contract, Player owner, World world, int ox, int oz, int y) {
        contract.requestMove(world.getName(), ox, oz, ox + contract.getChunks() * 16, oz + contract.getChunks() * 16);
        storage.saveContract(contract);
    }

    public boolean acceptMove(String contractId, Player tenant) {
        LeaseContract contract = contracts.get(contractId);
        if (contract == null || !contract.getTenantId().equals(tenant.getUniqueId())) return false;
        if (!contract.isHasPendingMove()) return false;

        World world = Bukkit.getWorld(contract.getPendingMoveWorld());
        if (world == null) {
            contract.clearPendingMove();
            storage.saveContract(contract);
            return false;
        }

        int ox = contract.getPendingMoveX1();
        int oz = contract.getPendingMoveZ1();
        int size = contract.getChunks() * 16;

        Terrain parent = TerrainManager.getInstance().getTerrain(contract.getParentTerrainId());
        String valid = validateSelectionForMove(contract, world, ox, oz, size);
        if (valid != null) {
            contract.clearPendingMove();
            storage.saveContract(contract);
            tenant.sendMessage(CC.translate("&cEl traslado ya no es válido: &7" + valid));
            notify(contract.getOwnerName(), "&c[Contrato] El traslado solicitado de &f" + contract.getTenantName() + " &cya no es válido (el espacio fue ocupado). Solicítalo de nuevo.");
            return false;
        }

        Terrain sub = TerrainManager.getInstance().getTerrain(contract.getSubTerrainId());
        if (sub == null) return false;
        int y = sub.getOrigin() != null ? sub.getOrigin().getBlockY() : tenant.getLocation().getBlockY();

        doMoveSubTerrain(contract, world, ox, oz, y);
        contract.clearPendingMove();
        storage.saveContract(contract);
        return true;
    }

    public boolean declineMove(String contractId, Player tenant) {
        LeaseContract contract = contracts.get(contractId);
        if (contract == null || !contract.getTenantId().equals(tenant.getUniqueId())) return false;
        if (!contract.isHasPendingMove()) return false;
        contract.clearPendingMove();
        storage.saveContract(contract);
        notify(contract.getOwnerName(), "&c[Contrato] &f" + tenant.getName() + " &crechazó el traslado de &f" + contract.getSubTerrainId() + "&c.");
        return true;
    }

    public boolean adminMoveSubTerrain(String contractId, Player admin, World world, int ox, int oz, int y) {
        LeaseContract contract = contracts.get(contractId);
        if (contract == null) return false;
        if (!admin.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) return false;
        int size = contract.getChunks() * 16;
        String err = validateSelectionForMove(contract, world, ox, oz, size);
        if (err != null) {
            admin.sendMessage(CC.translate("&7" + err));
            return false;
        }
        return doMoveSubTerrain(contract, world, ox, oz, y);
    }

    private boolean doMoveSubTerrain(LeaseContract contract, World world, int newOx, int newOz, int y) {
        Terrain sub = TerrainManager.getInstance().getTerrain(contract.getSubTerrainId());
        if (sub == null) return false;

        removeSubBorders(contract.getSubTerrainId());

        sub.setOrigin(new Location(world, newOx, y, newOz));
        TerrainManager.getInstance().save(sub);

        int size = sub.getSizeInBlocks();
        buildSubBorders(world, newOx, newOz, y, size);
        placeSubSign(sub, world, newOx, newOz, y, contract);

        notify(contract.getTenantName(), "&7Tu sub-terreno &f" + contract.getSubTerrainId() + " &7fue movido a &fX=" + newOx + " Z=" + newOz + "&7.");
        notify(contract.getOwnerName(), "&7Sub-terreno &f" + contract.getSubTerrainId() + " &7trasladado.");
        return true;
    }

    private void buildSubBorders(World world, int ox, int oz, int y, int size) {
        for (int x = ox; x < ox + size; x++) {
            setWool(world, x, y, oz);
            setWool(world, x, y, oz + size);
        }
        for (int z = oz; z <= oz + size; z++) {
            setWool(world, ox, y, z);
            setWool(world, ox + size, y, z);
        }
    }

    private void setWool(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setTypeIdAndData(RED_WOOL_ID, RED_WOOL_DATA, false);
    }

    public void removeSubBorders(String subTerrainId) {
        Terrain sub = TerrainManager.getInstance().getTerrain(subTerrainId);
        if (sub == null || sub.getOrigin() == null) return;
        World world = sub.getOrigin().getWorld();
        int ox = sub.getOrigin().getBlockX();
        int oz = sub.getOrigin().getBlockZ();
        int y = sub.getOrigin().getBlockY();
        int size = sub.getSizeInBlocks();
        for (int x = ox; x < ox + size; x++) { clearWool(world, x, y, oz); clearWool(world, x, y, oz + size); }
        for (int z = oz; z <= oz + size; z++) { clearWool(world, ox, y, z); clearWool(world, ox + size, y, z); }
    }

    private void clearWool(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        if (block.getTypeId() == RED_WOOL_ID && block.getData() == RED_WOOL_DATA) block.setTypeId(0, false);
    }

    private void placeSubSign(Terrain subTerrain, World world, int ox, int oz, int y, LeaseContract contract) {
        Block signBlock = world.getBlockAt(ox + 1, y, oz);
        if (signBlock.getTypeId() == 0) {
            signBlock.setTypeIdAndData(63, (byte) 0, false);
            if (signBlock.getState() instanceof Sign) {
                Sign sign = (Sign) signBlock.getState();
                sign.setLine(0, CC.translate("&8[ Sub-Terreno ]"));
                sign.setLine(1, CC.translate("&7" + subTerrain.getId()));
                sign.setLine(2, CC.translate("&f" + contract.getTenantName()));
                sign.setLine(3, CC.translate("&8Inquilino"));
                sign.update(true);
            }
        }
    }

    public boolean cancelContract(String contractId, Player canceller) {
        LeaseContract contract = contracts.get(contractId);
        if (contract == null) return false;
        boolean isOwner = contract.getOwnerId().equals(canceller.getUniqueId());
        boolean isTenant = contract.getTenantId().equals(canceller.getUniqueId());
        boolean isAdmin = canceller.hasPermission(ClaimsPermissions.ADMIN_MANAGE);
        if (!isOwner && !isTenant && !isAdmin) return false;
        if (contract.getStatus() == LeaseContract.ContractStatus.ACTIVE) {
            contract.startGracePeriod();
            storage.saveContract(contract);
            notify(contract.getTenantName(), "&c[Contrato] Tu contrato en &f" + contract.getSubTerrainId() + " &cfue cancelado. Tienes &f3 días &cpara desalojar.");
            if (!canceller.getName().equals(contract.getOwnerName())) notify(contract.getOwnerName(), "&7[Contrato] Contrato con &f" + contract.getTenantName() + " &7cancelado.");
            return true;
        }
        if (contract.getStatus() == LeaseContract.ContractStatus.PENDING_TENANT
                || contract.getStatus() == LeaseContract.ContractStatus.PENDING_OWNER
                || contract.getStatus() == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN) {
            contract.setStatus(LeaseContract.ContractStatus.CANCELLED);
            storage.saveContract(contract);
            return true;
        }
        return false;
    }

    public boolean forceEvict(String contractId, Player admin) {
        LeaseContract contract = contracts.get(contractId);
        if (contract == null || !admin.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) return false;
        evictTenant(contract);
        return true;
    }

    public boolean tryPayGrace(String contractId) {
        LeaseContract contract = contracts.get(contractId);
        if (contract == null || contract.getStatus() != LeaseContract.ContractStatus.GRACE_PERIOD) return false;
        Economy economy = TerrainManager.getInstance().getEconomy();
        if (economy == null || !economy.has(contract.getTenantName(), contract.getPricePerCycle())) return false;
        economy.withdrawPlayer(contract.getTenantName(), contract.getPricePerCycle());
        economy.depositPlayer(contract.getOwnerName(), contract.getPricePerCycle());
        contract.setStatus(LeaseContract.ContractStatus.ACTIVE);
        contract.renewPayment();
        storage.saveContract(contract);
        return true;
    }

    public boolean isSubTerrain(String terrainId) {
        for (LeaseContract c : contracts.values()) {
            if (terrainId.equals(c.getSubTerrainId())) return true;
        }
        return false;
    }

    public LeaseContract getContractBySubTerrain(String subTerrainId) {
        for (LeaseContract c : contracts.values()) {
            if (subTerrainId.equals(c.getSubTerrainId())) return c;
        }
        return null;
    }

    public boolean ownerCanInteractInSubTerrain(UUID actorId, String subTerrainId) {
        LeaseContract contract = getContractBySubTerrain(subTerrainId);
        if (contract == null) return true;
        return !contract.getOwnerId().equals(actorId);
    }

    public List<LeaseContract> getContractsByOwner(UUID ownerId) {
        List<LeaseContract> result = new ArrayList<LeaseContract>();
        for (LeaseContract c : contracts.values()) if (c.getOwnerId().equals(ownerId)) result.add(c);
        return result;
    }

    public List<LeaseContract> getContractsByTenant(UUID tenantId) {
        List<LeaseContract> result = new ArrayList<LeaseContract>();
        for (LeaseContract c : contracts.values()) if (c.getTenantId().equals(tenantId)) result.add(c);
        return result;
    }

    public List<LeaseContract> getPendingForPlayer(UUID playerId) {
        List<LeaseContract> result = new ArrayList<LeaseContract>();
        for (LeaseContract c : contracts.values()) {
            if ((c.getStatus() == LeaseContract.ContractStatus.PENDING_TENANT && c.getTenantId().equals(playerId))
                    || (c.getStatus() == LeaseContract.ContractStatus.PENDING_OWNER && c.getOwnerId().equals(playerId))
                    || (c.getStatus() == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN && c.getOwnerId().equals(playerId)))
                result.add(c);
        }
        return result;
    }

    public LeaseContract getContract(String contractId) { return contracts.get(contractId); }

    public Map<String, LeaseContract> getAll() { return contracts; }

    public void shutdown() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
    }
}