package org.debentialc.raids.events;

import noppes.npcs.api.IPos;
import noppes.npcs.api.event.INpcEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.debentialc.Main;
import org.debentialc.raids.effects.RaidEffects;
import org.debentialc.raids.managers.NPCSpawnManager;
import org.debentialc.raids.managers.RaidSessionManager;
import org.debentialc.raids.managers.RaidSoundManager;
import org.debentialc.raids.managers.RaidTitleManager;
import org.debentialc.raids.models.RaidSession;
import org.debentialc.raids.models.SpawnPoint;
import org.debentialc.raids.models.Wave;
import org.debentialc.raids.models.WaveStatus;

import java.util.*;

/**
 * NPCDeathListener - Escucha muertes de NPCs en CustomNPC
 * VERSIÓN CORREGIDA: Usa entity ID para tracking confiable de NPCs
 */
public class NPCDeathListener implements Listener {

    // Trackear qué oleadas ya dieron recompensas
    private static final Set<String> rewardsGiven = new HashSet<>();

    // Trackear countdowns activos para evitar duplicados
    private static final Set<String> activeCountdowns = new HashSet<>();

    public void onNpcDie(INpcEvent.DiedEvent event) {
        try {
            int entityId = event.getNpc().getEntityId();

            System.out.println("[Raids] NPC muerto detectado - Entity ID: " + entityId);

            String waveId = NPCSpawnManager.getWaveIdForNpc(entityId);

            if (waveId == null) {
                System.out.println("[Raids] NPC no pertenece a ninguna raid activa");
                return;
            }

            System.out.println("[Raids] NPC pertenece a wave: " + waveId);

            Player killer = null;
            try {
                UUID killerUuid = UUID.fromString(event.getSource().getUniqueID());
                killer = Bukkit.getPlayer(killerUuid);
            } catch (Exception e) {
                System.err.println("[Raids] Error al obtener killer: " + e.getMessage());
            }

            // Obtener la sesión de raid
            RaidSession session = getSessionByWaveId(waveId);
            if (session == null) {
                System.err.println("[Raids] No se encontró sesión para wave: " + waveId);
                return;
            }

            handleNpcDeath(entityId, waveId, session, killer);

        } catch (Exception e) {
            System.err.println("[Raids] Error en onNpcDie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la sesión de raid basándose en el waveId
     */
    private RaidSession getSessionByWaveId(String waveId) {
        // El waveId tiene formato: sessionId_wave_X
        String sessionId = waveId.substring(0, waveId.lastIndexOf("_wave_"));
        return RaidSessionManager.getSessionById(sessionId);
    }

    /**
     * Maneja la muerte de un NPC en una raid
     */
    private void handleNpcDeath(int entityId, String waveId, RaidSession session, Player killer) {
        Wave wave = session.getCurrentWave();

        if (wave == null) {
            return;
        }

        boolean wasTracked = NPCSpawnManager.markNpcDead(entityId, waveId);

        if (!wasTracked) {
            System.out.println("[Raids] NPC ya estaba marcado como muerto");
            return;
        }

        playDeathEffects(session, killer);

        int remaining = NPCSpawnManager.getAliveNpcsCount(waveId);

        System.out.println("[Raids] NPCs restantes: " + remaining);

        if (remaining == 0) {
            System.out.println("[Raids] ¡Oleada completada!");
            completeWave(session, waveId);
        } else {
            updateWaveProgress(session, remaining);
        }
    }

    /**
     * Reproduce efectos cuando muere un NPC
     */
    private void playDeathEffects(RaidSession session, Player killer) {
        for (UUID playerId : session.getActivePlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                RaidSoundManager.playDamageSound(player);
            }
        }

        if (killer != null) {
            String waveId = getWaveId(session);
            int remaining = NPCSpawnManager.getAliveNpcsCount(waveId);
            String message = String.format("§e⚔ §f+1 enemigo §7(%d restantes)", remaining);
            killer.sendMessage(message);
        }
    }

    /**
     * Completa la onda actual
     */
    private void completeWave(RaidSession session, String waveId) {
        int waveNumber = session.getCurrentWaveIndex() + 1;
        int totalWaves = session.getRaid().getTotalWaves();

        if (rewardsGiven.contains(waveId)) {
            System.out.println("[Raids] Oleada ya completada anteriormente");
            return;
        }

        rewardsGiven.add(waveId);

        List<Player> players = getActivePlayers(session);

        RaidEffects.waveCompleteEffect(players, waveNumber);

        for (Player player : players) {
            RaidTitleManager.showWaveComplete(player, waveNumber);
            RaidSoundManager.playWaveCompleteSound(player);
        }

        boolean isLastWave = !session.hasNextWave();

        for (Player player : players) {
            player.sendMessage("");
            player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("§a§l  ✓ OLEADA " + waveNumber + "/" + totalWaves + " COMPLETADA ✓");

            if (isLastWave) {
                player.sendMessage("");
                player.sendMessage("§6§l  🎉 ¡ÚLTIMA OLEADA COMPLETADA! 🎉");
                player.sendMessage("§e  ¡Todas las oleadas han sido derrotadas!");
            } else {
                player.sendMessage("");
                player.sendMessage(String.format("§7  Progreso: §e[§a%s§7%s§e] §f%d%%",
                        repeatString("█", waveNumber),
                        repeatString("█", totalWaves - waveNumber),
                        (waveNumber * 100) / totalWaves));
            }

            player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("");
        }

        executeWaveRewardsOnce(session);

        NPCSpawnManager.clearWaveTracking(waveId);

        if (isLastWave) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> {
                completeRaidWithEffects(session);
                rewardsGiven.remove(waveId);
            }, 60L);
        } else {
            startWaveCountdown(session, waveId);
        }
    }

