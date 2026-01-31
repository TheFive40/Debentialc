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
 * VERSIÓN CORREGIDA: Valida correctamente si un item se debe consumir
 *
 * LÓGICA CORREGIDA:
 * 1. Un item se consume si: isConsumable = true O tpValue > 0
 * 2. Al consumir, SIEMPRE ejecuta comandos primero (si los tiene)
 * 3. Luego, si tiene TP, otorga los TPs
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

        // LÓGICA CORREGIDA: Un item se consume si:
        // 1. Es consumible (isConsumable = true)
        // 2. O tiene TP configurado (tpValue > 0)
        // 3. O tiene comandos configurados (commands no está vacío)
        boolean shouldConsume = customItem.isConsumable() ||
                customItem.getTpValue() > 0 ||
                (customItem.getCommands() != null && !customItem.getCommands().isEmpty());

        if (!shouldConsume) {
            // Si no cumple ninguna condición de consumo, no hacer nada
            return;
        }

        // CANCELAR el evento para que no se use normalmente
        event.setCancelled(true);

        // PASO 1: EJECUTAR COMANDOS (si los tiene)
        if (customItem.getCommands() != null && !customItem.getCommands().isEmpty()) {
            executeCommands(player, customItem.getCommands());

            // Mensaje al jugador informando que se ejecutaron comandos
            player.sendMessage(CC.translate("&b⚡ &fComandos ejecutados"));
        }

        // PASO 2: OTORGAR TP (si tiene)
        if (customItem.getTpValue() > 0) {
            // Obtener cantidad a consumir según configuración
            int amount = item.getAmount();
            int consumed = 1; // Por defecto consume 1

            // Si tiene TP y consume stack, consumir todo
            if (customItem.isTpConsumeStack()) {
                consumed = amount;
            }

            giveTP(player, customItem.getTpValue(), consumed);

            // Actualizar la cantidad después de dar TP
            amount = amount - consumed;

            // CONSUMIR ITEM
            if (amount <= 0) {
                // Eliminar completamente
                player.setItemInHand(null);
            } else {
                // Reducir cantidad
                item.setAmount(amount);
            }
        }
        // PASO 3: CONSUMIR ITEM (si es consumible pero NO tiene TP)
        else if (customItem.isConsumable()) {
            int amount = item.getAmount();
            int remaining = amount - 1;

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
     * Ejecuta los comandos configurados en orden
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
        return dot > 0.95;
    }
}