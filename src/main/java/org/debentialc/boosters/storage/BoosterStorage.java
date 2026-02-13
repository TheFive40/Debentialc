package org.debentialc.boosters.storage;

import com.google.gson.*;
import org.debentialc.boosters.managers.GlobalBoosterManager;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.boosters.models.GlobalBooster;
import org.debentialc.boosters.models.PersonalBooster;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class BoosterStorage {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String BASE_PATH = "plugins/Debentialc/data/boosters";
    private static final String GLOBAL_FILE = "global.json";
    private static final String PERSONAL_DIR = "personal";

    static {
        try {
            Files.createDirectories(Paths.get(BASE_PATH));
            Files.createDirectories(Paths.get(BASE_PATH, PERSONAL_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveGlobalBooster(GlobalBooster booster) {
        try {
            Path filePath = Paths.get(BASE_PATH, GLOBAL_FILE);
            String json = GSON.toJson(booster);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GlobalBooster loadGlobalBooster() {
        try {
            Path filePath = Paths.get(BASE_PATH, GLOBAL_FILE);
            if (Files.exists(filePath)) {
                String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                return GSON.fromJson(json, GlobalBooster.class);
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void savePersonalBoosters(UUID playerId, List<PersonalBooster> boosters) {
        try {
            Path playerDir = Paths.get(BASE_PATH, PERSONAL_DIR, playerId.toString());
            Files.createDirectories(playerDir);

            Path filePath = playerDir.resolve("boosters.json");
            String json = GSON.toJson(boosters);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<PersonalBooster> loadPersonalBoosters(UUID playerId) {
        try {
            Path filePath = Paths.get(BASE_PATH, PERSONAL_DIR, playerId.toString(), "boosters.json");
            if (Files.exists(filePath)) {
                String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                PersonalBooster[] boosters = GSON.fromJson(json, PersonalBooster[].class);
                return Arrays.asList(boosters);
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }



    public static void loadAllData() {
        GlobalBooster global = loadGlobalBooster();

        if (global != null && !global.hasExpired()) {
            GlobalBoosterManager.restoreBooster(global);

            System.out.println("[Boosters] Booster global restaurado");
            System.out.println("  Multiplicador: " + global.getMultiplier());
            System.out.println("  Tiempo restante: " + global.getFormattedTime());
            System.out.println("  Activado por: " + global.getActivatedBy());
        } else {
            System.out.println("[Boosters] No hay booster global guardado o ha expirado");
        }
    }

    public static void saveAllData() {
        GlobalBooster active = GlobalBoosterManager.getActiveBooster();
        if (active != null) {
            saveGlobalBooster(active);
        }
    }

    public static void deletePersonalBoosters(UUID playerId) {
        try {
            Path playerDir = Paths.get(BASE_PATH, PERSONAL_DIR, playerId.toString());
            if (Files.exists(playerDir)) {
                Files.walk(playerDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}