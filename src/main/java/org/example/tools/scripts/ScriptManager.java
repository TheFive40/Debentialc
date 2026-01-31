package org.example.tools.scripts;

import org.bukkit.Bukkit;
import org.example.Main;
import org.example.tools.CC;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Gestor principal del sistema de scripts
 * Maneja descarga, validación, carga y ejecución de scripts JavaScript mediante Rhino
 */
public class ScriptManager {
    private static ScriptManager instance;

    // Scripts compilados en memoria (itemId -> Script compilado)
    private final Map<String, Script> loadedScripts = new HashMap<>();

    // Metadatos de scripts (itemId -> ScriptMetadata)
    private final Map<String, ScriptMetadata> scriptMetadata = new HashMap<>();

    // Carpeta donde se guardan los scripts
    private File scriptsFolder;

    // Archivo de configuración de scripts
    private ScriptConfig scriptConfig;

    private ScriptManager() {
        initialize();
    }

    public static ScriptManager getInstance() {
        if (instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }

    /**
     * Inicializa el sistema de scripts
     */
    private void initialize() {
        // Crear carpeta de scripts
        scriptsFolder = new File(Main.instance.getDataFolder(), "scripts");
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdirs();
        }

        // Cargar configuración
        scriptConfig = new ScriptConfig();

        // Cargar scripts existentes
        loadAllScripts();

        Bukkit.getLogger().info("[ScriptManager] Sistema de scripts inicializado");
    }

    /**
     * Descarga un script desde una URL remota
     * @param url URL del script (GitHub raw, Pastebin, etc.)
     * @return Contenido del script, o null si falla
     */
    public String downloadScript(String url) {
        StringBuilder content = new StringBuilder();

        try {
            // Convertir URLs de GitHub a raw si es necesario
            url = convertToRawUrl(url);

            URL scriptUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) scriptUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                return content.toString();
            } else {
                Bukkit.getLogger().warning("[ScriptManager] Error HTTP " + responseCode + " al descargar: " + url);
                return null;
            }
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[ScriptManager] Error descargando script: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Valida un script sin ejecutarlo
     * @param scriptContent Contenido del script
     * @return true si el script es válido
     */
    public boolean validateScript(String scriptContent) {
        Context ctx = Context.enter();
        try {
            ctx.setOptimizationLevel(-1); // Modo interpretado para validación rápida

            // Intentar compilar el script
            ctx.compileString(scriptContent, "validation", 1, null);

            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[ScriptManager] Script inválido: " + e.getMessage());
            return false;
        } finally {
            Context.exit();
        }
    }

