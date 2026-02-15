package org.debentialc.raids.events;

import noppes.npcs.api.IPos;
import noppes.npcs.api.event.INpcEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.debentialc.Main;
import org.debentialc.raids.managers.RaidSessionManager;
import org.debentialc.raids.managers.RaidSoundManager;
import org.debentialc.raids.managers.RaidTitleManager;
import org.debentialc.raids.models.RaidSession;
import org.debentialc.raids.models.SpawnPoint;
import org.debentialc.raids.models.Wave;
import org.debentialc.raids.models.WaveStatus;

import java.util.Collection;
import java.util.UUID;

/**
 * NPCDeathListener - Escucha muertes de NPCs en CustomNPC
 * Maneja el evento cuando un NPC muere en una raid
 */
public class NPCDeathListener implements Listener {

    public void onNpcDie(INpcEvent.DiedEvent event) {
        IPos pos = event.getNpc().getPosition();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Player player = Main.instance.getServer().getPlayer(UUID.fromString(event.getSource().getUniqueID()));
        World world = player.getWorld();
        Location npcLocation = new Location(world, x, y, z);
        Collection<RaidSession> activeSessions = RaidSessionManager.getAllActiveSessions();

        for (RaidSession session : activeSessions) {
            if (isNpcFromRaid(event, session, npcLocation)) {
                handleNpcDeath(event, session, npcLocation);
                break;
            }
        }
    }

    /**
     * Verifica si un NPC pertenece a una raid específica
     */
    private boolean isNpcFromRaid(INpcEvent.DiedEvent event, RaidSession session, Location npcLocation) {
        if (session == null || session.getCurrentWave() == null) {
            return false;
        }

        Wave wave = session.getCurrentWave();
        for (SpawnPoint spawnPoint : wave.getSpawnPoints()) {
            if (isNpcNearLocation(npcLocation, spawnPoint.getLocation())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica si un NPC está cerca de una ubicación (tolerancia de 2 bloques)
     */
    private boolean isNpcNearLocation(Location npcLoc, Location spawnLoc) {
        if (npcLoc == null || spawnLoc == null) {
            return false;
        }

        double distance = npcLoc.distance(spawnLoc);
        return distance <= 2.0;
    }

    /**
     * Maneja la muerte de un NPC en una raid
     */
    private void handleNpcDeath(INpcEvent.DiedEvent event, RaidSession session, Location npcLocation) {
        Wave wave = session.getCurrentWave();

        if (wave == null) {
            return;
        }

        decrementEnemyCount(wave, npcLocation);

        playDeathEffects(npcLocation, session);

        if (isWaveComplete(wave)) {
            completeWave(session);
        } else {
            updateWaveProgress(session);
        }
    }

    /**
     * Decrementa el contador de enemigos vivos
     */
    private void decrementEnemyCount(Wave wave, Location npcLocation) {
        for (SpawnPoint spawnPoint : wave.getSpawnPoints()) {
            if (isNpcNearLocation(npcLocation, spawnPoint.getLocation())) {
                spawnPoint.decrementAliveCount();
                break;
            }
        }
    }

    /**
     * Verifica si todos los enemigos de la onda fueron derrotados
     */
    private boolean isWaveComplete(Wave wave) {
        for (SpawnPoint spawnPoint : wave.getSpawnPoints()) {
            if (spawnPoint.getRemainingEnemies() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reproduce efectos cuando muere un NPC
     */
    private void playDeathEffects(Location location, RaidSession session) {
        location.getWorld().playEffect(location, org.bukkit.Effect.SMOKE, 0);

        for (java.util.UUID playerId : session.getActivePlayers()) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                RaidSoundManager.playDamageSound(player);
            }
        }
    }

    /**
     * Completa la onda actual
     */
    private void completeWave(RaidSession session) {
        int waveNumber = session.getCurrentWaveIndex() + 1;

        for (java.util.UUID playerId : session.getActivePlayers()) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                RaidTitleManager.showWaveComplete(player, waveNumber);
                RaidSoundManager.playWaveCompleteSound(player);
                player.sendMessage("");
                player.sendMessage(String.format("§a✓ OLEADA %d COMPLETADA ✓", waveNumber));
                player.sendMessage("§7Preparándose para la siguiente...");
                player.sendMessage("");
            }
        }

        executeWaveRewards(session);

        if (session.hasNextWave()) {
            session.moveToNextWave();
            session.getCurrentWave().setStatus(WaveStatus.ACTIVE);

            for (java.util.UUID playerId : session.getActivePlayers()) {
                org.bukkit.entity.Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    RaidTitleManager.showWaveStart(player, session.getCurrentWaveIndex() + 1,
                            session.getRaid().getTotalWaves());
                }
            }
        } else {
            RaidSessionManager.completeRaid(session);
        }
    }

    /**
     * Actualiza el progreso de la onda para todos los jugadores
     */
    private void updateWaveProgress(RaidSession session) {
        Wave wave = session.getCurrentWave();
        if (wave == null) {
            return;
        }

        int enemiesRemaining = 0;
        for (SpawnPoint spawnPoint : wave.getSpawnPoints()) {
            enemiesRemaining += spawnPoint.getRemainingEnemies();
        }

        for (java.util.UUID playerId : session.getActivePlayers()) {
            org.bukkit.entity.Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                RaidTitleManager.showWaveProgress(player,
                        session.getCurrentWaveIndex() + 1,
                        session.getRaid().getTotalWaves(),
                        enemiesRemaining);
            }
        }
    }

    /**
     * Ejecuta las recompensas de la onda
     */
    private void executeWaveRewards(RaidSession session) {
        Wave wave = session.getCurrentWave();
        if (wave == null || !wave.hasRewards()) {
            return;
        }

        for (org.debentialc.raids.models.WaveReward reward : wave.getRewards()) {
            if (reward.shouldExecute()) {
                for (java.util.UUID playerId : session.getActivePlayers()) {
                    org.bukkit.entity.Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        String command = reward.getCommand()
                                .replace("@p", player.getName())
                                .replace("@s", player.getName())
                                .replace("{player}", player.getName());

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                        RaidSoundManager.playBuffSound(player);

                        String rewardName = reward.getCommand();
                        if (rewardName.length() > 30) {
                            rewardName = rewardName.substring(0, 30) + "...";
                        }
                        player.sendMessage(String.format("§a✓ Recompensa: %s", rewardName));
                    }
                }
            }
        }
    }
}