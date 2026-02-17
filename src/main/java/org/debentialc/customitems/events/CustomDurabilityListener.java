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

    // ── Armor takes damage when the player is hit ───────────────────────────
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
            if (CustomDurabilityManager.isUnbreakable(armor)) continue;

            boolean broken = CustomDurabilityManager.damageItem(armor, 1);
            changed = true;

            if (broken) {
                armorContents[i] = new ItemStack(Material.AIR);
                player.sendMessage(CC.translate("&c¡Tu armadura se ha roto!"));
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
            }
            // Whether broken or not we must write the array back (meta was
            // mutated in-place by damageItem / setCustomDurability).
        }

        if (changed) {
            player.getInventory().setArmorContents(armorContents);
            player.updateInventory();
        }
    }

    // ── Weapon / tool loses a use when the player hits something ────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        // IMPORTANT: getItemInHand() returns a copy in 1.7.10.
        // We must mutate that copy and then setItemInHand so the server
        // sees the change.
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;
        if (CustomDurabilityManager.isUnbreakable(item)) return;

        boolean broken = CustomDurabilityManager.damageItem(item, 1);

        if (broken) {
            player.setItemInHand(new ItemStack(Material.AIR));
            player.sendMessage(CC.translate("&c¡Tu item se ha roto!"));
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
        } else {
            // Write the mutated copy back so the server persists the lore change
            // and the visual durability bar.
            player.setItemInHand(item);
        }
        player.updateInventory();
    }

    // ── Mining costs a use ──────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (item == null || item.getTypeId() == 0) return;
        if (!CustomDurabilityManager.hasCustomDurability(item)) return;
        if (CustomDurabilityManager.isUnbreakable(item)) return;

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