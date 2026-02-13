package org.debentialc.raids.managers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CooldownManager - Gestor de cooldowns
 * Responsable de verificar y gestionar cooldowns de raids por jugador
 */
public class CooldownManager {

    // Estructura: player_uuid -> (raid_id -> cooldown_end_time)
    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Establece un cooldown para un jugador en una raid
     */
    public static void setCooldown(UUID playerId, String raidId, long durationSeconds) {
        long endTime = System.currentTimeMillis() + (durationSeconds * 1000);

        cooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(raidId, endTime);

        System.out.println("[Raids] Cooldown establecido: " + playerId + " - " + raidId +
                " - Duración: " + durationSeconds + "s");
    }

    /**
     * Verifica si un jugador tiene cooldown en una raid
     */
    public static boolean hasCooldown(UUID playerId, String raidId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return false;
        }

        Long endTime = playerCooldowns.get(raidId);
        if (endTime == null) {
            return false;
        }

        // Verificar si el cooldown aún está activo
        if (System.currentTimeMillis() >= endTime) {
            // Cooldown expiró, removerlo
            playerCooldowns.remove(raidId);
            return false;
        }

        return true;
    }

    /**
     * Obtiene los segundos restantes de un cooldown
     */
    public static long getCooldownRemaining(UUID playerId, String raidId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return 0;
        }

        Long endTime = playerCooldowns.get(raidId);
        if (endTime == null) {
            return 0;
        }

        long remaining = (endTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * Obtiene el cooldown formateado en texto legible
     */
    public static String getCooldownFormattedTime(UUID playerId, String raidId) {
        long remaining = getCooldownRemaining(playerId, raidId);

        if (remaining <= 0) {
            return "No hay cooldown";
        }

        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Limpia un cooldown específico
     */
    public static void clearCooldown(UUID playerId, String raidId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns != null) {
            playerCooldowns.remove(raidId);
            System.out.println("[Raids] Cooldown removido: " + playerId + " - " + raidId);
        }
    }

    /**
     * Limpia todos los cooldowns de un jugador
     */
    public static void clearAllPlayerCooldowns(UUID playerId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns != null) {
            playerCooldowns.clear();
            System.out.println("[Raids] Todos los cooldowns de " + playerId + " han sido removidos");
        }
    }

    /**
     * Limpia todos los cooldowns
     */
    public static void clearAllCooldowns() {
        cooldowns.clear();
        System.out.println("[Raids] Todos los cooldowns han sido removidos");
    }

    /**
     * Obtiene todos los cooldowns activos de un jugador
     */
    public static Map<String, Long> getPlayerCooldowns(UUID playerId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return new HashMap<>();
        }

        // Crear una copia y remover los expirados
        Map<String, Long> active = new HashMap<>(playerCooldowns);
        active.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue());

        return active;
    }

    /**
     * Obtiene un resumen de los cooldowns de un jugador
     */
    public static String getCooldownSummary(UUID playerId) {
        Map<String, Long> playerCooldowns = getPlayerCooldowns(playerId);

        if (playerCooldowns.isEmpty()) {
            return "§aSin cooldowns activos";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Cooldowns Activos ===\n");

        for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
            String raidId = entry.getKey();
            long remaining = (entry.getValue() - System.currentTimeMillis()) / 1000;
            String timeFormatted = formatTime(remaining);

            sb.append("§e").append(raidId).append(": §f").append(timeFormatted).append("\n");
        }

        return sb.toString();
    }

    /**
     * Verifica si un jugador tiene al menos un cooldown
     */
    public static boolean hasAnyCooldown(UUID playerId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null || playerCooldowns.isEmpty()) {
            return false;
        }

        // Remover cooldowns expirados
        playerCooldowns.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue());

        return !playerCooldowns.isEmpty();
    }

    /**
     * Obtiene el cooldown más cercano a expirar
     */
    public static String getNextCooldownToExpire(UUID playerId) {
        Map<String, Long> playerCooldowns = getPlayerCooldowns(playerId);

        if (playerCooldowns.isEmpty()) {
            return null;
        }

        return playerCooldowns.entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Obtiene el tiempo del siguiente cooldown a expirar
     */
    public static long getNextCooldownTime(UUID playerId) {
        String raidId = getNextCooldownToExpire(playerId);
        if (raidId == null) {
            return 0;
        }
        return getCooldownRemaining(playerId, raidId);
    }

    /**
     * Obtiene el total de cooldowns activos de un jugador
     */
    public static int getTotalActiveCooldowns(UUID playerId) {
        return getPlayerCooldowns(playerId).size();
    }

    /**
     * Obtiene el total de todos los cooldowns en el servidor
     */
    public static int getTotalCooldowns() {
        int total = 0;
        for (Map<String, Long> playerCooldowns : cooldowns.values()) {
            total += playerCooldowns.size();
        }
        return total;
    }

    /**
     * Formatea tiempo en formato legible
     */
    private static String formatTime(long seconds) {
        if (seconds <= 0) {
            return "Expirado";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("§6%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("§6%dm %ds", minutes, secs);
        } else {
            return String.format("§6%ds", secs);
        }
    }

    /**
     * Obtiene información detallada de cooldowns para un jugador
     */
    public static String getCooldownInfo(UUID playerId, String raidId) {
        if (!hasCooldown(playerId, raidId)) {
            return "§aNo hay cooldown para esta raid";
        }

        long remaining = getCooldownRemaining(playerId, raidId);
        String formatted = getCooldownFormattedTime(playerId, raidId);

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Cooldown de Raid ===\n");
        sb.append("§eRaid: §f").append(raidId).append("\n");
        sb.append("§eTiempo restante: §f").append(formatted).append("\n");
        sb.append("§eSegundos: §f").append(remaining).append("s");

        return sb.toString();
    }
}