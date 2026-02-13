package org.debentialc.raids.managers;

import org.debentialc.raids.models.Raid;
import org.debentialc.raids.models.RaidSession;
import org.debentialc.raids.models.RaidStatus;
import org.debentialc.raids.models.WaveStatus;
import org.debentialc.raids.models.Party;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RaidSessionManager - Gestor de sesiones de raid en progreso
 * Responsable de manejar raids activas
 */
public class RaidSessionManager {

    private static final Map<String, RaidSession> activeSessions = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerToSession = new ConcurrentHashMap<>();
    private static int sessionCounter = 0;

    /**
     * Crea una nueva sesión de raid
     */
    public static RaidSession createRaidSession(Raid raid, Party party) {
        if (raid == null || party == null) {
            return null;
        }

        String sessionId = "session_" + (++sessionCounter) + "_" + System.currentTimeMillis();
        RaidSession session = new RaidSession(sessionId, raid, party);

        activeSessions.put(sessionId, session);

        // Mapear jugadores a sesión
        for (UUID playerId : party.getActivePlayers()) {
            playerToSession.put(playerId, sessionId);
        }

        System.out.println("[Raids] Sesión de raid creada: " + sessionId + " - Raid: " + raid.getRaidId());
        return session;
    }

    /**
     * Obtiene la sesión activa de un jugador
     */
    public static RaidSession getPlayerSession(UUID playerId) {
        String sessionId = playerToSession.get(playerId);
        if (sessionId == null) {
            return null;
        }
        return activeSessions.get(sessionId);
    }

    /**
     * Obtiene una sesión por ID
     */
    public static RaidSession getSessionById(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Obtiene todas las sesiones activas
     */
    public static Collection<RaidSession> getAllActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }

    /**
     * Obtiene la sesión de una raid específica
     */
    public static RaidSession getSessionByRaid(Raid raid) {
        for (RaidSession session : activeSessions.values()) {
            if (session.getRaid().getRaidId().equals(raid.getRaidId())) {
                return session;
            }
        }
        return null;
    }

