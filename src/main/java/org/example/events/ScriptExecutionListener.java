package org.example.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.example.commands.items.CustomItemCommand;
import org.example.tools.ci.CustomItem;
import org.example.tools.scripts.ScriptContext;
import org.example.tools.scripts.ScriptManager;

/**
 * Listener que intercepta el uso de items y ejecuta sus scripts asociados
 */
public class ScriptExecutionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemUse(PlayerInteractEvent event) {
        // Solo clic derecho
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getTypeId() == 0) {
            return;
        }

        // Verificar si es un item custom
        CustomItemCommand itemCmd = new CustomItemCommand();
        CustomItem customItem = itemCmd.toItemCustom(item);

        if (customItem == null) {
            return;
        }

        String itemId = customItem.getId();

        // Verificar si tiene script asociado
        if (!ScriptManager.getInstance().hasScript(itemId)) {
            return;
        }

        // Crear contexto de ejecución
        ScriptContext context = new ScriptContext(player)
                .setItem(item)
                .setLocation(player.getLocation())
                .setWorld(player.getWorld())
                .setEventType("use");

        // Ejecutar script
        boolean executed = ScriptManager.getInstance().executeScript(itemId, context);

        if (executed) {
            // Opcional: Cancelar evento si el script se ejecutó
            // event.setCancelled(true);
        }
    }
}