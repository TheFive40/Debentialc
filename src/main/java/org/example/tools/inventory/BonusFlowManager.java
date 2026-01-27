package org.example.tools.inventory;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;

/**
 * Gestiona el flujo interactivo de creación de bonificaciones
 * Controla en qué paso del proceso se encuentra cada jugador
 */
public class BonusFlowManager {

    public static class BonusFlowState {
        public String step; // "selecting_stat", "selecting_operation", "selecting_value"
        public String selectedStat;
        public String selectedOperation;
        public double selectedValue;
        public String itemId; // Para saber a qué item/armor aplicar el bonus

        public BonusFlowState(String itemId) {
            this.step = "selecting_stat";
            this.itemId = itemId;
        }
    }

    private static final HashMap<UUID, BonusFlowState> playerStates = new HashMap<>();

    /**
     * Inicia el flujo de bonificación para un jugador
     */
    public static void startBonusFlow(Player player, String itemId) {
        playerStates.put(player.getUniqueId(), new BonusFlowState(itemId));
    }

    /**
     * Obtiene el estado actual del flujo de un jugador
     */
    public static BonusFlowState getFlowState(Player player) {
        return playerStates.getOrDefault(player.getUniqueId(), null);
    }

    /**
     * Avanza al siguiente paso
     */
    public static void nextStep(Player player) {
        BonusFlowState state = playerStates.get(player.getUniqueId());
        if (state == null) return;

        if ("selecting_stat".equals(state.step)) {
            state.step = "selecting_operation";
        } else if ("selecting_operation".equals(state.step)) {
            state.step = "selecting_value";
        }
    }

    /**
     * Establece el stat seleccionado
     */
    public static void setSelectedStat(Player player, String stat) {
        BonusFlowState state = playerStates.get(player.getUniqueId());
        if (state != null) {
            state.selectedStat = stat;
        }
    }

    /**
     * Establece la operación seleccionada
     */
    public static void setSelectedOperation(Player player, String operation) {
        BonusFlowState state = playerStates.get(player.getUniqueId());
        if (state != null) {
            state.selectedOperation = operation;
        }
    }

    /**
     * Establece el valor seleccionado
     */
    public static void setSelectedValue(Player player, double value) {
        BonusFlowState state = playerStates.get(player.getUniqueId());
        if (state != null) {
            state.selectedValue = value;
        }
    }

    /**
     * Limpia el estado del jugador
     */
    public static void clearFlowState(Player player) {
        playerStates.remove(player.getUniqueId());
    }

    /**
     * Verifica si el jugador está en un flujo de bonificación
     */
    public static boolean isInBonusFlow(Player player) {
        return playerStates.containsKey(player.getUniqueId());
    }
}