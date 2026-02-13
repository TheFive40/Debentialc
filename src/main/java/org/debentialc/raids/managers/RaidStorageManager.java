package org.debentialc.raids.managers;

import com.google.gson.*;
import org.debentialc.raids.models.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * RaidStorageManager - Gestor de almacenamiento de raids
 * Responsable de guardar y cargar raids en/desde JSON
 */
public class RaidStorageManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String BASE_PATH = "plugins/Debentialc/data/raids";
    private static final String RAIDS_FILE = "raids.json";
    private static final String COOLDOWNS_FILE = "cooldowns.json";

    static {
        try {
            Files.createDirectories(Paths.get(BASE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Guarda todas las raids en JSON
     */
    public static void saveAllRaids() {
        try {
            Path filePath = Paths.get(BASE_PATH, RAIDS_FILE);
            List<Raid> raidsList = RaidManager.getAllRaids();

            String json = GSON.toJson(raidsList);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));

            System.out.println("[Raids] Raids guardadas: " + raidsList.size());
        } catch (IOException e) {
            System.err.println("[Raids] Error al guardar raids: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga todas las raids desde JSON
     */
    public static void loadAllRaids() {
        try {
            Path filePath = Paths.get(BASE_PATH, RAIDS_FILE);

            if (!Files.exists(filePath)) {
                System.out.println("[Raids] Archivo de raids no encontrado. Se creará al guardar.");
                return;
            }

            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            Raid[] raidsArray = GSON.fromJson(json, Raid[].class);

            RaidManager.clearAllRaids();

            if (raidsArray != null) {
                for (Raid raid : raidsArray) {
                    RaidManager.updateRaid(raid);
                }
                System.out.println("[Raids] Raids cargadas: " + raidsArray.length);
            }

        } catch (IOException | JsonSyntaxException e) {
            System.err.println("[Raids] Error al cargar raids: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Guarda los cooldowns en JSON
     */
    public static void saveCooldowns() {
        try {
            Path filePath = Paths.get(BASE_PATH, COOLDOWNS_FILE);

            Map<String, Map<String, Long>> cooldownsData = new HashMap<>();


            String json = GSON.toJson(cooldownsData);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));

            System.out.println("[Raids] Cooldowns guardados");
        } catch (IOException e) {
            System.err.println("[Raids] Error al guardar cooldowns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga los cooldowns desde JSON
     */
    public static void loadCooldowns() {
        try {
            Path filePath = Paths.get(BASE_PATH, COOLDOWNS_FILE);

            if (!Files.exists(filePath)) {
                System.out.println("[Raids] Archivo de cooldowns no encontrado.");
                return;
            }

            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);

            // TODO: Cargar cooldowns en CooldownManager
            System.out.println("[Raids] Cooldowns cargados");

        } catch (IOException | JsonSyntaxException e) {
            System.err.println("[Raids] Error al cargar cooldowns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Guarda una raid individual
     */
    public static void saveRaid(Raid raid) {
        if (raid == null) {
            return;
        }

        try {
            Path raidDir = Paths.get(BASE_PATH, "raid_" + raid.getRaidId());
            Files.createDirectories(raidDir);

            Path filePath = raidDir.resolve("config.json");
            String json = GSON.toJson(raid);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));

            System.out.println("[Raids] Raid individual guardada: " + raid.getRaidId());
        } catch (IOException e) {
            System.err.println("[Raids] Error al guardar raid individual: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga una raid individual
     */
    public static Raid loadRaid(String raidId) {
        try {
            Path filePath = Paths.get(BASE_PATH, "raid_" + raidId, "config.json");

            if (!Files.exists(filePath)) {
                return null;
            }

            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            return GSON.fromJson(json, Raid.class);

        } catch (IOException | JsonSyntaxException e) {
            System.err.println("[Raids] Error al cargar raid: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Elimina los archivos de una raid
     */
    public static void deleteRaidFiles(String raidId) {
        try {
            Path raidDir = Paths.get(BASE_PATH, "raid_" + raidId);

            if (Files.exists(raidDir)) {
                Files.walk(raidDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("[Raids] Error al eliminar archivo: " + e.getMessage());
                            }
                        });

                System.out.println("[Raids] Archivos de raid eliminados: " + raidId);
            }
        } catch (IOException e) {
            System.err.println("[Raids] Error al eliminar archivos de raid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Guarda datos generales del sistema de raids
     */
    public static void saveRaidSystemData() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("totalRaids", RaidManager.getTotalRaids());
            data.put("totalEnabledRaids", RaidManager.getTotalEnabledRaids());
            data.put("totalParties", PartyManager.getTotalParties());
            data.put("totalPlayersInParties", PartyManager.getTotalPlayersInParties());
            data.put("totalActiveSessions", RaidSessionManager.getTotalActiveSessions());
            data.put("totalPlayersInRaids", RaidSessionManager.getTotalPlayersInRaids());
            data.put("lastSaveTime", System.currentTimeMillis());

            Path filePath = Paths.get(BASE_PATH, "system_data.json");
            String json = GSON.toJson(data);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));

            System.out.println("[Raids] Datos del sistema guardados");
        } catch (IOException e) {
            System.err.println("[Raids] Error al guardar datos del sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga datos generales del sistema de raids
     */
    public static Map<String, Object> loadRaidSystemData() {
        try {
            Path filePath = Paths.get(BASE_PATH, "system_data.json");

            if (!Files.exists(filePath)) {
                return new HashMap<>();
            }

            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            return GSON.fromJson(json, HashMap.class);

        } catch (IOException | JsonSyntaxException e) {
            System.err.println("[Raids] Error al cargar datos del sistema: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Realiza un backup de todas las raids
     */
    public static void backupRaids() {
        try {
            Path source = Paths.get(BASE_PATH);
            Path backup = Paths.get(BASE_PATH, "backup_" + System.currentTimeMillis());

            Files.createDirectories(backup);

            Files.walk(source)
                    .filter(p -> !p.equals(source))
                    .forEach(p -> {
                        try {
                            Path relative = source.relativize(p);
                            Path target = backup.resolve(relative);
                            if (Files.isDirectory(p)) {
                                Files.createDirectories(target);
                            } else {
                                Files.copy(p, target);
                            }
                        } catch (IOException e) {
                            System.err.println("[Raids] Error al hacer backup: " + e.getMessage());
                        }
                    });

            System.out.println("[Raids] Backup realizado: " + backup);
        } catch (IOException e) {
            System.err.println("[Raids] Error al realizar backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Limpia archivos innecesarios
     */
    public static void cleanup() {
        try {
            Path source = Paths.get(BASE_PATH);

            Files.walk(source)
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("raid_"))
                    .forEach(p -> {
                        String raidId = p.getFileName().toString().replace("raid_", "");
                        if (!RaidManager.raidExists(raidId)) {
                            try {
                                Files.walk(p)
                                        .sorted(Comparator.reverseOrder())
                                        .forEach(path -> {
                                            try {
                                                Files.delete(path);
                                            } catch (IOException e) {
                                                System.err.println("[Raids] Error en cleanup: " + e.getMessage());
                                            }
                                        });
                                System.out.println("[Raids] Directorio obsoleto eliminado: " + raidId);
                            } catch (IOException e) {
                                System.err.println("[Raids] Error al limpiar: " + e.getMessage());
                            }
                        }
                    });

        } catch (IOException e) {
            System.err.println("[Raids] Error durante cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}