package org.example.tools.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.example.Main;
import org.example.tools.ci.CustomItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomItemStorage {
    private File dataFolder;
    private File itemsFile;
    private FileConfiguration itemsConfig;

    public CustomItemStorage() {
        this.dataFolder = new File(Main.instance.getDataFolder(), "items");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.itemsFile = new File(dataFolder, "custom_items.yml");
        loadItems();
    }

    public void loadItems() {
        if (!itemsFile.exists()) {
            try {
                itemsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public void saveItem(CustomItem item) {
        String path = "items." + item.getId();
        itemsConfig.set(path + ".id", item.getId());
        itemsConfig.set(path + ".material", item.getMaterial());
        itemsConfig.set(path + ".displayName", item.getDisplayName());
        itemsConfig.set(path + ".lore", item.getLore());
        itemsConfig.set(path + ".isActive", item.isActive());
        itemsConfig.set(path + ".bonusStat", new HashMap<>(item.getValueByStat()));
        itemsConfig.set(path + ".operations", new HashMap<>(item.getOperation()));
        itemsConfig.set(path + ".effects", new HashMap<>(item.getEffects()));

        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(String id) {
        itemsConfig.set("items." + id, null);
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CustomItem loadItem(String id) {
        String path = "items." + id;
        if (!itemsConfig.contains(path)) {
            return null;
        }

        CustomItem item = new CustomItem();
        item.setId(id);
        item.setMaterial(itemsConfig.getInt(path + ".material"));
        item.setDisplayName(itemsConfig.getString(path + ".displayName"));
        item.setLore(itemsConfig.getStringList(path + ".lore"));
        item.setActive(itemsConfig.getBoolean(path + ".isActive", true));

        // Cargar bonificaciones
        if (itemsConfig.contains(path + ".bonusStat")) {
            HashMap<String, Double> bonusStat = new HashMap<>();
            for (String key : itemsConfig.getConfigurationSection(path + ".bonusStat").getKeys(false)) {
                bonusStat.put(key, itemsConfig.getDouble(path + ".bonusStat." + key));
            }
            item.setValueByStat(bonusStat);
        }

        // Cargar operaciones
        if (itemsConfig.contains(path + ".operations")) {
            HashMap<String, String> operations = new HashMap<>();
            for (String key : itemsConfig.getConfigurationSection(path + ".operations").getKeys(false)) {
                operations.put(key, itemsConfig.getString(path + ".operations." + key));
            }
            item.setOperation(operations);
        }

        // Cargar efectos
        if (itemsConfig.contains(path + ".effects")) {
            HashMap<String, Double> effects = new HashMap<>();
            for (String key : itemsConfig.getConfigurationSection(path + ".effects").getKeys(false)) {
                effects.put(key, itemsConfig.getDouble(path + ".effects." + key));
            }
            item.setEffects(effects);
        }

        return item;
    }

    public Map<String, CustomItem> loadAllItems() {
        Map<String, CustomItem> items = new HashMap<>();

        if (!itemsConfig.contains("items")) {
            return items;
        }

        for (String id : itemsConfig.getConfigurationSection("items").getKeys(false)) {
            CustomItem item = loadItem(id);
            if (item != null) {
                items.put(id, item);
            }
        }

        return items;
    }

    public boolean itemExists(String id) {
        return itemsConfig.contains("items." + id);
    }

    public Set<String> getAllItemIds() {
        if (!itemsConfig.contains("items")) {
            return new HashSet<>();
        }
        return itemsConfig.getConfigurationSection("items").getKeys(false);
    }
}