    /**
     * Guarda un script descargado en la carpeta local
     * @param itemId ID del item asociado
     * @param scriptContent Contenido del script
     * @param sourceUrl URL de origen
     * @return true si se guardó correctamente
     */
    public boolean saveScript(String itemId, String scriptContent, String sourceUrl) {
        File scriptFile = new File(scriptsFolder, itemId + ".js");

        try {
            // Guardar contenido
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile))) {
                writer.write(scriptContent);
            }

            // Guardar metadata
            scriptConfig.setScriptMetadata(itemId, sourceUrl, scriptFile.getName());

            Bukkit.getLogger().info("[ScriptManager] Script guardado: " + itemId + ".js");
            return true;
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[ScriptManager] Error guardando script: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Carga un script desde archivo y lo compila
     * @param itemId ID del item asociado
     * @return true si se cargó correctamente
     */
    public boolean loadScript(String itemId) {
        File scriptFile = new File(scriptsFolder, itemId + ".js");

        if (!scriptFile.exists()) {
            return false;
        }

        try {
            // Leer contenido
            String scriptContent = new String(Files.readAllBytes(scriptFile.toPath()));

            // Compilar script
            Context ctx = Context.enter();
            try {
                ctx.setOptimizationLevel(-1); // Modo interpretado

                Script compiledScript = ctx.compileString(scriptContent, itemId, 1, null);

                // Guardar en memoria
                loadedScripts.put(itemId, compiledScript);

                // Cargar metadata
                ScriptMetadata metadata = scriptConfig.getScriptMetadata(itemId);
                if (metadata != null) {
                    scriptMetadata.put(itemId, metadata);
                }

                Bukkit.getLogger().info("[ScriptManager] Script cargado: " + itemId + ".js");
                return true;
            } finally {
                Context.exit();
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[ScriptManager] Error cargando script " + itemId + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Carga todos los scripts existentes en la carpeta
     */
    public void loadAllScripts() {
        File[] scriptFiles = scriptsFolder.listFiles((dir, name) -> name.endsWith(".js"));

        if (scriptFiles == null || scriptFiles.length == 0) {
            Bukkit.getLogger().info("[ScriptManager] No hay scripts para cargar");
            return;
        }

        int loaded = 0;
        for (File scriptFile : scriptFiles) {
            String itemId = scriptFile.getName().replace(".js", "");
            if (loadScript(itemId)) {
                loaded++;
            }
        }

        Bukkit.getLogger().info("[ScriptManager] Cargados " + loaded + " scripts");
    }

    /**
     * Ejecuta un script asociado a un item
     * @param itemId ID del item
     * @param context Contexto de ejecución (jugador, ubicación, etc.)
     * @return true si se ejecutó correctamente
     */
    public boolean executeScript(String itemId, ScriptContext context) {
        Script compiledScript = loadedScripts.get(itemId);

        if (compiledScript == null) {
            return false;
        }

        Context ctx = Context.enter();
        try {
            ctx.setOptimizationLevel(-1);

            // Crear scope con variables disponibles
            Scriptable scope = ctx.initStandardObjects();

            // Inyectar variables del contexto
            scope.put("player", scope, context.getPlayer());
            scope.put("server", scope, Bukkit.getServer());
            scope.put("location", scope, context.getLocation());
            scope.put("item", scope, context.getItem());
            scope.put("world", scope, context.getWorld());

            // Ejecutar script
            compiledScript.exec(ctx, scope);

            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[ScriptManager] Error ejecutando script " + itemId + ": " + e.getMessage(), e);

            // Notificar al jugador si está disponible
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage(CC.translate("&c✗ Error ejecutando script"));
            }

            return false;
        } finally {
            Context.exit();
        }
    }

    /**
     * Elimina un script (archivo y metadata)
     * @param itemId ID del item asociado
     * @return true si se eliminó correctamente
     */
    public boolean deleteScript(String itemId) {
        // Eliminar de memoria
        loadedScripts.remove(itemId);
        scriptMetadata.remove(itemId);

        // Eliminar archivo
        File scriptFile = new File(scriptsFolder, itemId + ".js");
        boolean fileDeleted = !scriptFile.exists() || scriptFile.delete();

        // Eliminar metadata
        scriptConfig.removeScriptMetadata(itemId);

        if (fileDeleted) {
            Bukkit.getLogger().info("[ScriptManager] Script eliminado: " + itemId);
        }

        return fileDeleted;
    }

    /**
     * Verifica si un item tiene un script asociado
     */
    public boolean hasScript(String itemId) {
        return loadedScripts.containsKey(itemId);
    }

    /**
     * Obtiene la metadata de un script
     */
    public ScriptMetadata getMetadata(String itemId) {
        return scriptMetadata.get(itemId);
    }

    /**
     * Obtiene todos los IDs de items con scripts
     */
    public Map<String, ScriptMetadata> getAllScripts() {
        return new HashMap<>(scriptMetadata);
    }

    /**
     * Recarga un script específico
     */
    public boolean reloadScript(String itemId) {
        loadedScripts.remove(itemId);
        return loadScript(itemId);
    }

    /**
     * Recarga todos los scripts
     */
    public void reloadAllScripts() {
        loadedScripts.clear();
        scriptMetadata.clear();
        loadAllScripts();
    }

    /**
     * Convierte URLs de GitHub a formato raw
     */
    private String convertToRawUrl(String url) {
        if (url.contains("github.com") && !url.contains("/raw/")) {
            // github.com/user/repo/blob/main/script.js -> raw.githubusercontent.com/user/repo/main/script.js
            url = url.replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/");
        }
        return url;
    }

    /**
     * Clase interna para metadata de scripts
     */
    public static class ScriptMetadata {
        private String itemId;
        private String sourceUrl;
        private String fileName;
        private long downloadDate;

        public ScriptMetadata(String itemId, String sourceUrl, String fileName) {
            this.itemId = itemId;
            this.sourceUrl = sourceUrl;
            this.fileName = fileName;
            this.downloadDate = System.currentTimeMillis();
        }

        public String getItemId() { return itemId; }
        public String getSourceUrl() { return sourceUrl; }
        public String getFileName() { return fileName; }
        public long getDownloadDate() { return downloadDate; }
    }
}