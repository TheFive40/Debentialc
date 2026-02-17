package org.debentialc.claims.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

public class TerrainProtectionListener implements Listener {

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