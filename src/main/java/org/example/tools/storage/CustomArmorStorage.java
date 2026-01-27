package org.example.tools.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.example.Main;
import org.example.tools.ci.CustomArmor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomArmorStorage {
    private File dataFolder;
    private File armorFile;
    private FileConfiguration armorConfig;

    public CustomArmorStorage() {
        this.dataFolder = new File(Main.instance.getDataFolder(), "armors");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.armorFile = new File(dataFolder, "custom_armors.yml");
        loadArmors();
    }

    public void loadArmors() {
        if (!armorFile.exists()) {
            try {
                armorFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.armorConfig = YamlConfiguration.loadConfiguration(armorFile);
    }

    public void saveArmor(CustomArmor armor) {
        String path = "armors." + armor.getId();
        armorConfig.set(path + ".id", armor.getId());
        armorConfig.set(path + ".material", armor.getMaterial());
        armorConfig.set(path + ".displayName", armor.getDisplayName());
        armorConfig.set(path + ".lore", armor.getLore());
        armorConfig.set(path + ".isArmor", armor.isArmor());
        armorConfig.set(path + ".bonusStat", new HashMap<>(armor.getValueByStat()));
        armorConfig.set(path + ".operations", new HashMap<>(armor.getOperation()));
        armorConfig.set(path + ".effects", new HashMap<>(armor.getEffects())); // AÑADIDO

        try {
            armorConfig.save(armorFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteArmor(String id) {
        armorConfig.set("armors." + id, null);
        try {
            armorConfig.save(armorFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CustomArmor loadArmor(String id) {
        String path = "armors." + id;
        if (!armorConfig.contains(path)) {
            return null;
        }

        CustomArmor armor = new CustomArmor();
        armor.setId(id);
        armor.setMaterial(armorConfig.getInt(path + ".material"));
        armor.setDisplayName(armorConfig.getString(path + ".displayName"));
        armor.setLore(armorConfig.getStringList(path + ".lore"));

        if (armorConfig.contains(path + ".bonusStat")) {
            HashMap<String, Double> bonusStat = new HashMap<>();
            for (String key : armorConfig.getConfigurationSection(path + ".bonusStat").getKeys(false)) {
                bonusStat.put(key, armorConfig.getDouble(path + ".bonusStat." + key));
            }
            armor.setValueByStat(bonusStat);
        }

        if (armorConfig.contains(path + ".operations")) {
            HashMap<String, String> operations = new HashMap<>();
            for (String key : armorConfig.getConfigurationSection(path + ".operations").getKeys(false)) {
                operations.put(key, armorConfig.getString(path + ".operations." + key));
            }
            armor.setOperation(operations);
        }

        // AÑADIDO: Cargar efectos
        if (armorConfig.contains(path + ".effects")) {
            HashMap<String, Double> effects = new HashMap<>();
            for (String key : armorConfig.getConfigurationSection(path + ".effects").getKeys(false)) {
                effects.put(key, armorConfig.getDouble(path + ".effects." + key));
            }
            armor.setEffects(effects);
        }

        return armor;
    }

    public Map<String, CustomArmor> loadAllArmors() {
        Map<String, CustomArmor> armors = new HashMap<>();

        if (!armorConfig.contains("armors")) {
            return armors;
        }

        for (String id : armorConfig.getConfigurationSection("armors").getKeys(false)) {
            CustomArmor armor = loadArmor(id);
            if (armor != null) {
                armors.put(id, armor);
            }
        }

        return armors;
    }
}