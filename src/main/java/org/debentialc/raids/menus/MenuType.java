package org.debentialc.raids.menus;

/**
 * Tipos de menús disponibles
 */
public enum MenuType {
    MAIN("Menú Principal"),
    RAID_LIST("Lista de Raids"),
    CREATE_RAID("Crear Raid"),
    EDIT_RAID("Editar Raid"),
    RAID_CONFIG("Configurar Raid"),
    WAVES_MENU("Menú de Oleadas"),
    CREATE_WAVE("Crear Oleada"),
    EDIT_WAVE("Editar Oleada"),
    SPAWN_POINTS_MENU("Puntos de Aparición"),
    CREATE_SPAWN_POINT("Crear Punto de Aparición"),
    REWARDS_MENU("Menú de Recompensas"),
    ADD_REWARD("Agregar Recompensa");

    private final String displayName;

    MenuType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
