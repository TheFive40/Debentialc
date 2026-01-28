package org.example.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.durability.CustomDurabilityManager;

/**
 * Listener para interceptar eventos de daño vanilla y aplicar durabilidad custom
 */
public class CustomDurabilityListener implements Listener {

    /**
     * Previene el daño vanilla a items con durabilidad custom
     * Este es el evento principal que captura TODO el daño de items
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() == Material.AIR) return;

        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

        // Cancelamos el daño vanilla
        //event.setCancelled(true);

        // Aplicamos daño custom
        boolean broken = CustomDurabilityManager.damageItem(item, 1);

        if (broken) {
            player.setItemInHand(new ItemStack(Material.AIR));
            player.sendMessage(CC.translate("&c¡Tu item se ha roto!"));
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
        } else {
            CustomDurabilityManager.updateDurabilityLore(item);
        }
    }


    /**
     * Manejo adicional para minería de bloques
     * Asegura que las herramientas con durabilidad custom reciban daño
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getTypeId() == 0) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

        // El daño ya fue aplicado por PlayerItemDamageEvent
        // Solo actualizamos el lore si es necesario
        CustomDurabilityManager.updateDurabilityLore(item);
    }

    /**
     * Actualiza el lore al atacar entidades
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getTypeId() == 0) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

        // El daño ya fue aplicado por PlayerItemDamageEvent
        // Solo actualizamos el lore
        CustomDurabilityManager.updateDurabilityLore(item);
    }

    /**
     * Actualiza el lore cuando el jugador interactúa (opcional)
     * Útil para herramientas como azadas, tijeras, etc.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null || item.getTypeId() == 0) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

        // El daño ya fue aplicado por PlayerItemDamageEvent si corresponde
        // Solo actualizamos el lore
        CustomDurabilityManager.updateDurabilityLore(item);
    }
}