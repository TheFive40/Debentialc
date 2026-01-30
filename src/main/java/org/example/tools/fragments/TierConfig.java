package org.example.tools.fragments;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.example.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la configuración de tiers y límites de atributos
 */
public class TierConfig {
    private File configFile;
    private FileConfiguration config;
    private Map<String, Map<String, Integer>> tierLimits;

    public TierConfig() {
        this.tierLimits = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        File dataFolder = new File(Main.instance.getDataFolder(), "fragments");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        configFile = new File(dataFolder, "tier_config.yml");

        if (!configFile.exists()) {
            createDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        loadTierLimits();
    }

    private void createDefaultConfig() {
        try {
            configFile.createNewFile();
            config = YamlConfiguration.loadConfiguration(configFile);

            // Tier 1 - Básico
            config.set("tiers.TIER_1.STR", 10);
            config.set("tiers.TIER_1.CON", 10);
            config.set("tiers.TIER_1.DEX", 10);
            config.set("tiers.TIER_1.WIL", 10);
            config.set("tiers.TIER_1.MND", 10);
            config.set("tiers.TIER_1.SPI", 10);

            // Tier 2 - Intermedio
            config.set("tiers.TIER_2.STR", 20);
            config.set("tiers.TIER_2.CON", 20);
            config.set("tiers.TIER_2.DEX", 20);
            config.set("tiers.TIER_2.WIL", 20);
            config.set("tiers.TIER_2.MND", 20);
            config.set("tiers.TIER_2.SPI", 20);

            // Tier 3 - Avanzado
            config.set("tiers.TIER_3.STR", 30);
            config.set("tiers.TIER_3.CON", 30);
            config.set("tiers.TIER_3.DEX", 30);
            config.set("tiers.TIER_3.WIL", 30);
            config.set("tiers.TIER_3.MND", 30);
            config.set("tiers.TIER_3.SPI", 30);

            // Tier VIP - Premium
            config.set("tiers.VIP.STR", 50);
            config.set("tiers.VIP.CON", 50);
            config.set("tiers.VIP.DEX", 50);
            config.set("tiers.VIP.WIL", 50);
            config.set("tiers.VIP.MND", 50);
            config.set("tiers.VIP.SPI", 50);

            // Tier por defecto para armaduras vanilla
            config.set("default_tier", "TIER_1");

            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTierLimits() {
        if (!config.contains("tiers")) return;

        for (String tier : config.getConfigurationSection("tiers").getKeys(false)) {
            Map<String, Integer> limits = new HashMap<>();

            for (String attr : config.getConfigurationSection("tiers." + tier).getKeys(false)) {
                limits.put(attr, config.getInt("tiers." + tier + "." + attr));
            }

            tierLimits.put(tier, limits);
        }
    }

    /**
     * Obtiene el límite de un atributo para un tier
     */
    public int getLimit(String tier, String attribute) {
        if (!tierLimits.containsKey(tier)) {
            return 0;
        }

        Map<String, Integer> limits = tierLimits.get(tier);
        return limits.getOrDefault(attribute, 0);
    }

    /**
     * Verifica si se puede aplicar un valor a un atributo
     */
    public boolean canApply(String tier, String attribute, int currentValue, int valueToAdd) {
        int limit = getLimit(tier, attribute);
        return (currentValue + valueToAdd) <= limit;
    }

    /**
     * Obtiene el tier por defecto
     */
    public String getDefaultTier() {
        return config.getString("default_tier", "TIER_1");
    }

    /**
     * Establece el tier por defecto
     */
    public boolean setDefaultTier(String tier) {
        // Verificar que el tier existe
        if (!tierLimits.containsKey(tier)) {
            return false;
        }

        config.set("default_tier", tier);

        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los tiers disponibles
     */
    public Map<String, Map<String, Integer>> getAllTiers() {
        return new HashMap<>(tierLimits);
    }

    /**
     * Establece el límite de un atributo para un tier
     * @param tier Nombre del tier
     * @param attribute Nombre del atributo
     * @param limit Nuevo límite
     * @return true si se actualizó correctamente
     */
    public boolean setLimit(String tier, String attribute, int limit) {
        // Si el tier no existe, crearlo
        if (!tierLimits.containsKey(tier)) {
            tierLimits.put(tier, new HashMap<>());
        }

        // Actualizar en memoria
        Map<String, Integer> limits = tierLimits.get(tier);
        limits.put(attribute, limit);
        tierLimits.put(tier, limits);

        // Actualizar en archivo
        config.set("tiers." + tier + "." + attribute, limit);

        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un tier completo
     * @param tier Nombre del tier a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteTier(String tier) {
        // Verificar que el tier existe
        if (!tierLimits.containsKey(tier)) {
            return false;
        }

        // No permitir eliminar el tier por defecto
        if (tier.equals(getDefaultTier())) {
            return false;
        }

        // Eliminar de memoria
        tierLimits.remove(tier);

        // Eliminar del archivo
        config.set("tiers." + tier, null);

        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crea un nuevo tier con límites por defecto
     * @param tierName Nombre del nuevo tier
     * @param defaultLimit Límite por defecto para todos los atributos
     * @return true si se creó correctamente
     */
    public boolean createTier(String tierName, int defaultLimit) {
        // Verificar que el tier no existe
        if (tierLimits.containsKey(tierName)) {
            return false;
        }

        // Crear tier con límites por defecto
        String[] attributes = {"STR", "CON", "DEX", "WIL", "MND", "SPI"};
        Map<String, Integer> limits = new HashMap<>();

        for (String attr : attributes) {
            limits.put(attr, defaultLimit);
            config.set("tiers." + tierName + "." + attr, defaultLimit);
        }

        tierLimits.put(tierName, limits);

        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recarga la configuración
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        tierLimits.clear();
        loadTierLimits();
    }
}