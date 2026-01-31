package org.debentialc.customitems.tools.scripts;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.debentialc.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la configuración persistente de scripts
 * Guarda metadata como URL de origen, fecha de descarga, etc.
 */
public class ScriptConfig {
    private File configFile;
    private FileConfiguration config;

    public ScriptConfig() {
        File dataFolder = new File(Main.instance.getDataFolder(), "scripts");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        configFile = new File(dataFolder, "scripts.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Guarda metadata de un script
     */
    public void setScriptMetadata(String itemId, String sourceUrl, String fileName) {
        String path = "scripts." + itemId;
        config.set(path + ".sourceUrl", sourceUrl);
        config.set(path + ".fileName", fileName);
        config.set(path + ".downloadDate", System.currentTimeMillis());

        save();
    }

    /**
     * Obtiene metadata de un script
     */
    public ScriptManager.ScriptMetadata getScriptMetadata(String itemId) {
        String path = "scripts." + itemId;

        if (!config.contains(path)) {
            return null;
        }

        String sourceUrl = config.getString(path + ".sourceUrl");
        String fileName = config.getString(path + ".fileName");

        return new ScriptManager.ScriptMetadata(itemId, sourceUrl, fileName);
    }

    /**
     * Elimina metadata de un script
     */
    public void removeScriptMetadata(String itemId) {
        config.set("scripts." + itemId, null);
        save();
    }

    /**
     * Obtiene todos los scripts registrados
     */
    public Map<String, ScriptManager.ScriptMetadata> getAllScripts() {
        Map<String, ScriptManager.ScriptMetadata> scripts = new HashMap<>();

        if (!config.contains("scripts")) {
            return scripts;
        }

        for (String itemId : config.getConfigurationSection("scripts").getKeys(false)) {
            ScriptManager.ScriptMetadata metadata = getScriptMetadata(itemId);
            if (metadata != null) {
                scripts.put(itemId, metadata);
            }
        }

        return scripts;
    }

    /**
     * Guarda la configuración
     */
    private void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recarga la configuración
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}