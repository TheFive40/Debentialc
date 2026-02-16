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

public class NPCSpawnManager {

    private static final Map<String, Set<Integer>> spawnedNpcIds = new ConcurrentHashMap<>();
    private static final Map<String, List<ICustomNpc>> spawnedNpcRefs = new ConcurrentHashMap<>();

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
                System.out.println("[Raids] ADVERTENCIA: SpawnPoint sin ubicación válida para NPC: "
                        + spawnPoint.getNpcName());
                continue;
            }

            for (int i = 0; i < spawnPoint.getQuantity(); i++) {
                ICustomNpc npc = spawnNpc(loc, spawnPoint.getNpcName(), spawnPoint.getNpcTab());
                if (npc != null) {
                    int entityId = npc.getEntityId();
                    npcIds.add(entityId);
                    npcRefs.add(npc);
                    totalSpawned++;
                }
            }
        }

        spawnedNpcIds.put(waveId, npcIds);
        spawnedNpcRefs.put(waveId, npcRefs);

        System.out.println("[Raids] Spawneados " + totalSpawned + " NPCs para waveId: " + waveId);
        return totalSpawned > 0;
    }

    private static ICustomNpc spawnNpc(Location location, String npcName, int npcTab) {
        try {
            World world = location.getWorld();
            AbstractNpcAPI api = NpcAPI.Instance();

            int bx = location.getBlockX();
            int by = location.getBlockY();
            int bz = location.getBlockZ();

            System.out.println(String.format(
                    "[Raids] Spawneando NPC '%s' (tab=%d) en bloque %d,%d,%d (origen: %.2f,%.2f,%.2f)",
                    npcName, npcTab, bx, by, bz,
                    location.getX(), location.getY(), location.getZ()
            ));

            ICustomNpc npc = (ICustomNpc) api.getIWorld(world.getEnvironment().getId()).spawnClone(
                    bx,
                    by,
                    bz,
                    npcTab,
                    npcName
            );

            if (npc != null) {
                npc.setName(npcName);
                return npc;
            } else {
                System.out.println("[Raids] ERROR: spawnClone retornó null para '" + npcName
                        + "' tab=" + npcTab + " en " + bx + "," + by + "," + bz);
            }

        } catch (Exception e) {
            System.out.println("[Raids] ERROR spawneando NPC '" + npcName + "': " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isNpcFromWave(int entityId, String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return false;
        }
        return ids.contains(entityId);
    }

    public static String getWaveIdForNpc(int entityId) {
        for (Map.Entry<String, Set<Integer>> entry : spawnedNpcIds.entrySet()) {
            if (entry.getValue().contains(entityId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static boolean markNpcDead(int entityId, String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return false;
        }

        return ids.remove(entityId);
    }

    public static void despawnWaveNpcs(String waveId) {
        List<ICustomNpc> npcs = spawnedNpcRefs.get(waveId);
        if (npcs == null) {
            return;
        }

        for (ICustomNpc npc : npcs) {
            try {
                if (npc != null && npc.isAlive()) {
                    npc.despawn();
                }
            } catch (Exception e) {
            }
        }

        spawnedNpcRefs.remove(waveId);
        spawnedNpcIds.remove(waveId);
    }

    public static void despawnAllNpcs() {
        for (String waveId : new ArrayList<>(spawnedNpcRefs.keySet())) {
            despawnWaveNpcs(waveId);
        }
    }

    public static int getAliveNpcsCount(String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return 0;
        }
        return ids.size();
    }

    public static boolean areAllNpcsDead(String waveId) {
        return getAliveNpcsCount(waveId) == 0;
    }

    public static void clearWaveTracking(String waveId) {
        spawnedNpcIds.remove(waveId);
        spawnedNpcRefs.remove(waveId);
    }

    public static String getDebugInfo(String waveId) {
        Set<Integer> ids = spawnedNpcIds.get(waveId);
        if (ids == null) {
            return "Wave " + waveId + ": No tracking data";
        }

        return String.format("Wave %s: %d NPCs vivos - IDs: %s",
                waveId, ids.size(), ids.toString());
    }

    public static Set<String> getActiveWaves() {
        return new HashSet<>(spawnedNpcIds.keySet());
    }
}