package org.debentialc.claims.events;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

public class TerrainSignListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        String line0 = org.bukkit.ChatColor.stripColor(sign.getLine(0));
        if (!line0.equals("[ Terreno ]")) return;

        event.setCancelled(true);

        Terrain terrain = TerrainManager.getInstance().getTerrainBySign(block.getLocation());
        if (terrain == null) return;

        Player player = event.getPlayer();

        if (terrain.hasOwner()) {
            if (terrain.isOwner(player.getUniqueId())) {
                sendOwnerInfo(player, terrain);
            } else {
                player.sendMessage(CC.translate("&7Terreno &f" + terrain.getId() + " &7— Propietario: &f" + terrain.getOwnerName()));
            }
            return;
        }

        if (terrain.getPrice() <= 0) {
            player.sendMessage(CC.translate("&7Este terreno no tiene precio asignado."));
            return;
        }

        if (TerrainManager.getInstance().getEconomy() == null) {
            player.sendMessage(CC.translate("&7El sistema de economía no está disponible."));
            return;
        }

        if (!TerrainManager.getInstance().getEconomy().has(player.getName(), terrain.getPrice())) {
            player.sendMessage(CC.translate("&7No tienes suficiente dinero. Precio: &f$" + (int) terrain.getPrice() + "&7."));
            return;
        }

        boolean purchased = TerrainManager.getInstance().purchaseTerrain(terrain, player);
        if (purchased) {
            player.sendMessage(CC.translate("&7Compraste el terreno &f" + terrain.getId() + " &7por &f$" + (int) terrain.getPrice() + "&7."));
        } else {
            player.sendMessage(CC.translate("&7No se pudo completar la compra."));
        }
    }

    private void sendOwnerInfo(Player player, Terrain terrain) {
        player.sendMessage(CC.translate("&8&m                    "));
        player.sendMessage(CC.translate("  &7Terreno &f" + terrain.getId()));
        player.sendMessage(CC.translate("  &7Eres el propietario."));
        player.sendMessage(CC.translate("  &7Usa &f/terrain <subcomando> &7para gestionarlo."));
        player.sendMessage(CC.translate("&8&m                    "));
    }
}