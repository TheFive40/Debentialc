package org.debentialc.customitems.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.debentialc.service.CC;
import org.debentialc.customitems.tools.durability.CustomDurabilityManager;


public class CustomDurabilityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerReceiveDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armor = armorContents[i];

            if (armor == null || armor.getType() == Material.AIR) continue;
            if (CustomDurabilityManager.isUnbreakable(armor)) continue;
            if (!CustomDurabilityManager.hasCustomDurability(armor)) continue;

            boolean broken = CustomDurabilityManager.damageItem(armor, 1);

            if (broken) {
                armorContents[i] = new ItemStack(Material.AIR);
                player.getInventory().setArmorContents(armorContents);
                player.sendMessage(CC.translate("&c¡Tu armadura se ha roto!"));
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
            } else {
                CustomDurabilityManager.updateDurabilityLore(armor);
            }
        }
    }

    /**
     * Previene el daño vanilla a items con durabilidad custom (arma en mano)
     * cuando el jugador ataca a otra entidad.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;

        if (CustomDurabilityManager.isUnbreakable(item)) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

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
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getTypeId() == 0) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

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

        CustomDurabilityManager.updateDurabilityLore(item);
    }

    /**
     * Actualiza el lore cuando el jugador interactúa
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null || item.getTypeId() == 0) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

        CustomDurabilityManager.updateDurabilityLore(item);
    }
}