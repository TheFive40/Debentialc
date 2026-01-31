package org.example.events;

import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.example.commands.items.CustomItemCommand;
import org.example.tools.CC;
import org.example.tools.General;
import org.example.tools.ci.CustomItem;

import java.util.List;

/**
 * Listener para items consumibles
 * Maneja el consumo de items y la ejecución de comandos/TP
 */
public class ConsumableItemListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
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

        // Verificar si es consumible O tiene TP configurado
        if (!customItem.isConsumable() && customItem.getTpValue() <= 0) {
            return;
        }

        // CANCELAR el evento para que no se use normalmente
        event.setCancelled(true);

        // Obtener cantidad a consumir
        int amount = item.getAmount();
        int consumed = 1; // Por defecto consume 1

        // Si tiene TP y consume stack, consumir todo
        if (customItem.getTpValue() > 0 && customItem.isTpConsumeStack()) {
            consumed = amount;
        }

        // EJECUTAR COMANDOS (si tiene)
        if (customItem.getCommands() != null && !customItem.getCommands().isEmpty()) {
            executeCommands(player, customItem.getCommands());
        }

        // OTORGAR TP (si tiene)
        if (customItem.getTpValue() > 0) {
            giveTP(player, customItem.getTpValue(), consumed);
        }

        // CONSUMIR ITEM
        if (customItem.isConsumable() || customItem.getTpValue() > 0) {
            int remaining = amount - consumed;

            if (remaining <= 0) {
                // Eliminar completamente
                player.setItemInHand(null);
            } else {
                // Reducir cantidad
                item.setAmount(remaining);
            }
        }
    }

    /**
     * Ejecuta los comandos configurados
     */
    private void executeCommands(Player player, List<String> commands) {
        // Obtener jugador al que apunta
        Player targetPlayer = getTargetPlayer(player);

        for (String cmd : commands) {
            // Reemplazar placeholders
            String finalCmd = cmd;

            // @dp = Jugador que usa el item
            finalCmd = finalCmd.replace("@dp", player.getName());

            // @p = Jugador al que apunta
            if (targetPlayer != null) {
                finalCmd = finalCmd.replace("@p", targetPlayer.getName());
            } else {
                finalCmd = finalCmd.replace("@p", player.getName()); // Por defecto, a sí mismo
            }

            // Ejecutar comando como consola
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            } catch (Exception e) {
                player.sendMessage(CC.translate("&c✗ Error ejecutando comando: " + finalCmd));
                e.printStackTrace();
            }
        }
    }

    /**
     * Otorga TP al jugador
     */
    private void giveTP(Player player, int tpValue, int multiplier) {
        try {
            IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());

            int totalTP = tpValue * multiplier;
            int currentTP = idbcPlayer.getTP();
            int newTP = currentTP + totalTP;

            idbcPlayer.setTP(newTP);

            // Mensaje al jugador
            player.sendMessage("");
            player.sendMessage(CC.translate("&b⚡ &f¡Has obtenido TP!"));
            if (multiplier > 1) {
                player.sendMessage(CC.translate("&7  " + tpValue + " TP &fx " + multiplier + " items &7= &b" + totalTP + " TP"));
            } else {
                player.sendMessage(CC.translate("&7  +" + totalTP + " TP"));
            }
            player.sendMessage(CC.translate("&7  Total: &f" + newTP + " TP"));
            player.sendMessage("");

        } catch (Exception e) {
            player.sendMessage(CC.translate("&c✗ Error al otorgar TP"));
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el jugador al que está apuntando
     */
    private Player getTargetPlayer(Player player) {
        try {
            BlockIterator iterator = new BlockIterator(player, 10);

            while (iterator.hasNext()) {
                iterator.next();

                // Buscar jugadores cercanos
                for (Player target : player.getWorld().getPlayers()) {
                    if (target.equals(player)) continue;

                    if (target.getLocation().distance(player.getLocation()) <= 10) {
                        // Verificar si está en la línea de visión
                        if (isLookingAt(player, target)) {
                            return target;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar errores
        }

        return null;
    }

    /**
     * Verifica si el jugador está mirando a otro jugador
     */
    private boolean isLookingAt(Player player, Player target) {
        org.bukkit.util.Vector playerDirection = player.getLocation().getDirection().normalize();
        org.bukkit.util.Vector toTarget = target.getLocation().toVector()
                .subtract(player.getLocation().toVector()).normalize();

        double dot = playerDirection.dot(toTarget);
        return dot > 0.95; // Ángulo muy cerrado
    }
}