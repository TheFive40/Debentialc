package org.debentialc.boosters.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Material;
import org.debentialc.boosters.commands.BoosterConfigMenu;
import org.debentialc.boosters.core.BoosterSettings;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.boosters.models.PersonalBooster;
import org.debentialc.boosters.storage.BoosterStorage;

import java.util.List;

public class BoosterListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<PersonalBooster> boosters = BoosterStorage.loadPersonalBoosters(player.getUniqueId());
        for (PersonalBooster booster : boosters) {
            PersonalBoosterManager.addBooster(booster);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());
        if (!boosters.isEmpty()) {
            BoosterStorage.savePersonalBoosters(player.getUniqueId(), boosters);
        }
        PersonalBoosterManager.removeExpiredBoosters(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = view.getTitle();

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!title.contains("§6")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        Material material = event.getCurrentItem().getType();

        if (title.contains("Booster Configuration")) {
            handleMainMenu(player, event.getSlot());
        } else if (title.contains("Global Booster")) {
            handleGlobalMenu(player, event.getSlot());
        } else if (title.contains("Personal Booster")) {
            handlePersonalMenu(player, event.getSlot());
        } else if (title.contains("Rank Multipliers")) {
            handleRankMenu(player, event.getSlot());
        } else if (title.contains("Durations")) {
            handleDurationMenu(player, event.getSlot());
        }
    }

    private void handleMainMenu(Player player, int slot) {
        if (slot == 11) {
            player.openInventory(BoosterConfigMenu.createGlobalBoosterMenu());
        } else if (slot == 13) {
            player.openInventory(BoosterConfigMenu.createPersonalBoosterMenu());
        } else if (slot == 15) {
            player.openInventory(BoosterConfigMenu.createRankMultiplierMenu());
        } else if (slot == 22) {
            player.openInventory(BoosterConfigMenu.createDurationMenu());
        }
    }

    private void handleGlobalMenu(Player player, int slot) {
        if (slot == 26) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }

    private void handlePersonalMenu(Player player, int slot) {
        if (slot == 26) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }

    private void handleRankMenu(Player player, int slot) {
        if (slot == 35) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }

    private void handleDurationMenu(Player player, int slot) {
        if (slot == 26) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }
}