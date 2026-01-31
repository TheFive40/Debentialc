package org.debentialc.customitems.tools.fragments;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.debentialc.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gestiona el almacenamiento persistente de armaduras personalizadas
 */
public class CustomArmorStorage {
    private File dataFolder;
    private File armorFile;
    private FileConfiguration armorConfig;
    private Set<String> registeredHashes;

    public CustomArmorStorage() {
        this.registeredHashes = new HashSet<>();
        loadStorage();
    }

    private void loadStorage() {
        dataFolder = new File(Main.instance.getDataFolder(), "fragments");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        armorFile = new File(dataFolder, "customized_armors.yml");

        if (!armorFile.exists()) {
            try {
                armorFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        armorConfig = YamlConfiguration.loadConfiguration(armorFile);
        loadHashes();
    }

    private void loadHashes() {
        if (!armorConfig.contains("armors")) return;

        for (String hash : armorConfig.getConfigurationSection("armors").getKeys(false)) {
            registeredHashes.add(hash);
        }
    }

    /**
     * Guarda una armadura personalizada
     */
    public void saveArmor(CustomizedArmor armor) {
        String path = "armors." + armor.getHash();

        armorConfig.set(path + ".hash", armor.getHash());
        armorConfig.set(path + ".tier", armor.getTier());
        armorConfig.set(path + ".materialType", armor.getMaterialType());
        armorConfig.set(path + ".armorSlot", armor.getArmorSlot());
        armorConfig.set(path + ".displayName", armor.getDisplayName());

        // Guardar atributos
        for (Map.Entry<String, Integer> entry : armor.getAttributes().entrySet()) {
            armorConfig.set(path + ".attributes." + entry.getKey(), entry.getValue());
        }

        armorConfig.set(path + ".timestamp", System.currentTimeMillis());

        try {
            armorConfig.save(armorFile);
            registeredHashes.add(armor.getHash());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga una armadura por su hash
     */
    public CustomizedArmor loadArmor(String hash) {
        String path = "armors." + hash;

        if (!armorConfig.contains(path)) {
            return null;
        }

        CustomizedArmor armor = new CustomizedArmor(
                armorConfig.getString(path + ".hash"),
                armorConfig.getString(path + ".tier")
        );

        armor.setMaterialType(armorConfig.getInt(path + ".materialType"));
        armor.setArmorSlot(armorConfig.getString(path + ".armorSlot"));
        armor.setDisplayName(armorConfig.getString(path + ".displayName"));

        // Cargar atributos
        if (armorConfig.contains(path + ".attributes")) {
            for (String attr : armorConfig.getConfigurationSection(path + ".attributes").getKeys(false)) {
                int value = armorConfig.getInt(path + ".attributes." + attr);
                armor.getAttributes().put(attr, value);
            }
        }

        return armor;
    }

    /**
     * Verifica si un hash ya está registrado
     */
    public boolean hashExists(String hash) {
        return registeredHashes.contains(hash);
    }

    /**
     * Obtiene todos los hashes registrados
     */
    public Set<String> getRegisteredHashes() {
        return new HashSet<>(registeredHashes);
    }

    /**
     * Elimina una armadura del registro
     */
    public void deleteArmor(String hash) {
        armorConfig.set("armors." + hash, null);

        try {
            armorConfig.save(armorFile);
            registeredHashes.remove(hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene todas las armaduras registradas
     */
    public Map<String, CustomizedArmor> loadAllArmors() {
        Map<String, CustomizedArmor> armors = new HashMap<>();

        if (!armorConfig.contains("armors")) {
            return armors;
        }

        for (String hash : armorConfig.getConfigurationSection("armors").getKeys(false)) {
            CustomizedArmor armor = loadArmor(hash);
            if (armor != null) {
                armors.put(hash, armor);
            }
        }

        return armors;
    }

    /**
     * Recarga el almacenamiento desde disco
     */
    public void reload() {
        armorConfig = YamlConfiguration.loadConfiguration(armorFile);
        registeredHashes.clear();
        loadHashes();
    }

    /**
     * Obtiene estadísticas del almacenamiento
     */
    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_armors", registeredHashes.size());

        if (!armorConfig.contains("armors")) {
            return stats;
        }

        Map<String, Integer> tierCount = new HashMap<>();

        for (String hash : armorConfig.getConfigurationSection("armors").getKeys(false)) {
            String tier = armorConfig.getString("armors." + hash + ".tier");
            tierCount.put(tier, tierCount.getOrDefault(tier, 0) + 1);
        }

        stats.putAll(tierCount);
        return stats;
    }
}