package org.debentialc.raids.managers;

import noppes.npcs.api.AbstractNpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.scripted.NpcAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.debentialc.raids.models.SpawnPoint;
import org.debentialc.raids.models.Wave;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPCSpawnManager - Gestiona el spawn de NPCs para las raids
 * Usa la API de CustomNPCs para crear los NPCs
 * VERSIÓN CORREGIDA: Trackea NPCs por entity ID para conteo confiable
 */
public class NPCSpawnManager {

    private static final Map<String, Set<Integer>> spawnedNpcIds = new ConcurrentHashMap<>();

    private static final Map<String, List<ICustomNpc>> spawnedNpcRefs = new ConcurrentHashMap<>();

    /**
     * Spawnea todos los NPCs de una oleada
     * @param wave La oleada a spawnear
     * @param waveId ID único de esta wave (para tracking)
     * @return true si se spawnearon correctamente
     */
    public static boolean spawnWaveNpcs(Wave wave, String waveId) {
        if (wave == null || wave.getSpawnPoints().isEmpty()) {
            return false;
        }

        Set<Integer> npcIds = new HashSet<>();
        List<ICustomNpc> npcRefs = new ArrayList<>();

        int totalSpawned = 0;

        for (SpawnPoint spawnPoint : wave.getSpawnPoints()) {
            Location loc = spawnPoint.getLocation();
            if (loc == null || loc.getWorld() == null) {
                continue;
            }

            for (int i = 0; i < spawnPoint.getQuantity(); i++) {
                ICustomNpc npc = spawnNpc(loc, spawnPoint.getNpcName(), spawnPoint.getNpcTab());
                if (npc != null) {
                    int entityId = npc.getEntityId();
                    npcIds.add(entityId);
                    npcRefs.add(npc);
                    totalSpawned++;

                    System.out.println("[Raids] NPC spawneado - ID: " + entityId +
                            ", Nombre: " + spawnPoint.getNpcName());
                }
            }
        }

        spawnedNpcIds.put(waveId, npcIds);
        spawnedNpcRefs.put(waveId, npcRefs);

        System.out.println("[Raids] Spawneados " + totalSpawned + " NPCs para wave " + waveId);
        System.out.println("[Raids] Entity IDs trackeados: " + npcIds);

        return totalSpawned > 0;
    }

    /**
     * Spawnea un NPC individual usando CustomNPCs API
     */
    private static ICustomNpc spawnNpc(Location location, String npcName, int npcTab) {
        try {
            World world = location.getWorld();

            AbstractNpcAPI api = NpcAPI.Instance();


            ICustomNpc npc = (ICustomNpc) api.getIWorld(world.getEnvironment().getId()).spawnClone(
                    (int) location.getX(),
                    (int) location.getY(),
                    (int) location.getZ(),
                    npcTab,
                    npcName
            );

            if (npc != null) {
                // Configurar el NPC
                npc.setName(npcName);

                // Configuraciones adicionales si es necesario
                // npc.getStats().setMaxHealth(100);
                // npc.getStats().setHealth(100);

                return npc;
            }

        } catch (Exception e) {
            System.err.println("[Raids] Error al spawnear NPC: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Verifica si un entity ID pertenece a una oleada específica
     */
    public static boolean isNpcFromWave(int entityId, String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return false;
        }
        return ids.contains(entityId);
    }

    /**
     * Verifica si un entity ID pertenece a CUALQUIER oleada activa
     * @return waveId si pertenece, null si no
     */
    public static String getWaveIdForNpc(int entityId) {
        for (Map.Entry<String, Set<Integer>> entry : spawnedNpcIds.entrySet()) {
            if (entry.getValue().contains(entityId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Marca un NPC como muerto (remueve del tracking)
     */
    public static boolean markNpcDead(int entityId, String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return false;
        }

        boolean removed = ids.remove(entityId);
        if (removed) {
            System.out.println("[Raids] NPC muerto trackeado - ID: " + entityId +
                    ", Restantes: " + ids.size());
        }
        return removed;
    }

    /**
     * Despawnea todos los NPCs de una oleada
     */
    public static void despawnWaveNpcs(String waveId) {
        List<ICustomNpc> npcs = spawnedNpcRefs.get(waveId);
        if (npcs == null) {
            return;
        }

        int despawned = 0;
        for (ICustomNpc npc : npcs) {
            try {
                if (npc != null && !npc.isAlive()) {
                    npc.despawn();
                    despawned++;
                }
            } catch (Exception e) {
                System.err.println("[Raids] Error al despawnear NPC: " + e.getMessage());
            }
        }

        spawnedNpcRefs.remove(waveId);
        spawnedNpcIds.remove(waveId);
        System.out.println("[Raids] Despawneados " + despawned + " NPCs de wave " + waveId);
    }

    /**
     * Despawnea todos los NPCs de todas las oleadas
     */
    public static void despawnAllNpcs() {
        for (String waveId : new ArrayList<>(spawnedNpcRefs.keySet())) {
            despawnWaveNpcs(waveId);
        }
    }

    /**
     * Obtiene el número de NPCs vivos de una oleada
     */
    public static int getAliveNpcsCount(String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return 0;
        }
        return ids.size();
    }

    /**
     * Verifica si todos los NPCs de una oleada están muertos
     */
    public static boolean areAllNpcsDead(String waveId) {
        return getAliveNpcsCount(waveId) == 0;
    }

    /**
     * Limpia el tracking de una wave
     */
    public static void clearWaveTracking(String waveId) {
        spawnedNpcIds.remove(waveId);
        spawnedNpcRefs.remove(waveId);
    }

    /**
     * Obtiene información de debugging
     */
    public static String getDebugInfo(String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return "Wave " + waveId + ": No tracking data";
        }

        return String.format("Wave %s: %d NPCs vivos - IDs: %s",
                waveId, ids.size(), ids.toString());
    }

    /**
     * Lista todos los waves activos
     */
    public static Set<String> getActiveWaves() {
        return new HashSet<>(spawnedNpcIds.keySet());
    }
}