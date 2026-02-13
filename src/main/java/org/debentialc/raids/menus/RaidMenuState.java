package org.debentialc.raids.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.debentialc.raids.models.Raid;
import org.debentialc.raids.models.Wave;
import org.debentialc.raids.models.SpawnPoint;

import java.util.UUID;

/**
 * RaidMenuState - Rastrea el estado actual del jugador en los menús
 * Guarda qué menú tiene abierto y qué datos está editando
 */
@Getter
@Setter
public class RaidMenuState {

    private UUID playerId;
    private Player player;

    private MenuType currentMenu;
    private MenuStep currentStep;

    private Raid currentRaid;
    private Wave currentWave;
    private SpawnPoint currentSpawnPoint;

    private String tempRaidName;
    private String tempRaidDescription;
    private int tempCooldownMinutes;
    private String tempNpcName;
    private int tempNpcTab;
    private int tempQuantity;
    private int tempProbability;
    private String tempCommand;

    public RaidMenuState(UUID playerId, Player player) {
        this.playerId = playerId;
        this.player = player;
        this.currentMenu = MenuType.MAIN;
        this.currentStep = MenuStep.SELECT_ACTION;
        this.tempNpcTab = 10; // Default tab
    }

    public void reset() {
        this.currentMenu = MenuType.MAIN;
        this.currentStep = MenuStep.SELECT_ACTION;
        this.currentRaid = null;
        this.currentWave = null;
        this.currentSpawnPoint = null;
        clearTempData();
    }

    public void clearTempData() {
        this.tempRaidName = null;
        this.tempRaidDescription = null;
        this.tempCooldownMinutes = 0;
        this.tempNpcName = null;
        this.tempNpcTab = 10;
        this.tempQuantity = 0;
        this.tempProbability = 0;
        this.tempCommand = null;
    }

    @Override
    public String toString() {
        return String.format("RaidMenuState{player=%s, menu=%s, step=%s}",
                playerId, currentMenu, currentStep);
    }
}

/**
 * Pasos dentro de cada menú
 */
enum MenuStep {
    SELECT_ACTION("Selecciona una acción"),
    SELECT_RAID("Selecciona una raid"),
    INPUT_NAME("Ingresa el nombre"),
    INPUT_DESCRIPTION("Ingresa la descripción"),
    INPUT_COOLDOWN("Ingresa los minutos de cooldown"),
    SET_ARENA_SPAWN("Haz clic en la arena para establecer el punto"),
    SET_PLAYER_SPAWN("Haz clic donde deseas que aparezcan los jugadores"),
    INPUT_WAVE_NUMBER("Ingresa el número de oleada"),
    INPUT_NPC_NAME("Ingresa el nombre del NPC"),
    INPUT_NPC_TAB("Ingresa el tab del NPC (default 10)"),
    INPUT_QUANTITY("Ingresa la cantidad de NPCs"),
    INPUT_PROBABILITY("Ingresa la probabilidad (0-100%)"),
    INPUT_COMMAND("Ingresa el comando a ejecutar"),
    CONFIRM_DELETE("¿Confirmas la eliminación? (si/no)"),
    CONFIRM_SAVE("¿Confirmas los cambios? (si/no)");

    private final String description;

    MenuStep(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}