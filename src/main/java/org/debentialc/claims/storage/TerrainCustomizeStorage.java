package org.debentialc.claims.storage;

import org.debentialc.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Persiste las personalizaciones de terrenos (reglas, efectos, tiempo, clima)
 * en un único archivo claims/customize.properties.
 */
public class TerrainCustomizeStorage {

    private static final String FILENAME = "customize.properties";

    private final File file;
    private final Properties props;

    public TerrainCustomizeStorage() {
        File folder = new File(Main.instance.getDataFolder(), "claims");
        folder.mkdirs();
        file = new File(folder, FILENAME);
        props = new Properties();
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                props.load(fis);
                fis.close();
            } catch (Exception e) {
                Main.instance.getLogger().warning("[Claims] Error cargando customize.properties: " + e.getMessage());
            }
        }
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setRule(String terrainId, String ruleKey, boolean value) {
        props.setProperty(terrainId + ".rule." + ruleKey, String.valueOf(value));
        save();
    }

    public void setEffect(String terrainId, String effectName) {
        props.setProperty(terrainId + ".effect", effectName);
        save();
    }

    public void setTime(String terrainId, long ticks) {
        props.setProperty(terrainId + ".time", String.valueOf(ticks));
        save();
    }

    public void setWeather(String terrainId, String weatherType) {
        props.setProperty(terrainId + ".weather", weatherType);
        save();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public boolean getRule(String terrainId, String ruleKey, boolean defaultValue) {
        String key = terrainId + ".rule." + ruleKey;
        if (!props.containsKey(key)) return defaultValue;
        return Boolean.parseBoolean(props.getProperty(key));
    }

    public String getEffect(String terrainId) {
        return props.getProperty(terrainId + ".effect", "NONE");
    }

    public long getTime(String terrainId) {
        String val = props.getProperty(terrainId + ".time");
        if (val == null) return -1L;
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return -1L; }
    }

    public String getWeather(String terrainId) {
        return props.getProperty(terrainId + ".weather", "");
    }

    /**
     * Carga todas las reglas guardadas para un terreno específico.
     * @return Mapa de ruleKey → valor booleano con solo las reglas que existen en disco.
     */
    public Map<String, Boolean> loadRules(String terrainId) {
        Map<String, Boolean> rules = new HashMap<String, Boolean>();
        String prefix = terrainId + ".rule.";
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                String ruleKey = key.substring(prefix.length());
                rules.put(ruleKey, Boolean.parseBoolean(props.getProperty(key)));
            }
        }
        return rules;
    }

    /** Elimina todas las entradas de un terreno del archivo. */
    public void deleteTerrainData(String terrainId) {
        String prefix = terrainId + ".";
        for (String key : props.stringPropertyNames().toArray(new String[0])) {
            if (key.startsWith(prefix)) {
                props.remove(key);
            }
        }
        save();
    }

    // ─── Persistencia ─────────────────────────────────────────────────────────

    private void save() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            props.store(fos, "Terrain Customization Data — no editar manualmente");
            fos.close();
        } catch (Exception e) {
            Main.instance.getLogger().warning("[Claims] Error guardando customize.properties: " + e.getMessage());
        }
    }
}