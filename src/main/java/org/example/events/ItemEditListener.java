package org.example.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.example.tools.inventory.ItemEditManager;
import org.example.tools.inventory.ArmorEditManager;
import org.example.tools.inventory.EffectInputManager;
import org.example.tools.inventory.BonusInputManager;
import org.example.tools.inventory.DurabilityInputManager;

public class ItemEditListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();

        if (ItemEditManager.isEditingItem(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ItemEditManager.cancelItemEdit(player);
            } else {
                ItemEditManager.processItemEdit(player, message);
            }
            return;
        }

        if (ArmorEditManager.isEditingArmor(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                ArmorEditManager.cancelArmorEdit(player);
            } else {
                ArmorEditManager.processArmorEdit(player, message);
            }
            return;
        }

        if (BonusInputManager.isInputtingBonus(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                BonusInputManager.cancelBonusInput(player);
            } else {
                BonusInputManager.processBonusInput(player, message);
            }
            return;
        }

        if (EffectInputManager.isInputtingEffect(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                EffectInputManager.cancelEffectInput(player);
            } else {
                EffectInputManager.processEffectInput(player, message);
            }
            return;
        }

        if (DurabilityInputManager.isInputtingDurability(player)) {
            event.setCancelled(true);

            if (message.equalsIgnoreCase("cancelar")) {
                DurabilityInputManager.cancelDurabilityInput(player);
            } else {
                DurabilityInputManager.processDurabilityInput(player, message);
            }
            return;
        }
    }
}