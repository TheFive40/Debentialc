package org.debentialc.raids.menus;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RaidMenuManager - Gestor de estados de menús
 * Mantiene el estado de cada jugador mientras navega los menús
 */
public class RaidMenuManager {

    private static final Map<UUID, RaidMenuState> menuStates = new ConcurrentHashMap<>();

    /**
     * Obtiene o crea el estado de menú de un jugador
     */
    public static RaidMenuState getPlayerMenuState(Player player) {
        return menuStates.computeIfAbsent(player.getUniqueId(),
                uuid -> new RaidMenuState(uuid, player));
    }

    /**
     * Obtiene el estado de menú de un jugador por UUID
     */
    public static RaidMenuState getPlayerMenuState(UUID playerId) {
        return menuStates.get(playerId);
    }

    /**
     * Establece el menú actual de un jugador
     */
    public static void setCurrentMenu(Player player, MenuType menuType, MenuStep step) {
        RaidMenuState state = getPlayerMenuState(player);
        state.setCurrentMenu(menuType);
        state.setCurrentStep(step);
    }

    /**
     * Avanza al siguiente paso
     */
    public static void nextStep(Player player, MenuStep step) {
        RaidMenuState state = getPlayerMenuState(player);
        state.setCurrentStep(step);
    }

    /**
     * Vuelve al menú anterior
     */
    public static void backMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        state.reset();
        openMainMenu(player);
    }

    /**
     * Limpia el estado de un jugador
     */
    public static void clearPlayerState(Player player) {
        menuStates.remove(player.getUniqueId());
    }

    /**
     * Obtiene el total de jugadores en menús
     */
    public static int getTotalPlayersInMenus() {
        return menuStates.size();
    }

    /**
     * Limpia todos los estados
     */
    public static void clearAllStates() {
        menuStates.clear();
    }

    // ====== MÉTODOS DE NAVEGACIÓN ======

    public static void openMainMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        state.setCurrentMenu(MenuType.MAIN);
        state.setCurrentStep(MenuStep.SELECT_ACTION);
        RaidMenus.sendMainMenu(player);
    }

    public static void openRaidListMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        state.setCurrentMenu(MenuType.RAID_LIST);
        state.setCurrentStep(MenuStep.SELECT_RAID);
        RaidMenus.sendRaidListMenu(player);
    }

    public static void openCreateRaidMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        state.setCurrentMenu(MenuType.CREATE_RAID);
        state.setCurrentStep(MenuStep.INPUT_NAME);
        state.clearTempData();
        RaidMenus.sendCreateRaidMenu(player);
    }

    public static void openRaidConfigMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        if (state.getCurrentRaid() == null) {
            openMainMenu(player);
            return;
        }
        state.setCurrentMenu(MenuType.RAID_CONFIG);
        state.setCurrentStep(MenuStep.SELECT_ACTION);
        RaidMenus.sendRaidConfigMenu(player);
    }

    public static void openWavesMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        if (state.getCurrentRaid() == null) {
            openMainMenu(player);
            return;
        }
        state.setCurrentMenu(MenuType.WAVES_MENU);
        state.setCurrentStep(MenuStep.SELECT_ACTION);
        RaidMenus.sendWavesMenu(player);
    }

    public static void openCreateWaveMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        if (state.getCurrentRaid() == null) {
            openMainMenu(player);
            return;
        }
        state.setCurrentMenu(MenuType.CREATE_WAVE);
        state.setCurrentStep(MenuStep.INPUT_WAVE_NUMBER);
        state.clearTempData();
        RaidMenus.sendCreateWaveMenu(player);
    }

    public static void openSpawnPointsMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        if (state.getCurrentWave() == null) {
            openWavesMenu(player);
            return;
        }
        state.setCurrentMenu(MenuType.SPAWN_POINTS_MENU);
        state.setCurrentStep(MenuStep.SELECT_ACTION);
        RaidMenus.sendSpawnPointsMenu(player);
    }

    public static void openCreateSpawnPointMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        if (state.getCurrentWave() == null) {
            openSpawnPointsMenu(player);
            return;
        }
        state.setCurrentMenu(MenuType.CREATE_SPAWN_POINT);
        state.setCurrentStep(MenuStep.SET_PLAYER_SPAWN);
        state.clearTempData();
        RaidMenus.sendCreateSpawnPointMenu(player);
    }

    public static void openRewardsMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        if (state.getCurrentWave() == null) {
            openWavesMenu(player);
            return;
        }
        state.setCurrentMenu(MenuType.REWARDS_MENU);
        state.setCurrentStep(MenuStep.SELECT_ACTION);
        RaidMenus.sendRewardsMenu(player);
    }

    public static void openAddRewardMenu(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        if (state.getCurrentWave() == null) {
            openRewardsMenu(player);
            return;
        }
        state.setCurrentMenu(MenuType.ADD_REWARD);
        state.setCurrentStep(MenuStep.INPUT_COMMAND);
        state.clearTempData();
        RaidMenus.sendAddRewardMenu(player);
    }

    /**
     * Obtiene información del estado actual para debugging
     */
    public static String getStateInfo(Player player) {
        RaidMenuState state = getPlayerMenuState(player);
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Estado de Menú ===\n");
        sb.append("§eJugador: §f").append(player.getName()).append("\n");
        sb.append("§eMenuú: §f").append(state.getCurrentMenu().getDisplayName()).append("\n");
        sb.append("§ePaso: §f").append(state.getCurrentStep().getDescription()).append("\n");
        if (state.getCurrentRaid() != null) {
            sb.append("§eRaid: §f").append(state.getCurrentRaid().getRaidName()).append("\n");
        }
        if (state.getCurrentWave() != null) {
            sb.append("§eOleada: §f").append(state.getCurrentWave().getWaveNumber()).append("\n");
        }
        return sb.toString();
    }
}