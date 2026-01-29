package org.example.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.example.tools.fragments.CustomizedArmor;
import org.example.tools.fragments.FragmentBonusIntegration;

/**
 * Listener para manejar la remoción de bonus cuando se quita armadura con fragmentos
 */
public class FragmentArmorEquipListener implements Listener {

    /**
     * Detecta cuando un jugador quita una armadura del slot de armadura
     * Los slots de armadura son: 36 (boots), 37 (legs), 38 (chest), 39 (helmet)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorRemove(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Solo nos interesa cuando tocan los slots de armadura (36-39)
        if (slot < 36 || slot > 39) return;

        ItemStack clickedItem = event.getCurrentItem();

        // Solo nos interesa cuando hay una armadura con fragmentos en el slot
        if (clickedItem == null || clickedItem.getTypeId() == 0) return;
        if (!CustomizedArmor.isCustomized(clickedItem)) return;

        String hash = CustomizedArmor.getHash(clickedItem);
        if (hash == null) return;

        // Remover bonus después de un tick (para dar tiempo a que se actualice el inventario)
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(
                org.example.Main.instance,
                () -> {
                    // Verificar si la armadura sigue equipada
                    ItemStack[] armorContents = player.getInventory().getArmorContents();
                    boolean stillEquipped = false;

                    for (ItemStack armor : armorContents) {
                        if (armor != null && CustomizedArmor.isCustomized(armor)) {
                            String currentHash = CustomizedArmor.getHash(armor);
                            if (hash.equals(currentHash)) {
                                stillEquipped = true;
                                break;
                            }
                        }
                    }

                    // Si ya no está equipada, remover bonus
                    if (!stillEquipped) {
                        FragmentBonusIntegration.removeFragmentBonuses(player, hash);
                    }
                },
                1L
        );
    }

    /**
     * Limpiar bonus cuando el jugador sale del servidor
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        // Remover todos los bonus de fragmentos
        for (ItemStack armor : armorContents) {
            if (armor == null || armor.getTypeId() == 0) continue;
            if (!CustomizedArmor.isCustomized(armor)) continue;

            String hash = CustomizedArmor.getHash(armor);
            if (hash != null) {
                FragmentBonusIntegration.removeFragmentBonuses(player, hash);
            }
        }
    }
}