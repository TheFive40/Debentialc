package org.debentialc.customitems.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.debentialc.service.CC;
import org.debentialc.customitems.tools.fragments.ArmorFragment;
import org.debentialc.customitems.tools.fragments.FragmentManager;

/**
 * Listener para manejar la aplicación de fragmentos a armaduras
 * El jugador hace clic derecho con el fragmento y se aplica a su armadura equipada
 */
public class FragmentApplyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFragmentUse(PlayerInteractEvent event) {
        // Solo clic derecho (en aire o en bloque)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();

        // Verificar si es un fragmento
        if (!ArmorFragment.isFragment(itemInHand)) {
            return;
        }

        // Cancelar el evento para evitar interacción con bloques
        event.setCancelled(true);

        // Buscar pieza de armadura equipada (prioridad de arriba hacia abajo)
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack targetArmor = null;
        int targetSlot = -1;

        // Buscar primera pieza equipada (helmet -> chest -> legs -> boots)
        for (int i = 3; i >= 0; i--) {
            if (armorContents[i] != null && armorContents[i].getType() != Material.AIR) {
                targetArmor = armorContents[i];
                targetSlot = i;
                break;
            }
        }

        if (targetArmor == null) {
            player.sendMessage(CC.translate("&c✗ No tienes ninguna armadura equipada"));
            player.sendMessage(CC.translate("&7Equipa al menos una pieza de armadura"));
            return;
        }

        // Aplicar el fragmento
        FragmentManager manager = FragmentManager.getInstance();
        boolean applied = manager.applyFragment(player, itemInHand, targetArmor);

        if (applied) {
            // IMPORTANTE: Actualizar la armadura en el slot específico
            armorContents[targetSlot] = targetArmor;
            player.getInventory().setArmorContents(armorContents);
            player.updateInventory();
        }
    }
}