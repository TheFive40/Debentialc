package org.example.tools.fragments;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.example.Main;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gestiona la configuración de tiers y límites de atributos
 * VERSIÓN CORREGIDA:
 * - Validación correcta de porcentajes (115 guardado = 15% real)
 * - Soporte para operaciones permitidas por tier
 */
public class TierConfig {
    private File configFile;
    private FileConfiguration config;
    private Map<String, Map<String, Integer>> tierLimits;
    private Map<String, List<String>> tierAllowedOperations; // Operaciones permitidas por tier

    public TierConfig() {
        this.tierLimits = new HashMap<>();
        this.tierAllowedOperations = new HashMap<>();
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
        loadTierOperations();
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
            // POR DEFECTO: Todas las operaciones permitidas
            config.set("tiers.TIER_1.allowed_operations", Arrays.asList("+", "-", "*"));

            // Tier 2 - Intermedio
            config.set("tiers.TIER_2.STR", 20);
            config.set("tiers.TIER_2.CON", 20);
            config.set("tiers.TIER_2.DEX", 20);
            config.set("tiers.TIER_2.WIL", 20);
            config.set("tiers.TIER_2.MND", 20);
            config.set("tiers.TIER_2.SPI", 20);
            config.set("tiers.TIER_2.allowed_operations", Arrays.asList("+", "-", "*"));

            // Tier 3 - Avanzado
            config.set("tiers.TIER_3.STR", 30);
            config.set("tiers.TIER_3.CON", 30);
            config.set("tiers.TIER_3.DEX", 30);
            config.set("tiers.TIER_3.WIL", 30);
            config.set("tiers.TIER_3.MND", 30);
            config.set("tiers.TIER_3.SPI", 30);
            config.set("tiers.TIER_3.allowed_operations", Arrays.asList("+", "-", "*"));

            // Tier VIP - Premium
            config.set("tiers.VIP.STR", 50);
            config.set("tiers.VIP.CON", 50);
            config.set("tiers.VIP.DEX", 50);
            config.set("tiers.VIP.WIL", 50);
            config.set("tiers.VIP.MND", 50);
            config.set("tiers.VIP.SPI", 50);
            config.set("tiers.VIP.allowed_operations", Arrays.asList("+", "-", "*"));

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
                // Saltar la key de operaciones permitidas
                if (attr.equals("allowed_operations")) continue;

                limits.put(attr, config.getInt("tiers." + tier + "." + attr));
            }

            tierLimits.put(tier, limits);
        }
    }

    private void loadTierOperations() {
        if (!config.contains("tiers")) return;

        for (String tier : config.getConfigurationSection("tiers").getKeys(false)) {
            List<String> operations = config.getStringList("tiers." + tier + ".allowed_operations");

            // Si no hay operaciones definidas, permitir todas por defecto
            if (operations.isEmpty()) {
                operations = Arrays.asList("+", "-", "*");
            }

            tierAllowedOperations.put(tier, operations);
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
     * MÉTODO CORREGIDO: Valida si un valor puede ser aplicado considerando la operación
     *
     * IMPORTANTE: Para operaciones multiplicativas (*), el valor guardado está escalado x100
     * Ejemplo: 115 guardado = multiplicador 1.15 = 15% real
     *
     * @param tier Tier a validar
     * @param attribute Atributo (STR, CON, etc)
     * @param currentValue Valor actual GUARDADO (puede ser escalado si es *)
     * @param valueToAdd Valor a AGREGAR (puede ser escalado si es *)
     * @param operation Operación del atributo ("+", "-", "*")
     * @return true si no excede el límite
     */
    public boolean canApply(String tier, String attribute, int currentValue, int valueToAdd, String operation) {
        int limit = getLimit(tier, attribute);

        if (operation == null || !operation.equals("*")) {
            // Operaciones aditivas/sustractivas: validar directamente
            return (currentValue + valueToAdd) <= limit;
        } else {
            // Operación multiplicativa: valores están escalados x100
            // Convertir a porcentaje real para comparar con el límite
            int totalScaled = currentValue + valueToAdd;

            // 115 escalado = 1.15 multiplicador = 15% real
            double percentageReal = (totalScaled / 100.0 - 1.0) * 100.0;

            // El límite también está en porcentaje, así que comparar directamente
            // Ej: límite 20 = 20%, totalScaled 115 = 15% → OK
            return Math.abs(percentageReal) <= limit;
        }
    }

    /**
     * MÉTODO CORREGIDO: Verifica si un valor actual excede el límite de un tier
     *
     * @param tier Tier a validar
     * @param attribute Atributo
     * @param currentValue Valor actual GUARDADO
     * @param operation Operación ("+" , "-", "*")
     * @return true si excede el límite
     */
    public boolean exceedsLimit(String tier, String attribute, int currentValue, String operation) {
        int limit = getLimit(tier, attribute);

        if (operation == null || !operation.equals("*")) {
            // Aditivo/Sustractivo: comparar directamente
            return currentValue > limit;
        } else {
            // Multiplicativo: convertir a porcentaje real
            double percentageReal = Math.abs((currentValue / 100.0 - 1.0) * 100.0);
            return percentageReal > limit;
        }
    }

    /**
     * Verifica si una operación está permitida en un tier
     *
     * @param tier Nombre del tier
     * @param operation Operación a verificar ("+", "-", "*")
     * @return true si está permitida
     */
    public boolean isOperationAllowed(String tier, String operation) {
        List<String> allowed = tierAllowedOperations.getOrDefault(tier, Arrays.asList("+", "-", "*"));
        return allowed.contains(operation);
    }

    /**
     * Obtiene las operaciones permitidas para un tier
     *
     * @param tier Nombre del tier
     * @return Lista de operaciones permitidas
     */
    public List<String> getAllowedOperations(String tier) {
        return new ArrayList<>(tierAllowedOperations.getOrDefault(tier, Arrays.asList("+", "-", "*")));
    }

    /**
     * Establece las operaciones permitidas para un tier
     *
     * @param tier Nombre del tier
     * @param operations Lista de operaciones ("+", "-", "*")
     * @return true si se guardó correctamente
     */
    public boolean setAllowedOperations(String tier, List<String> operations) {
        // Validar tier
        if (!tierLimits.containsKey(tier)) {
            return false;
        }

        // Validar operaciones
        for (String op : operations) {
            if (!op.equals("+") && !op.equals("-") && !op.equals("*")) {
                return false;
            }
        }

        // Actualizar en memoria
        tierAllowedOperations.put(tier, new ArrayList<>(operations));

        // Actualizar en archivo
        config.set("tiers." + tier + ".allowed_operations", operations);

        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
            // Agregar operaciones por defecto
            tierAllowedOperations.put(tier, Arrays.asList("+", "-", "*"));
            config.set("tiers." + tier + ".allowed_operations", Arrays.asList("+", "-", "*"));
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
        tierAllowedOperations.remove(tier);

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

        // Agregar operaciones permitidas por defecto
        config.set("tiers." + tierName + ".allowed_operations", Arrays.asList("+", "-", "*"));

        tierLimits.put(tierName, limits);
        tierAllowedOperations.put(tierName, Arrays.asList("+", "-", "*"));

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
        tierAllowedOperations.clear();
        loadTierLimits();
        loadTierOperations();
    }
}