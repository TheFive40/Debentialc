package org.debentialc.customitems.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.debentialc.service.CC;
import org.debentialc.customitems.tools.durability.CustomDurabilityManager;

public class CustomDurabilityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerReceiveDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        boolean changed = false;

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armor = armorContents[i];
            if (armor == null || armor.getType() == Material.AIR) continue;
            if (!CustomDurabilityManager.hasCustomDurability(armor)) continue;

            boolean broken = CustomDurabilityManager.damageItem(armor, 1);
            changed = true;

            if (broken) {
                armorContents[i] = new ItemStack(Material.AIR);
                player.sendMessage(CC.translate("&c¡Tu armadura se ha roto!"));
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
            }
        }

        if (changed) {
            player.getInventory().setArmorContents(armorContents);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

        boolean broken = CustomDurabilityManager.damageItem(item, 1);

        if (broken) {
            player.setItemInHand(new ItemStack(Material.AIR));
            player.sendMessage(CC.translate("&c¡Tu item se ha roto!"));
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
        } else {
            player.setItemInHand(item);
        }
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (item == null || item.getTypeId() == 0) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;

        boolean broken = CustomDurabilityManager.damageItem(item, 1);
        if (broken) {
            player.setItemInHand(new ItemStack(Material.AIR));
            player.sendMessage(CC.translate("&c¡Tu item se ha roto!"));
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
        } else {
            player.setItemInHand(item);
        }
        player.updateInventory();
    }
}