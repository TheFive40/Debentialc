package org.example.tools.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DBCConfigManager {

    private static final Map<String, Map<String, Double>> configCache = new HashMap<>();
    private static final String CONFIG_PATH = "/config/jingames/dbc";

    private static final String[] RACES = {"human", "saiyan", "half-saiyan", "namekian", "arcosian", "majin"};
    private static final String[] CLASSES = {"MartialArtist", "Spiritualist", "Warrior"};

    public static void loadAllConfigs() {
        for (String race : RACES) {
            loadRaceConfig(race);
        }
    }

    private static void loadRaceConfig(String race) {
        File configFile = new File(CONFIG_PATH + "/" + race + "/main.cfg");

        if (!configFile.exists()) {
            System.out.println("[DBC Config] Archivo no encontrado: " + configFile.getAbsolutePath());
            return;
        }

        try {
            String content = new String(Files.readAllBytes(configFile.toPath()));

            for (String dbcClass : CLASSES) {
                String key = race + "_" + dbcClass;

                double bodyMultiplier = extractBodyMultiplier(content, race, dbcClass);
                double energyPoolMultiplier = extractEnergyPoolMultiplier(content, race, dbcClass);
                double staminaMultiplier = extractStaminaMultiplier(content, race, dbcClass);

                Map<String, Double> classConfig = new HashMap<>();
                classConfig.put("Body", bodyMultiplier);
                classConfig.put("EnergyPool", energyPoolMultiplier);
                classConfig.put("Stamina", staminaMultiplier);

                configCache.put(key, classConfig);

                System.out.println("[DBC Config] Cargado: " + key + " | Body: " + bodyMultiplier + " | EnergyPool: " + energyPoolMultiplier + " | Stamina: " + staminaMultiplier);
            }

        } catch (IOException e) {
            System.out.println("[DBC Config] Error al leer " + configFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private static double extractBodyMultiplier(String content, String race, String dbcClass) {
        String raceFormatted = Arrays.stream(race.split("-"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining("-"));
        String pattern = "DBC " + raceFormatted + " " + dbcClass + " Stat Multiplier from Attribute[\\s\\S]*?Body\\s+([0-9.]+)";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);

        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                System.out.println("[DBC Config] Error al parsear Body multiplier para " + race + " " + dbcClass);
            }
        }

        return 20.0;
    }

    private static double extractEnergyPoolMultiplier(String content, String race, String dbcClass) {
        String raceFormatted = Arrays.stream(race.split("-"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining("-"));
        String pattern = "DBC " + raceFormatted + " " + dbcClass + " Stat Multiplier from Attribute[\\s\\S]*?EnergyPool\\s+([0-9.]+)";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);

        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                System.out.println("[DBC Config] Error al parsear EnergyPool multiplier para " + race + " " + dbcClass);
            }
        }

        return 40.0;
    }

    private static double extractStaminaMultiplier(String content, String race, String dbcClass) {
        String raceFormatted = Arrays.stream(race.split("-"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining("-"));
        String pattern = "DBC " + raceFormatted + " " + dbcClass + " Stat Multiplier from Attribute[\\s\\S]*?Stamina\\s+([0-9.]+)";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);

        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                System.out.println("[DBC Config] Error al parsear Stamina multiplier para " + race + " " + dbcClass);
            }
        }

        return 3.5;
    }

    public static double getBodyMultiplier(String race, String dbcClass) {
        String key = race.toLowerCase() + "_" + dbcClass;
        Map<String, Double> config = configCache.getOrDefault(key, new HashMap<>());
        return config.getOrDefault("Body", 20.0);
    }

    public static double getEnergyPoolMultiplier(String race, String dbcClass) {
        String key = race.toLowerCase() + "_" + dbcClass;
        Map<String, Double> config = configCache.getOrDefault(key, new HashMap<>());
        return config.getOrDefault("EnergyPool", 40.0);
    }

    public static double getStaminaMultiplier(String race, String dbcClass) {
        String key = race.toLowerCase() + "_" + dbcClass;
        Map<String, Double> config = configCache.getOrDefault(key, new HashMap<>());
        return config.getOrDefault("Stamina", 3.5);
    }

    public static Map<String, Double> getClassConfig(String race, String dbcClass) {
        String key = race.toLowerCase() + "_" + dbcClass;
        return configCache.getOrDefault(key, new HashMap<>());
    }
}