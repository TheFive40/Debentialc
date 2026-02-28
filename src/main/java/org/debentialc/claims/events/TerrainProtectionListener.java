package org.debentialc.claims.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.TerrainCustomizeManager;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

public class TerrainProtectionListener implements Listener {

    // ─────────────────────────────────────────────────────────────────
    //  Bloques
    // ─────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(block.getLocation());
        if (terrain == null) return;

        if (!TerrainManager.getInstance().canBreak(terrain, player)) {
            event.setCancelled(true);
            player.sendMessage(CC.translate("&7No tienes permiso para romper bloques aquí."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(block.getLocation());
        if (terrain == null) return;

        if (!TerrainManager.getInstance().canBuild(terrain, player)) {
            event.setCancelled(true);
            player.sendMessage(CC.translate("&7No tienes permiso para construir aquí."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        Terrain terrain = TerrainManager.getInstance().getTerrainAt(block.getLocation());
        if (terrain == null) return;

        if (isContainer(block)) {
            if (!TerrainManager.getInstance().canOpenContainers(terrain, player)) {
                event.setCancelled(true);
                player.sendMessage(CC.translate("&7No tienes permiso para abrir contenedores aquí."));
            }
            return;
        }

        if (isInteractable(block)) {
            if (!TerrainManager.getInstance().canInteract(terrain, player)) {
                event.setCancelled(true);
                player.sendMessage(CC.translate("&7No tienes permiso para interactuar aquí."));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  PvP (regla personalizable)
    // ─────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player victim  = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        Terrain terrain = TerrainManager.getInstance().getTerrainAt(victim.getLocation());
        if (terrain == null) return;

        if (!TerrainCustomizeManager.isPvpEnabled(terrain.getId())) {
            event.setCancelled(true);
            attacker.sendMessage(CC.translate("&7El PvP está &cdisabled &7en este terreno."));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Spawn de mobs (regla personalizable)
    // ─────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(event.getLocation());
        if (terrain == null) return;

        // Solo aplica a mobs (no animales ni spawns artificiales)
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CHUNK_GEN) {
            if (!TerrainCustomizeManager.isMobSpawningEnabled(terrain.getId())) {
                event.setCancelled(true);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Explosiones TNT (regla personalizable)
    // ─────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosion(EntityExplodeEvent event) {
        if (event.blockList().isEmpty()) return;

        // Revisamos si la explosión está en un terreno con TNT deshabilitado
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(event.getLocation());
        if (terrain == null) return;

        if (!TerrainCustomizeManager.isRuleEnabled(terrain.getId(), "tntExplodes")) {
            event.blockList().clear(); // Sin daño de bloques
        }
    }

    private boolean isContainer(Block block) {
        int id = block.getTypeId();
        return id == 54 || id == 27 || id == 28 || id == 61 || id == 62
                || id == 23 || id == 158 || id == 154 || id == 84;
    }

    private boolean isInteractable(Block block) {
        int id = block.getTypeId();
        return id == 64 || id == 71 || id == 193 || id == 194 || id == 195 || id == 196 || id == 197
                || id == 96 || id == 167 || id == 107 || id == 69 || id == 77 || id == 143
                || id == 72 || id == 147 || id == 148 || id == 25 || id == 116 || id == 379
                || id == 145 || id == 151 || id == 178;
    }
}