    /**
     * Verifica si hay una sesión activa para una raid
     */
    public static boolean hasActiveSession(String raidId) {
        for (RaidSession session : activeSessions.values()) {
            if (session.getRaid().getRaidId().equals(raidId) &&
                    session.getStatus() == RaidStatus.IN_PROGRESS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Completa una oleada
     */
    public static void completeWave(RaidSession session) {
        if (session == null) {
            return;
        }

        session.getCurrentWave().setStatus(WaveStatus.COMPLETED);
        System.out.println("[Raids] Oleada completada: " + session.getSessionId() + " - Wave " +
                (session.getCurrentWaveIndex() + 1));

        // Ejecutar recompensas
        if (session.getCurrentWave().hasRewards()) {
            executeWaveRewards(session);
        }

        // Verificar si hay siguiente oleada
        if (session.hasNextWave()) {
            session.moveToNextWave();
            session.getCurrentWave().setStatus(WaveStatus.ACTIVE);
            System.out.println("[Raids] Siguiente oleada iniciada: Wave " +
                    (session.getCurrentWaveIndex() + 1));
        } else {
            // Raid completada
            completeRaid(session);
        }
    }

    /**
     * Ejecuta las recompensas de una oleada a todos los jugadores vivos
     */
    private static void executeWaveRewards(RaidSession session) {
        session.getCurrentWave().getRewards().forEach(reward -> {
            if (reward.shouldExecute()) {
                // Ejecutar comando para cada jugador activo
                session.getActivePlayers().forEach(playerId -> {
                    String command = reward.getCommand()
                            .replace("@a", playerId.toString())
                            .replace("{player}", playerId.toString());

                    System.out.println("[Raids] Ejecutando recompensa: " + command);
                    // TODO: Ejecutar comando via Bukkit
                });
            }
        });
    }

    /**
     * Completa la raid (victoria)
     */
    public static void completeRaid(RaidSession session) {
        if (session == null) {
            return;
        }

        session.setStatus(RaidStatus.COMPLETED);
        session.setEndTime(System.currentTimeMillis());

        System.out.println("[Raids] Raid completada: " + session.getSessionId());
        System.out.println("[Raids] Duración: " + session.getDurationSeconds() + "s");
        System.out.println("[Raids] Jugadores activos: " + session.getActivePlayers().size());

        // Aplicar cooldown a todos los jugadores
        for (UUID playerId : session.getActivePlayers()) {
            CooldownManager.setCooldown(playerId, session.getRaid().getRaidId(),
                    session.getRaid().getCooldownSeconds());
        }

        // Generar efectos de victoria
        // TODO: Generar efectos visuales

        // Remover sesión después de un tiempo
        scheduleSessionRemoval(session.getSessionId());
    }

    /**
     * Falla la raid (derrota - todos mueren)
     */
    public static void failRaid(RaidSession session) {
        if (session == null) {
            return;
        }

        session.setStatus(RaidStatus.FAILED);
        session.setEndTime(System.currentTimeMillis());

        System.out.println("[Raids] Raid fallida: " + session.getSessionId());

        // Generar efectos de derrota
        // TODO: Generar efectos visuales

        // Remover sesión
        removeSession(session.getSessionId());
    }

    /**
     * Maneja la muerte de un jugador en la raid
     */
    public static void playerDied(UUID playerId) {
        RaidSession session = getPlayerSession(playerId);
        if (session == null) {
            return;
        }

        session.playerDied(playerId);
        System.out.println("[Raids] Jugador muerto en raid: " + playerId);

        // Verificar si la raid falló (todos muertos)
        if (session.isRaidFailed()) {
            failRaid(session);
        }
    }

    /**
     * Maneja cuando un jugador se va de la raid
     */
    public static void playerLeft(UUID playerId) {
        RaidSession session = getPlayerSession(playerId);
        if (session == null) {
            return;
        }

        session.playerLeft(playerId);
        playerToSession.remove(playerId);
        System.out.println("[Raids] Jugador salió de la raid: " + playerId);

        // Verificar si la raid falló
        if (session.isRaidFailed()) {
            failRaid(session);
        }
    }

    /**
     * Maneja cuando un jugador regresa a la raid
     */
    public static void playerReturned(UUID playerId) {
        RaidSession session = getPlayerSession(playerId);
        if (session == null) {
            return;
        }

        if (session.canPlayerRejoin(playerId)) {
            session.playerReturned(playerId);
            playerToSession.put(playerId, session.getSessionId());
            System.out.println("[Raids] Jugador regresó a la raid: " + playerId);
        }
    }

    /**
     * Obtiene el progreso de una sesión (0-100)
     */
    public static int getSessionProgress(RaidSession session) {
        return session != null ? session.getProgress() : 0;
    }

    /**
     * Obtiene la duración en segundos de una sesión
     */
    public static long getSessionDuration(RaidSession session) {
        return session != null ? session.getDurationSeconds() : 0;
    }

    /**
     * Obtiene información de una sesión
     */
    public static String getSessionInfo(RaidSession session) {
        if (session == null) {
            return "Sesión no encontrada";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== Información de Sesión ===\n");
        sb.append("§eID: §f").append(session.getSessionId()).append("\n");
        sb.append("§eRaid: §f").append(session.getRaid().getRaidName()).append("\n");
        sb.append("§eOleada: §f").append(session.getCurrentWaveIndex() + 1).append("/")
                .append(session.getRaid().getTotalWaves()).append("\n");
        sb.append("§eProgreso: §f").append(session.getProgress()).append("%\n");
        sb.append("§eJugadores activos: §f").append(session.getActivePlayers().size()).append("\n");
        sb.append("§eJugadores muertos: §f").append(session.getDeadPlayers().size()).append("\n");
        sb.append("§eEstado: §f").append(session.getStatus().getDisplayName()).append("\n");
        sb.append("§eDuración: §f").append(session.getDurationSeconds()).append("s");

        return sb.toString();
    }

    /**
     * Cuenta el total de sesiones activas
     */
    public static int getTotalActiveSessions() {
        return activeSessions.size();
    }

    /**
     * Obtiene el total de jugadores en raids
     */
    public static int getTotalPlayersInRaids() {
        int total = 0;
        for (RaidSession session : activeSessions.values()) {
            total += session.getActivePlayers().size();
        }
        return total;
    }

    /**
     * Programa la eliminación de una sesión (después de 5 minutos)
     */
    private static void scheduleSessionRemoval(String sessionId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                removeSession(sessionId);
            }
        }, 300000); // 5 minutos
    }

    /**
     * Elimina una sesión
     */
    public static void removeSession(String sessionId) {
        RaidSession session = activeSessions.remove(sessionId);
        if (session != null) {
            for (UUID playerId : session.getActivePlayers()) {
                playerToSession.remove(playerId);
            }
            System.out.println("[Raids] Sesión eliminada: " + sessionId);
        }
    }

    /**
     * Limpia todas las sesiones
     */
    public static void clearAllSessions() {
        activeSessions.clear();
        playerToSession.clear();
        System.out.println("[Raids] Todas las sesiones han sido limpiadas");
    }
}