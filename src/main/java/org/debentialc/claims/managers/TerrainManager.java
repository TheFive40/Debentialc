package org.debentialc.claims.managers;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.debentialc.Main;
import org.debentialc.claims.models.Terrain;
import org.debentialc.claims.storage.TerrainStorage;
import org.debentialc.service.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class TerrainManager {

    private static final int BUFFER_BLOCKS = 16;

    private static TerrainManager instance;

    private final Map<String, Terrain> terrains = new HashMap<String, Terrain>();
    private final TerrainStorage storage;
    @Setter
    @Getter
    private Economy economy;

    private TerrainManager() {
        storage = new TerrainStorage();
        terrains.putAll(storage.loadAll());
        setupEconomy();
    }

    public static TerrainManager getInstance() {
        if (instance == null) instance = new TerrainManager();
        return instance;
    }

    public boolean createTerrain(String id, int chunks) {
        if (terrains.containsKey(id)) return false;
        Terrain terrain = new Terrain(id, chunks);
        terrains.put(id, terrain);
        storage.saveTerrain(terrain);
        return true;
    }

    public boolean setPrice(String id, double price) {
        Terrain terrain = terrains.get(id);
        if (terrain == null) return false;
        terrain.setPrice(price);
        storage.saveTerrain(terrain);
        return true;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return false;

        economy = rsp.getProvider();
        return economy != null;
    }
    public Terrain getTerrain(String id) {
        return terrains.get(id);
    }

    public Terrain getTerrainAt(Location loc) {
        for (Terrain terrain : terrains.values()) {
            if (terrain.isCommitted() && terrain.isInsideTerrain(loc)) return terrain;
        }
        return null;
    }

    public Terrain getTerrainBySign(Location loc) {
        for (Terrain terrain : terrains.values()) {
            if (!terrain.isCommitted() || terrain.getSignLocation() == null) continue;
            Location signLoc = terrain.getSignLocation();
            if (signLoc.getWorld().getName().equals(loc.getWorld().getName())
                    && signLoc.getBlockX() == loc.getBlockX()
                    && signLoc.getBlockY() == loc.getBlockY()
                    && signLoc.getBlockZ() == loc.getBlockZ()) {
                return terrain;
            }
        }
        return null;
    }

    public String checkCollision(String excludeId, World world, int ox, int oz, int size) {
        int ax1 = ox - BUFFER_BLOCKS;
        int az1 = oz - BUFFER_BLOCKS;
        int ax2 = ox + size + BUFFER_BLOCKS;
        int az2 = oz + size + BUFFER_BLOCKS;

        for (Terrain other : terrains.values()) {
            if (!other.isCommitted()) continue;
            if (excludeId != null && other.getId().equals(excludeId)) continue;
            if (other.getOrigin() == null) continue;
            if (!other.getOrigin().getWorld().getName().equals(world.getName())) continue;

            int bx1 = other.getOrigin().getBlockX();
            int bz1 = other.getOrigin().getBlockZ();
            int bx2 = bx1 + other.getSizeInBlocks();
            int bz2 = bz1 + other.getSizeInBlocks();

            boolean overlapX = ax1 < bx2 && ax2 > bx1;
            boolean overlapZ = az1 < bz2 && az2 > bz1;

            if (overlapX && overlapZ) return other.getId();
        }
        return null;
    }

    public boolean commitTerrain(String id, Player player) {
        Terrain terrain = terrains.get(id);
        if (terrain == null) return false;
        if (terrain.isCommitted()) return false;

        Location center = player.getLocation();
        int chunkX = center.getChunk().getX() * 16;
        int chunkZ = center.getChunk().getZ() * 16;
        int y = center.getBlockY();
        World world = center.getWorld();

        String collision = checkCollision(id, world, chunkX, chunkZ, terrain.getSizeInBlocks());
        if (collision != null) return false;

        Location origin = new Location(world, chunkX, y, chunkZ);
        terrain.setOrigin(origin);
        terrain.setCommitted(true);

        buildBorders(terrain, world, chunkX, chunkZ, y);
        placeSign(terrain, world, chunkX, chunkZ, y);

        storage.saveTerrain(terrain);
        return true;
    }

    public String getCollisionId(String id, Player player, Terrain terrain) {
        Location center = player.getLocation();
        int chunkX = center.getChunk().getX() * 16;
        int chunkZ = center.getChunk().getZ() * 16;
        return checkCollision(id, center.getWorld(), chunkX, chunkZ, terrain.getSizeInBlocks());
    }

    private void buildBorders(Terrain terrain, World world, int ox, int oz, int y) {
        int size = terrain.getSizeInBlocks();
        int slabId = 44;
        byte slabData = 0;

        for (int x = ox; x < ox + size; x++) {
            setBlock(world, x, y, oz, slabId, slabData);
            setBlock(world, x, y, oz + size, slabId, slabData);
        }
        for (int z = oz; z < oz + size + 1; z++) {
            setBlock(world, ox, y, z, slabId, slabData);
            setBlock(world, ox + size, y, z, slabId, slabData);
        }
    }

    private void removeBorders(Terrain terrain) {
        if (terrain.getOrigin() == null) return;
        World world = terrain.getOrigin().getWorld();
        int ox = terrain.getOrigin().getBlockX();
        int oz = terrain.getOrigin().getBlockZ();
        int y = terrain.getOrigin().getBlockY();
        int size = terrain.getSizeInBlocks();

        for (int x = ox; x < ox + size; x++) {
            clearBlock(world, x, y, oz);
            clearBlock(world, x, y, oz + size);
        }
        for (int z = oz; z < oz + size + 1; z++) {
            clearBlock(world, ox, y, z);
            clearBlock(world, ox + size, y, z);
        }
    }

    private void clearBlock(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        int id = block.getTypeId();
        if (id == 44 || id == 63 || id == 68) {
            block.setTypeId(0, false);
        }
    }

    private void setBlock(World world, int x, int y, int z, int id, byte data) {
        Block block = world.getBlockAt(x, y, z);
        block.setTypeIdAndData(id, data, false);
    }

    private void placeSign(Terrain terrain, World world, int ox, int oz, int y) {
        Block signBlock = world.getBlockAt(ox, y, oz);
        signBlock.setTypeIdAndData(63, (byte) 0, false);

        if (signBlock.getState() instanceof Sign) {
            Sign sign = (Sign) signBlock.getState();
            updateSign(sign, terrain);
            sign.update(true);
        }
        terrain.setOrigin(new Location(world, ox, y, oz));
        storage.saveTerrain(terrain);
    }

    public void updateSign(Terrain terrain) {
        if (!terrain.isCommitted() || terrain.getOrigin() == null) return;
        Location loc = terrain.getSignLocation();
        Block block = loc.getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            updateSign(sign, terrain);
            sign.update(true);
        }
    }

    private void updateSign(Sign sign, Terrain terrain) {
        sign.setLine(0, CC.translate("&8[ Terreno ]"));
        sign.setLine(1, CC.translate("&7" + terrain.getId()));
        if (terrain.hasOwner()) {
            sign.setLine(2, CC.translate("&f" + terrain.getOwnerName()));
            sign.setLine(3, CC.translate("&8Propietario"));
        } else {
            sign.setLine(2, CC.translate("&aEn venta"));
            sign.setLine(3, CC.translate("&7$" + (int) terrain.getPrice()));
        }
    }

    public boolean deleteTerrain(String id) {
        Terrain terrain = terrains.remove(id);
        if (terrain == null) return false;
        if (terrain.isCommitted()) {
            removeBorders(terrain);
            removeSign(terrain);
        }
        storage.deleteTerrain(id);
        return true;
    }

    public boolean dissolveTerrain(String id) {
        Terrain terrain = terrains.get(id);
        if (terrain == null) return false;
        if (!terrain.hasOwner()) return false;
        terrain.setOwner(null);
        terrain.setOwnerName(null);
        terrain.getMembers().clear();
        storage.saveTerrain(terrain);
        updateSign(terrain);
        return true;
    }

    private void removeSign(Terrain terrain) {
        if (terrain.getSignLocation() == null) return;
        Block block = terrain.getSignLocation().getBlock();
        int id = block.getTypeId();
        if (id == 63 || id == 68) {
            block.setTypeId(0, false);
        }
    }

    public boolean purchaseTerrain(Terrain terrain, Player player) {
        if (terrain.hasOwner()) return false;
        if (economy == null) return false;
        if (!economy.has(player.getName(), terrain.getPrice())) return false;

        economy.withdrawPlayer(player.getName(), terrain.getPrice());
        terrain.setOwner(player.getUniqueId());
        terrain.setOwnerName(player.getName());
        storage.saveTerrain(terrain);
        updateSign(terrain);
        return true;
    }

    public void transferOwner(Terrain terrain, UUID newOwner, String newOwnerName) {
        terrain.setOwner(newOwner);
        terrain.setOwnerName(newOwnerName);
        storage.saveTerrain(terrain);
        updateSign(terrain);
    }

    public boolean canInteract(Terrain terrain, Player player) {
        if (terrain == null) return true;
        if (player.hasPermission(ClaimsPermissions.ADMIN_BYPASS)) return true;
        if (terrain.isOwner(player.getUniqueId())) return true;
        return false;
    }

    public boolean canBuild(Terrain terrain, Player player) {
        if (terrain == null) return true;
        if (player.hasPermission(ClaimsPermissions.ADMIN_BYPASS)) return true;
        if (terrain.isOwner(player.getUniqueId())) return true;
        return terrain.hasMemberRole(player.getUniqueId(), Terrain.MemberRole.BUILD);
    }

    public boolean canBreak(Terrain terrain, Player player) {
        if (terrain == null) return true;
        if (player.hasPermission(ClaimsPermissions.ADMIN_BYPASS)) return true;
        if (terrain.isOwner(player.getUniqueId())) return true;
        return terrain.hasMemberRole(player.getUniqueId(), Terrain.MemberRole.BREAK);
    }

    public boolean canOpenContainers(Terrain terrain, Player player) {
        if (terrain == null) return true;
        if (player.hasPermission(ClaimsPermissions.ADMIN_BYPASS)) return true;
        if (terrain.isOwner(player.getUniqueId())) return true;
        return terrain.hasMemberRole(player.getUniqueId(), Terrain.MemberRole.CONTAINERS);
    }

    public void addMember(Terrain terrain, UUID uuid, String name, Terrain.MemberRole role) {
        terrain.addMemberRole(uuid, name, role);
        storage.saveTerrain(terrain);
    }

    public void removeMember(Terrain terrain, UUID uuid) {
        terrain.removeMember(uuid);
        storage.saveTerrain(terrain);
    }

    public void save(Terrain terrain) {
        storage.saveTerrain(terrain);
    }

    public Map<String, Terrain> getAll() {
        return terrains;
    }

    public int getBuffer() {
        return BUFFER_BLOCKS;
    }
}