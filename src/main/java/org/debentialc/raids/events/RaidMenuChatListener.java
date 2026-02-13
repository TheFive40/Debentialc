package org.debentialc.raids.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.debentialc.raids.menus.RaidMenuManager;
import org.debentialc.raids.menus.RaidMenuState;
import org.debentialc.raids.menus.MenuType;
import org.debentialc.raids.menus.RaidMenuInputHandler;

/**
 * RaidMenuChatListener - Escucha inputs de chat para los menús
 * Procesa lo que escribe el usuario cuando está en un menú
 */
public class RaidMenuChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();

        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player.getUniqueId());
        if (state == null) {
            return;
        }

        event.setCancelled(true);

        MenuType currentMenu = state.getCurrentMenu();

        switch (currentMenu) {
            case MAIN:
                RaidMenuInputHandler.handleMainMenuInput(player, message);
                break;

            case CREATE_RAID:
                RaidMenuInputHandler.handleCreateRaidInput(player, message);
                break;

            case RAID_LIST:
                RaidMenuInputHandler.handleRaidListInput(player, message);
                break;

            case RAID_CONFIG:
                RaidMenuInputHandler.handleRaidConfigInput(player, message);
                break;

            case WAVES_MENU:
                RaidMenuInputHandler.handleWavesMenuInput(player, message);
                break;

            case CREATE_WAVE:
                RaidMenuInputHandler.handleCreateWaveInput(player, message);
                break;

            case SPAWN_POINTS_MENU:
                RaidMenuInputHandler.handleSpawnPointsMenuInput(player, message);
                break;

            case CREATE_SPAWN_POINT:
                RaidMenuInputHandler.handleCreateSpawnPointInput(player, message);
                break;

            case REWARDS_MENU:
                RaidMenuInputHandler.handleRewardsMenuInput(player, message);
                break;

            case ADD_REWARD:
                RaidMenuInputHandler.handleAddRewardInput(player, message);
                break;

            default:
                break;
        }
    }
}