package org.debentialc.boosters.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.Material;
import org.debentialc.boosters.commands.BoosterConfigMenu;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.boosters.models.PersonalBooster;
import org.debentialc.boosters.storage.BoosterStorage;

import java.util.List;

/**
 * Listener de eventos del sistema de boosters
 * VERSIÓN CORREGIDA: Mejor manejo de eventos y prioridades
 */
public class BoosterListener implements Listener {

    /**
     * Carga los boosters personales cuando un jugador entra
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            List<PersonalBooster> boosters = BoosterStorage.loadPersonalBoosters(player.getUniqueId());

            for (PersonalBooster booster : boosters) {
                PersonalBoosterManager.addBooster(booster);
            }

            if (!boosters.isEmpty()) {
                player.sendMessage("§7[Boosters] §aCargados " + boosters.size() + " booster(s) personal(es)");
            }

        } catch (Exception e) {
            player.sendMessage("§c[Boosters] Error al cargar tus boosters personales");
            e.printStackTrace();
        }
    }

    /**
     * Guarda los boosters personales cuando un jugador sale
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        try {
            List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());

            if (!boosters.isEmpty()) {
                BoosterStorage.savePersonalBoosters(player.getUniqueId(), boosters);
            }

            PersonalBoosterManager.removeExpiredBoosters(player.getUniqueId());

        } catch (Exception e) {
            System.err.println("[Boosters] Error al guardar boosters de " + player.getName());
            e.printStackTrace();
        }
    }

    /**
     * Maneja los clicks en los menús de configuración
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();
        String title = view.getTitle();

        if (!title.contains("§6")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

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

    /**
     * Maneja clicks en el menú principal
     */
    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 11:
                player.openInventory(BoosterConfigMenu.createGlobalBoosterMenu());
                break;

            case 13:
                player.openInventory(BoosterConfigMenu.createPersonalBoosterMenu());
                break;

            case 15:
                player.openInventory(BoosterConfigMenu.createRankMultiplierMenu());
                break;

            case 22:
                player.openInventory(BoosterConfigMenu.createDurationMenu());
                break;
        }
    }

    /**
     * Maneja clicks en el menú de booster global
     */
    private void handleGlobalMenu(Player player, int slot) {
        if (slot == 26) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }

    /**
     * Maneja clicks en el menú de boosters personales
     */
    private void handlePersonalMenu(Player player, int slot) {
        if (slot == 26) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }

    /**
     * Maneja clicks en el menú de multiplicadores de rango
     */
    private void handleRankMenu(Player player, int slot) {
        if (slot == 35) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }

    /**
     * Maneja clicks en el menú de duraciones
     */
    private void handleDurationMenu(Player player, int slot) {
        if (slot == 26) {
            player.openInventory(BoosterConfigMenu.createMainMenu());
        }
    }
}