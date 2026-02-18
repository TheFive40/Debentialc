package org.debentialc.claims.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

public class LeaseProtectionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(ClaimsPermissions.ADMIN_BYPASS)) return;

        Block block = event.getBlock();
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(block.getLocation());
        if (terrain == null) return;

        if (!LeaseManager.getInstance().isSubTerrain(terrain.getId())) return;

        if (!LeaseManager.getInstance().ownerCanInteractInSubTerrain(player.getUniqueId(), terrain.getId())) {
            event.setCancelled(true);
            player.sendMessage(CC.translate("&cNo puedes romper bloques en el terreno de tu inquilino."));
            return;
        }

        LeaseContract contract = LeaseManager.getInstance().getContractBySubTerrain(terrain.getId());
        if (contract == null) return;

        if (contract.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD
                || contract.getStatus() == LeaseContract.ContractStatus.EXPIRED) {
            if (!player.getUniqueId().equals(contract.getTenantId())) {
                event.setCancelled(true);
                player.sendMessage(CC.translate("&7No tienes permisos en este terreno."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(ClaimsPermissions.ADMIN_BYPASS)) return;

        Block block = event.getBlock();
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(block.getLocation());
        if (terrain == null) return;

        if (!LeaseManager.getInstance().isSubTerrain(terrain.getId())) return;

        if (!LeaseManager.getInstance().ownerCanInteractInSubTerrain(player.getUniqueId(), terrain.getId())) {
            event.setCancelled(true);
            player.sendMessage(CC.translate("&cNo puedes construir en el terreno de tu inquilino."));
            return;
        }

        LeaseContract contract = LeaseManager.getInstance().getContractBySubTerrain(terrain.getId());
        if (contract == null) return;

        if (contract.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD
                || contract.getStatus() == LeaseContract.ContractStatus.EXPIRED) {
            if (!player.getUniqueId().equals(contract.getTenantId())) {
                event.setCancelled(true);
                player.sendMessage(CC.translate("&7No tienes permisos en este terreno."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(ClaimsPermissions.ADMIN_BYPASS)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Terrain terrain = TerrainManager.getInstance().getTerrainAt(block.getLocation());
        if (terrain == null) return;

        if (!LeaseManager.getInstance().isSubTerrain(terrain.getId())) return;

        if (!LeaseManager.getInstance().ownerCanInteractInSubTerrain(player.getUniqueId(), terrain.getId())) {
            event.setCancelled(true);
            player.sendMessage(CC.translate("&cNo puedes interactuar en el terreno de tu inquilino."));
        }
    }
}