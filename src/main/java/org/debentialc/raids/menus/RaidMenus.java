package org.debentialc.raids.menus;

import org.bukkit.entity.Player;
import org.debentialc.raids.managers.RaidManager;
import org.debentialc.raids.models.Raid;
import org.debentialc.raids.models.SpawnPoint;
import org.debentialc.raids.models.Wave;
import org.debentialc.raids.models.WaveReward;
import org.debentialc.service.CC;
import java.util.List;

/**
 * RaidMenus - Sistema de menús para configuración de raids
 * Interfaz intuitiva basada en chat input
 */
public class RaidMenus {

    // ====== MENÚ PRINCIPAL ======

    public static void sendMainMenu(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  SISTEMA DE RAIDS"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Selecciona una opción escribiendo el número:"));
        player.sendMessage("");
        player.sendMessage(CC.translate("  &e[1] &7Crear Raid"));
        player.sendMessage(CC.translate("  &e[2] &7Editar Raid"));
        player.sendMessage(CC.translate("  &e[3] &7Ver Raids"));
        player.sendMessage(CC.translate("  &e[4] &7Eliminar Raid"));
        player.sendMessage(CC.translate("  &e[5] &7Salir"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== CREAR RAID ======

    public static void sendCreateRaidMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  CREAR NUEVA RAID"));
        player.sendMessage("");

        switch (state.getCurrentStep()) {
            case INPUT_NAME:
                player.sendMessage(CC.translate("&7Paso 1/4: Ingresa el nombre de la raid"));
                player.sendMessage(CC.translate("&eEjemplo: &fDragon's Lair"));
                break;

            case INPUT_DESCRIPTION:
                player.sendMessage(CC.translate("&7Paso 2/4: Ingresa la descripción"));
                player.sendMessage(CC.translate("&7Nombre: &f" + state.getTempRaidName()));
                player.sendMessage(CC.translate("&eEjemplo: &fUna épica batalla contra el dragón"));
                break;

            case INPUT_COOLDOWN:
                player.sendMessage(CC.translate("&7Paso 3/4: Ingresa el cooldown en minutos"));
                player.sendMessage(CC.translate("&7Nombre: &f" + state.getTempRaidName()));
                player.sendMessage(CC.translate("&7Descripción: &f" + state.getTempRaidDescription()));
                player.sendMessage(CC.translate("&eEjemplo: &f60"));
                break;

            case CONFIRM_SAVE:
                player.sendMessage(CC.translate("&7Paso 4/4: Confirma los datos"));
                player.sendMessage(CC.translate("&aNombre: &f" + state.getTempRaidName()));
                player.sendMessage(CC.translate("&aDescripción: &f" + state.getTempRaidDescription()));
                player.sendMessage(CC.translate("&aCooldown: &f" + state.getTempCooldownMinutes() + " minutos"));
                player.sendMessage("");
                player.sendMessage(CC.translate("&eEscribe &fsi &epara confirmar o &fno &epara cancelar"));
                break;

            default:
                break;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== LISTA DE RAIDS ======

    public static void sendRaidListMenu(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  RAIDS DISPONIBLES"));
        player.sendMessage("");

        List<Raid> raids = RaidManager.getEnabledRaids();

        if (raids.isEmpty()) {
            player.sendMessage(CC.translate("&cNo hay raids disponibles"));
        } else {
            for (int i = 0; i < raids.size(); i++) {
                Raid raid = raids.get(i);
                String status = raid.isConfigured() ? "&a✓" : "&c✗";
                player.sendMessage(CC.translate(String.format("&e[%d] &f%s %s &7(%d oleadas)",
                        i + 1, raid.getRaidName(), status, raid.getTotalWaves())));
            }
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&7Escribe el número para seleccionar una raid"));
        player.sendMessage(CC.translate("&7O escribe &fvolver &7para regresar al menú principal"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== CONFIGURAR RAID ======

    public static void sendRaidConfigMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Raid raid = state.getCurrentRaid();

        if (raid == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  CONFIGURAR RAID: &f" + raid.getRaidName()));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7¿Qué deseas hacer?"));
        player.sendMessage("");
        player.sendMessage(CC.translate("  &e[1] &7Configurar Arena"));
        player.sendMessage(CC.translate("  &e[2] &7Gestionar Oleadas"));
        player.sendMessage(CC.translate("  &e[3] &7Ver Información"));
        player.sendMessage(CC.translate("  &e[4] &7Habilitar/Deshabilitar"));
        player.sendMessage(CC.translate("  &e[5] &7Volver"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== GESTIONAR OLEADAS ======

    public static void sendWavesMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Raid raid = state.getCurrentRaid();

        if (raid == null) {
            RaidMenuManager.openMainMenu(player);
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  OLEADAS - &f" + raid.getRaidName()));
        player.sendMessage("");

        if (raid.getTotalWaves() == 0) {
            player.sendMessage(CC.translate("&cNo hay oleadas configuradas"));
        } else {
            for (int i = 0; i < raid.getTotalWaves(); i++) {
                Wave wave = raid.getWaveByIndex(i);
                player.sendMessage(CC.translate(String.format("&e[%d] &fOleada %d &7- %d enemigos",
                        i + 1, wave.getWaveNumber(), wave.getTotalEnemies())));
            }
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("  &e[A] &7Agregar Oleada"));
        player.sendMessage(CC.translate("  &e[E] &7Editar Oleada"));
        player.sendMessage(CC.translate("  &e[B] &7Volver"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== CREAR OLEADA ======

    public static void sendCreateWaveMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  CREAR OLEADA"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Ingresa el número de oleada"));
        player.sendMessage(CC.translate("&eEjemplo: &f1"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== PUNTOS DE APARICIÓN ======

    public static void sendSpawnPointsMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Wave wave = state.getCurrentWave();

        if (wave == null) {
            RaidMenuManager.openWavesMenu(player);
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  PUNTOS DE APARICIÓN - Oleada &f" + wave.getWaveNumber()));
        player.sendMessage("");

        if (wave.getSpawnPoints().isEmpty()) {
            player.sendMessage(CC.translate("&cNo hay puntos de aparición configurados"));
        } else {
            for (int i = 0; i < wave.getSpawnPoints().size(); i++) {
                SpawnPoint spawn = wave.getSpawnPoints().get(i);
                player.sendMessage(CC.translate(String.format("&e[%d] &f%s &7x%d",
                        i + 1, spawn.getNpcName(), spawn.getQuantity())));
            }
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("  &e[A] &7Agregar Punto de Aparición"));
        player.sendMessage(CC.translate("  &e[E] &7Editar Punto"));
        player.sendMessage(CC.translate("  &e[R] &7Recompensas"));
        player.sendMessage(CC.translate("  &e[B] &7Volver"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== CREAR PUNTO DE APARICIÓN ======

    public static void sendCreateSpawnPointMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  CREAR PUNTO DE APARICIÓN"));
        player.sendMessage("");

        switch (state.getCurrentStep()) {
            case SET_PLAYER_SPAWN:
                player.sendMessage(CC.translate("&7Tu ubicación actual será el punto de aparición"));
                player.sendMessage(CC.translate("&aPosición: &f" + state.getPlayer().getLocation().getBlockX() +
                        ", " + state.getPlayer().getLocation().getBlockY() +
                        ", " + state.getPlayer().getLocation().getBlockZ()));
                player.sendMessage("");
                player.sendMessage(CC.translate("&7Presiona SHIFT para confirmar o muévete para cambiar"));
                break;

            case INPUT_NPC_NAME:
                player.sendMessage(CC.translate("&7Ingresa el nombre del NPC"));
                player.sendMessage(CC.translate("&eEjemplo: &fDragon"));
                break;

            case INPUT_NPC_TAB:
                player.sendMessage(CC.translate("&7Ingresa el tab del NPC (default 10)"));
                player.sendMessage(CC.translate("&eEjemplo: &f10"));
                break;

            case INPUT_QUANTITY:
                player.sendMessage(CC.translate("&7NPC: &f" + state.getTempNpcName()));
                player.sendMessage(CC.translate("&7Tab: &f" + state.getTempNpcTab()));
                player.sendMessage(CC.translate("&7Ingresa la cantidad de NPCs"));
                player.sendMessage(CC.translate("&eEjemplo: &f5"));
                break;

            default:
                break;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== RECOMPENSAS ======

    public static void sendRewardsMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);
        Wave wave = state.getCurrentWave();

        if (wave == null) {
            RaidMenuManager.openSpawnPointsMenu(player);
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  RECOMPENSAS - Oleada &f" + wave.getWaveNumber()));
        player.sendMessage("");

        if (wave.getRewards().isEmpty()) {
            player.sendMessage(CC.translate("&cNo hay recompensas configuradas"));
        } else {
            for (int i = 0; i < wave.getRewards().size(); i++) {
                WaveReward reward = wave.getRewards().get(i);
                player.sendMessage(CC.translate(String.format("&e[%d] &f%s &7(%d%%)",
                        i + 1, reward.getCommand().substring(0, Math.min(30, reward.getCommand().length())) + "...",
                        reward.getProbability())));
            }
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("  &e[A] &7Agregar Recompensa"));
        player.sendMessage(CC.translate("  &e[E] &7Editar Recompensa"));
        player.sendMessage(CC.translate("  &e[B] &7Volver"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    // ====== AGREGAR RECOMPENSA ======

    public static void sendAddRewardMenu(Player player) {
        RaidMenuState state = RaidMenuManager.getPlayerMenuState(player);

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  AGREGAR RECOMPENSA"));
        player.sendMessage("");

        switch (state.getCurrentStep()) {
            case INPUT_COMMAND:
                player.sendMessage(CC.translate("&7Ingresa el comando a ejecutar"));
                player.sendMessage(CC.translate("&eEjemplo: &fgive @p diamond 1"));
                player.sendMessage(CC.translate("&e@p &f= jugadores que completaron la oleada"));
                break;

            case INPUT_PROBABILITY:
                player.sendMessage(CC.translate("&7Comando: &f" + state.getTempCommand()));
                player.sendMessage(CC.translate("&7Ingresa la probabilidad (0-100%)"));
                player.sendMessage(CC.translate("&eEjemplo: &f100"));
                break;

            default:
                break;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    /**
     * Envía un mensaje de error
     */
    public static void sendError(Player player, String message) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Error: &f" + message));
        player.sendMessage("");
    }

    /**
     * Envía un mensaje de éxito
     */
    public static void sendSuccess(Player player, String message) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Éxito: &f" + message));
        player.sendMessage("");
    }

    /**
     * Envía un mensaje informativo
     */
    public static void sendInfo(Player player, String message) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&bℹ &f" + message));
        player.sendMessage("");
    }
}