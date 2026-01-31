package org.example.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.example.tools.inventory.*;

/**
 * Listener para manejar todos los inputs de chat relacionados con items
 */
public class ItemEditListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();

        // Item Edit Manager (Renombrar, etc)
        if (ItemEditManager.isEditingItem(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ItemEditManager.cancelItemEdit(player);
            } else {
                ItemEditManager.processItemEdit(player, message);
            }
            return;
        }

        // Armor Edit Manager
        if (ArmorEditManager.isEditingArmor(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ArmorEditManager.cancelArmorEdit(player);
            } else {
                ArmorEditManager.processArmorEdit(player, message);
            }
            return;
        }

        // Bonus Input Manager
        if (BonusInputManager.isInputtingBonus(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                BonusInputManager.cancelBonusInput(player);
            } else {
                BonusInputManager.processBonusInput(player, message);
            }
            return;
        }

        // Effect Input Manager
        if (EffectInputManager.isInputtingEffect(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                EffectInputManager.cancelEffectInput(player);
            } else {
                EffectInputManager.processEffectInput(player, message);
            }
            return;
        }

        // Durability Input Manager
        if (DurabilityInputManager.isInputtingDurability(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                DurabilityInputManager.cancelDurabilityInput(player);
            } else {
                DurabilityInputManager.processDurabilityInput(player, message);
            }
            return;
        }

        // NUEVO: Command Input Manager
        if (ItemCommandInputManager.isInputtingCommand(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ItemCommandInputManager.cancelCommandInput(player);
            } else {
                ItemCommandInputManager.processCommandInput(player, message);
            }
            return;
        }

        // NUEVO: TP Input Manager
        if (ItemTPInputManager.isInputtingTPValue(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ItemTPInputManager.cancelTPValueInput(player);
            } else {
                ItemTPInputManager.processTPValueInput(player, message);
            }
            return;
        }
    }
}