package org.debentialc.boosters.core;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.debentialc.boosters.commands.BoosterCommand;
import org.debentialc.boosters.events.BoosterListener;
import org.debentialc.boosters.integration.TPConsumeListener;
import org.debentialc.boosters.placeholders.BoosterPlaceholder;
import org.debentialc.boosters.storage.BoosterStorage;

/**
 * Módulo principal del sistema de Boosters
 * VERSIÓN CORREGIDA: Registro adecuado de comandos, listeners y placeholders
 */
public class BoosterModule {

    private static JavaPlugin plugin;
    private static BoosterPlaceholder placeholderExpansion;

    /**
     * Inicializa el sistema de boosters
     */
    public static void initialize(JavaPlugin instance) {
        plugin = instance;

        loadData();

        registerPlaceholders();

        scheduleT();

        plugin.getLogger().info("Sistema de Boosters inicializado correctamente");
    }

    private static void startTPMonitoring() {
        TPConsumeListener.startTPMonitoring();
        plugin.getLogger().info("Monitoreo de TPs iniciado");
    }
    /**
     * Registra los placeholders si PlaceholderAPI está disponible
     */
    private static void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new BoosterPlaceholder();

            if (placeholderExpansion.register()) {
                plugin.getLogger().info("PlaceholderAPI hook registrado exitosamente");
            } else {
                plugin.getLogger().warning("No se pudo registrar PlaceholderAPI hook");
            }
        } else {
            plugin.getLogger().info("PlaceholderAPI no encontrado - Placeholders no disponibles");
        }
    }

    /**
     * Carga los datos almacenados
     */
    private static void loadData() {
        BoosterStorage.loadAllData();
        plugin.getLogger().info("Datos de Boosters cargados");
    }

    /**
     * Inicia las tareas programadas
     */
    private static void scheduleT() {
        BoosterTaskScheduler.initialize(plugin);
        plugin.getLogger().info("Tareas programadas de Boosters iniciadas");
    }

    /**
     * Apaga el sistema guardando datos y cancelando tareas
     */
    public static void shutdown() {
        BoosterStorage.saveAllData();

        BoosterTaskScheduler.cancelTasks();

        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }

        plugin.getLogger().info("Sistema de Boosters guardado y apagado");
    }

    /**
     * Obtiene la instancia del plugin
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }
}