    /**
     * Inicia countdown de 10 segundos
     */
    private void startWaveCountdown(RaidSession session, String previousWaveId) {
        String countdownKey = session.getSessionId() + "_countdown";

        if (activeCountdowns.contains(countdownKey)) {
            return;
        }
        activeCountdowns.add(countdownKey);

        List<Player> players = getActivePlayers(session);
        int nextWaveNumber = session.getCurrentWaveIndex() + 2;

        for (Player player : players) {
            player.sendMessage("");
            player.sendMessage("§6⏳ §fPreparándose para oleada §6" + nextWaveNumber + "§f...");
            player.sendMessage("");
        }

        final int[] countdown = {10};

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, new Runnable() {
            @Override
            public void run() {
                List<Player> currentPlayers = getActivePlayers(session);

                if (countdown[0] > 0) {
                    String countdownMsg = getCountdownMessage(countdown[0]);

                    for (Player player : currentPlayers) {
                        player.sendMessage(countdownMsg);

                        if (countdown[0] <= 5) {
                            RaidSoundManager.playAlertSound(player);
                        }
                    }

                    countdown[0]--;
                } else {
                    for (Player player : currentPlayers) {
                        player.sendMessage("");
                        player.sendMessage("§c§l⚔ ¡OLEADA " + nextWaveNumber + " INICIADA! ⚔");
                        player.sendMessage("");
                        RaidSoundManager.playWaveStartSound(player);
                    }

                    session.moveToNextWave();
                    Wave nextWave = session.getCurrentWave();

                    if (nextWave != null) {
                        for (SpawnPoint sp : nextWave.getSpawnPoints()) {
                            sp.resetAliveCount();
                        }

                        nextWave.setStatus(WaveStatus.ACTIVE);

                        String newWaveId = getWaveId(session);
                        boolean spawned = NPCSpawnManager.spawnWaveNpcs(nextWave, newWaveId);

                        if (spawned) {
                            System.out.println("[Raids] " + NPCSpawnManager.getDebugInfo(newWaveId));

                            RaidEffects.waveActiveEffect(currentPlayers,
                                    session.getCurrentWaveIndex() + 1,
                                    session.getRaid().getTotalWaves());

                            for (Player player : currentPlayers) {
                                RaidTitleManager.showWaveStart(player,
                                        session.getCurrentWaveIndex() + 1,
                                        session.getRaid().getTotalWaves());
                            }
                        } else {
                            System.err.println("[Raids] Error al spawnear oleada " + nextWaveNumber);
                        }
                    }

                    activeCountdowns.remove(countdownKey);
                    rewardsGiven.remove(previousWaveId);
                }
            }
        }, 20L, 20L);
    }

    /**
     * Mensaje de countdown
     */
    private String getCountdownMessage(int seconds) {
        if (seconds > 5) {
            return String.format("§e⏳ §fSiguiente oleada en §e%d §fsegundos...", seconds);
        } else if (seconds > 1) {
            return String.format("§6⏳ §f¡Oleada en §6%d§f!", seconds);
        } else {
            return "§c⏳ §f¡§c1§f!";
        }
    }

    /**
     * Completa la raid con victoria
     */
    private void completeRaidWithEffects(RaidSession session) {
        String raidName = session.getRaid().getRaidName();
        List<Player> players = getActivePlayers(session);

        RaidEffects.raidVictoryEffect(players, raidName);

        for (Player player : players) {
            RaidSoundManager.playVictorySound(player);
            RaidTitleManager.showVictory(player, raidName);
        }

        long duration = session.getDurationSeconds();
        long minutes = duration / 60;
        long seconds = duration % 60;

        for (Player player : players) {
            player.sendMessage("");
            player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("§6§l         🏆 ¡RAID COMPLETADA! 🏆");
            player.sendMessage("");
            player.sendMessage(String.format("§e  Raid: §f%s", raidName));
            player.sendMessage(String.format("§e  Oleadas: §a%d/%d §fcompletadas §l✓",
                    session.getRaid().getTotalWaves(),
                    session.getRaid().getTotalWaves()));
            player.sendMessage(String.format("§e  Tiempo: §f%dm %ds", minutes, seconds));
            player.sendMessage(String.format("§e  Jugadores: §f%d sobrevivientes", players.size()));
            player.sendMessage("");
            player.sendMessage("§a  ✓ Todas las recompensas han sido otorgadas");
            player.sendMessage("§7  Regresando al spawn...");
            player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("");
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, () -> {
            for (Player player : players) {
                player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.MOBSPAWNER_FLAMES, 0);
                RaidSoundManager.playBuffSound(player);
            }
        }, 40L);

        RaidSessionManager.completeRaid(session);
    }

    /**
     * Actualiza progreso de oleada
     */
    private void updateWaveProgress(RaidSession session, int enemiesRemaining) {
        String waveId = getWaveId(session);
        Wave wave = session.getCurrentWave();

        if (wave == null) {
            return;
        }

        int totalEnemies = wave.getTotalEnemies();
        int enemiesKilled = totalEnemies - enemiesRemaining;
        int waveNumber = session.getCurrentWaveIndex() + 1;
        int totalWaves = session.getRaid().getTotalWaves();

        int waveProgress = (enemiesKilled * 100) / Math.max(1, totalEnemies);
        String progressBar = createProgressBar(waveProgress, 20);

        for (UUID playerId : session.getActivePlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                String progressMessage = String.format(
                        "§6Oleada %d/%d §8| §7Enemigos: %s §f%d/%d",
                        waveNumber, totalWaves, progressBar, enemiesKilled, totalEnemies
                );

                player.sendMessage(progressMessage);

                if (enemiesRemaining <= 5 && enemiesRemaining > 0) {
                    if (enemiesRemaining == 5 || enemiesRemaining == 3 || enemiesRemaining == 1) {
                        player.sendMessage(String.format("§c⚠ §f¡Solo quedan §c%d §fenemigos!", enemiesRemaining));
                        RaidSoundManager.playAttackSound(player);
                    }
                }
            }
        }
    }

    /**
     * Ejecuta recompensas UNA VEZ
     */
    private void executeWaveRewardsOnce(RaidSession session) {
        Wave wave = session.getCurrentWave();
        if (wave == null || !wave.hasRewards()) {
            return;
        }

        for (org.debentialc.raids.models.WaveReward reward : wave.getRewards()) {
            boolean shouldGive = reward.shouldExecute();

            if (shouldGive) {
                for (UUID playerId : session.getActivePlayers()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        String command = reward.getCommand()
                                .replace("@p", player.getName())
                                .replace("@s", player.getName())
                                .replace("{player}", player.getName());

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                        RaidSoundManager.playBuffSound(player);
                        player.getWorld().playEffect(player.getLocation(),
                                org.bukkit.Effect.MOBSPAWNER_FLAMES, 0);

                        String rewardName = getRewardDisplayName(reward.getCommand());
                        player.sendMessage("");
                        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                        player.sendMessage("§6§l  ✦ RECOMPENSA ✦");
                        player.sendMessage("§f  " + rewardName);
                        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                        player.sendMessage("");
                    }
                }
            }
        }
    }

    private String getRewardDisplayName(String command) {
        if (command.length() > 40) {
            return command.substring(0, 37) + "...";
        }

        if (command.startsWith("give")) {
            return "§a✓ §fRecompensa otorgada";
        } else if (command.startsWith("eco") || command.startsWith("money")) {
            return "§6✓ §fDinero otorgado";
        } else if (command.startsWith("xp")) {
            return "§b✓ §fExperiencia otorgada";
        }

        return "§e✓ §f" + command;
    }

    private List<Player> getActivePlayers(RaidSession session) {
        List<Player> players = new ArrayList<>();
        for (UUID playerId : session.getActivePlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private String createProgressBar(int percentage, int length) {
        int filled = (percentage * length) / 100;

        StringBuilder bar = new StringBuilder();
        bar.append("§a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }

        bar.append("§7");
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }

        return bar.toString();
    }

    private String repeatString(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private String getWaveId(RaidSession session) {
        return session.getSessionId() + "_wave_" + session.getCurrentWaveIndex();
    }

    public static void clearSessionTracking(String sessionId) {
        rewardsGiven.removeIf(id -> id.startsWith(sessionId));
        activeCountdowns.removeIf(id -> id.startsWith(sessionId));
    }
}