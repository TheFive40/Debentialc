package org.debentialc.raids.menus;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.debentialc.raids.managers.RaidManager;
import org.debentialc.raids.models.Raid;
import org.debentialc.raids.models.Wave;
import org.debentialc.raids.models.SpawnPoint;
import org.debentialc.raids.models.WaveReward;
import java.util.List;

/**
 * RaidMenuInputHandler - Procesa inputs de chat en los menús
 * Valida y procesa lo que el usuario escribe
 */
public class RaidMenuInputHandler {

    /**
     * Procesa el input del menú principal
     */
    public static void handleMainMenuInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);

        switch (input.toLowerCase()) {
            case "1":
                RaidMenuManager.openCreateRaidMenu(player);
                break;
            case "2":
                RaidMenuManager.openRaidListMenu(player);
                break;
            case "3":
                RaidMenuManager.openRaidListMenu(player);
                break;
            case "4":
                RaidMenus.sendInfo(player, "Selecciona una raid de la lista para eliminarla");
                RaidMenuManager.openRaidListMenu(player);
                break;
            case "5":
                RaidMenus.sendInfo(player, "Menú cerrado");
                RaidMenuManager.clearPlayerState(player);
                break;
            default:
                RaidMenus.sendError(player, "Opción no válida. Ingresa un número del 1 al 5");
                RaidMenuManager.openMainMenu(player);
        }
    }

    /**
     * Procesa el input de crear raid
     */
    public static void handleCreateRaidInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);

        switch (state.getCurrentStep()) {
            case INPUT_NAME:
                if (input.trim().isEmpty() || input.length() > 30) {
                    RaidMenus.sendError(player, "El nombre debe tener entre 1 y 30 caracteres");
                    RaidMenuManager.openCreateRaidMenu(player);
                    return;
                }
                state.setTempRaidName(input);
                state.setCurrentStep(MenuStep.INPUT_DESCRIPTION);
                RaidMenuManager.openCreateRaidMenu(player);
                break;

            case INPUT_DESCRIPTION:
                if (input.trim().isEmpty() || input.length() > 100) {
                    RaidMenus.sendError(player, "La descripción debe tener entre 1 y 100 caracteres");
                    RaidMenuManager.openCreateRaidMenu(player);
                    return;
                }
                state.setTempRaidDescription(input);
                state.setCurrentStep(MenuStep.INPUT_COOLDOWN);
                RaidMenuManager.openCreateRaidMenu(player);
                break;

            case INPUT_COOLDOWN:
                try {
                    int minutes = Integer.parseInt(input);
                    if (minutes < 1 || minutes > 1440) {
                        RaidMenus.sendError(player, "El cooldown debe estar entre 1 y 1440 minutos");
                        RaidMenuManager.openCreateRaidMenu(player);
                        return;
                    }
                    state.setTempCooldownMinutes(minutes);
                    state.setCurrentStep(MenuStep.CONFIRM_SAVE);
                    RaidMenuManager.openCreateRaidMenu(player);
                } catch (NumberFormatException e) {
                    RaidMenus.sendError(player, "Debes ingresar un número válido");
                    RaidMenuManager.openCreateRaidMenu(player);
                }
                break;

            case CONFIRM_SAVE:
                if (input.equalsIgnoreCase("si")) {
                    // Crear raid
                    Raid raid = RaidManager.createRaid(state.getTempRaidName());
                    raid.setDescription(state.getTempRaidDescription());
                    raid.setCooldownSeconds(state.getTempCooldownMinutes() * 60L);
                    RaidManager.updateRaid(raid);

                    RaidMenus.sendSuccess(player, "Raid creada: " + state.getTempRaidName());

                    // Establecer como raid actual y abrir config
                    state.setCurrentRaid(raid);
                    state.setCurrentStep(MenuStep.SELECT_ACTION);
                    RaidMenuManager.openRaidConfigMenu(player);
                } else if (input.equalsIgnoreCase("no")) {
                    RaidMenus.sendInfo(player, "Operación cancelada");
                    RaidMenuManager.openMainMenu(player);
                } else {
                    RaidMenus.sendError(player, "Escribe 'si' o 'no'");
                    RaidMenuManager.openCreateRaidMenu(player);
                }
                break;

            default:
                RaidMenuManager.openCreateRaidMenu(player);
        }
    }

    /**
     * Procesa el input de lista de raids
     */
    public static void handleRaidListInput(Player player, String input) {
        if (input.equalsIgnoreCase("volver")) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        try {
            int index = Integer.parseInt(input) - 1;
            List<Raid> raids = RaidManager.getEnabledRaids();

            if (index < 0 || index >= raids.size()) {
                RaidMenus.sendError(player, "Número no válido");
                RaidMenuManager.openRaidListMenu(player);
                return;
            }

            RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
            state.setCurrentRaid(raids.get(index));
            RaidMenuManager.openRaidConfigMenu(player);

        } catch (NumberFormatException e) {
            RaidMenus.sendError(player, "Debes ingresar un número");
            RaidMenuManager.openRaidListMenu(player);
        }
    }

    /**
     * Procesa el input de configuración de raid
     */
    public static void handleRaidConfigInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Raid raid = state.getCurrentRaid();

        if (raid == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        switch (input.toLowerCase()) {
            case "1":
                RaidMenus.sendInfo(player, "Haz clic en el bloque donde aparecerán los NPCs");
                state.setCurrentStep(MenuStep.SET_ARENA_SPAWN);
                break;

            case "2":
                state.setCurrentStep(MenuStep.SELECT_ACTION);
                RaidMenuManager.openWavesMenu(player);
                break;

            case "3":
                player.sendMessage(RaidManager.getRaidInfo(raid.getRaidId()));
                RaidMenuManager.openRaidConfigMenu(player);
                break;

            case "4":
                if (raid.isEnabled()) {
                    RaidManager.disableRaid(raid.getRaidId());
                    RaidMenus.sendSuccess(player, "Raid deshabilitada");
                } else {
                    RaidManager.enableRaid(raid.getRaidId());
                    RaidMenus.sendSuccess(player, "Raid habilitada");
                }
                RaidMenuManager.openRaidConfigMenu(player);
                break;

            case "5":
                RaidMenuManager.openMainMenu(player);
                break;

            default:
                RaidMenus.sendError(player, "Opción no válida");
                RaidMenuManager.openRaidConfigMenu(player);
        }
    }

    /**
     * Procesa el input de oleadas
     */
    public static void handleWavesMenuInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Raid raid = state.getCurrentRaid();

        if (raid == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        switch (input.toLowerCase()) {
            case "a":
                RaidMenuManager.openCreateWaveMenu(player);
                break;

            case "e":
                RaidMenus.sendInfo(player, "Ingresa el número de la oleada a editar");
                break;

            case "b":
                RaidMenuManager.openRaidConfigMenu(player);
                break;

            default:
                // Intenta seleccionar oleada
                try {
                    int index = Integer.parseInt(input) - 1;
                    if (index < 0 || index >= raid.getTotalWaves()) {
                        RaidMenus.sendError(player, "Oleada no válida");
                        RaidMenuManager.openWavesMenu(player);
                        return;
                    }

                    Wave wave = raid.getWaveByIndex(index);
                    state.setCurrentWave(wave);
                    RaidMenuManager.openSpawnPointsMenu(player);

                } catch (NumberFormatException e) {
                    RaidMenus.sendError(player, "Debes ingresar 'a', 'e' o un número");
                    RaidMenuManager.openWavesMenu(player);
                }
        }
    }

    /**
     * Procesa el input de crear oleada
     */
    public static void handleCreateWaveInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Raid raid = state.getCurrentRaid();

        if (raid == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        try {
            int waveNumber = Integer.parseInt(input);
            if (waveNumber < 1 || waveNumber > 20) {
                RaidMenus.sendError(player, "El número de oleada debe estar entre 1 y 20");
                RaidMenuManager.openCreateWaveMenu(player);
                return;
            }

            // Verificar que no exista
            if (raid.getWaveByIndex(waveNumber - 1) != null) {
                RaidMenus.sendError(player, "Esta oleada ya existe");
                RaidMenuManager.openCreateWaveMenu(player);
                return;
            }

            Wave wave = new Wave(waveNumber);
            raid.addWave(wave);
            RaidManager.updateRaid(raid);

            RaidMenus.sendSuccess(player, "Oleada " + waveNumber + " creada");

            state.setCurrentWave(wave);
            RaidMenuManager.openSpawnPointsMenu(player);

        } catch (NumberFormatException e) {
            RaidMenus.sendError(player, "Debes ingresar un número válido");
            RaidMenuManager.openCreateWaveMenu(player);
        }
    }

    /**
     * Procesa el input de puntos de aparición
     */
    public static void handleSpawnPointsMenuInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Wave wave = state.getCurrentWave();

        if (wave == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        switch (input.toLowerCase()) {
            case "a":
                RaidMenuManager.openCreateSpawnPointMenu(player);
                break;

            case "e":
                RaidMenus.sendInfo(player, "Ingresa el número del punto a editar");
                break;

            case "r":
                RaidMenuManager.openRewardsMenu(player);
                break;

            case "b":
                RaidMenuManager.openWavesMenu(player);
                break;

            default:
                RaidMenus.sendError(player, "Opción no válida. Ingresa 'a', 'e', 'r' o 'b'");
                RaidMenuManager.openSpawnPointsMenu(player);
        }
    }

    /**
     * Procesa el input de crear punto de aparición
     */
    public static void handleCreateSpawnPointInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Wave wave = state.getCurrentWave();

        if (wave == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        switch (state.getCurrentStep()) {
            case INPUT_NPC_NAME:
                if (input.trim().isEmpty() || input.length() > 30) {
                    RaidMenus.sendError(player, "El nombre debe tener entre 1 y 30 caracteres");
                    RaidMenuManager.openCreateSpawnPointMenu(player);
                    return;
                }
                state.setTempNpcName(input);
                state.setCurrentStep(MenuStep.INPUT_NPC_TAB);
                RaidMenuManager.openCreateSpawnPointMenu(player);
                break;

            case INPUT_NPC_TAB:
                try {
                    int tab = Integer.parseInt(input);
                    if (tab < 1 || tab > 100) {
                        RaidMenus.sendError(player, "El tab debe estar entre 1 y 100");
                        RaidMenuManager.openCreateSpawnPointMenu(player);
                        return;
                    }
                    state.setTempNpcTab(tab);
                    state.setCurrentStep(MenuStep.INPUT_QUANTITY);
                    RaidMenuManager.openCreateSpawnPointMenu(player);
                } catch (NumberFormatException e) {
                    RaidMenus.sendError(player, "Debes ingresar un número válido");
                    RaidMenuManager.openCreateSpawnPointMenu(player);
                }
                break;

            case INPUT_QUANTITY:
                try {
                    int quantity = Integer.parseInt(input);
                    if (quantity < 1 || quantity > 100) {
                        RaidMenus.sendError(player, "La cantidad debe estar entre 1 y 100");
                        RaidMenuManager.openCreateSpawnPointMenu(player);
                        return;
                    }

                    // Crear spawn point con ubicación actual del jugador
                    Location loc = player.getLocation();
                    SpawnPoint spawn = new SpawnPoint(loc, state.getTempNpcName(),
                            state.getTempNpcTab(), quantity);

                    wave.addSpawnPoint(spawn);

                    // Actualizar raid
                    Raid raid = state.getCurrentRaid();
                    RaidManager.updateRaid(raid);

                    RaidMenus.sendSuccess(player, "Punto de aparición creado");
                    RaidMenuManager.openSpawnPointsMenu(player);

                } catch (NumberFormatException e) {
                    RaidMenus.sendError(player, "Debes ingresar un número válido");
                    RaidMenuManager.openCreateSpawnPointMenu(player);
                }
                break;

            default:
                RaidMenuManager.openCreateSpawnPointMenu(player);
        }
    }

    /**
     * Procesa el input de recompensas
     */
    public static void handleRewardsMenuInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Wave wave = state.getCurrentWave();

        if (wave == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        switch (input.toLowerCase()) {
            case "a":
                RaidMenuManager.openAddRewardMenu(player);
                break;

            case "e":
                RaidMenus.sendInfo(player, "Ingresa el número de la recompensa a editar");
                break;

            case "b":
                RaidMenuManager.openSpawnPointsMenu(player);
                break;

            default:
                RaidMenus.sendError(player, "Opción no válida. Ingresa 'a', 'e' o 'b'");
                RaidMenuManager.openRewardsMenu(player);
        }
    }

    /**
     * Procesa el input de agregar recompensa
     */
    public static void handleAddRewardInput(Player player, String input) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Wave wave = state.getCurrentWave();

        if (wave == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        switch (state.getCurrentStep()) {
            case INPUT_COMMAND:
                if (input.trim().isEmpty() || input.length() > 200) {
                    RaidMenus.sendError(player, "El comando debe tener entre 1 y 200 caracteres");
                    RaidMenuManager.openAddRewardMenu(player);
                    return;
                }
                state.setTempCommand(input);
                state.setCurrentStep(MenuStep.INPUT_PROBABILITY);
                RaidMenuManager.openAddRewardMenu(player);
                break;

            case INPUT_PROBABILITY:
                try {
                    int probability = Integer.parseInt(input);
                    if (probability < 0 || probability > 100) {
                        RaidMenus.sendError(player, "La probabilidad debe estar entre 0 y 100");
                        RaidMenuManager.openAddRewardMenu(player);
                        return;
                    }

                    WaveReward reward = new WaveReward(state.getTempCommand(), probability);
                    wave.addReward(reward);

                    // Actualizar raid
                    Raid raid = state.getCurrentRaid();
                    RaidManager.updateRaid(raid);

                    RaidMenus.sendSuccess(player, "Recompensa agregada");
                    RaidMenuManager.openRewardsMenu(player);

                } catch (NumberFormatException e) {
                    RaidMenus.sendError(player, "Debes ingresar un número válido");
                    RaidMenuManager.openAddRewardMenu(player);
                }
                break;

            default:
                RaidMenuManager.openAddRewardMenu(player);
        }
    }
}