package org.debentialc.boosters.core;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.debentialc.boosters.commands.BoosterCommand;
import org.debentialc.boosters.events.BoosterListener;
import org.debentialc.boosters.placeholders.BoosterPlaceholder;
import org.debentialc.boosters.storage.BoosterStorage;

public class BoosterModule {

    private static JavaPlugin plugin;

    public static void initialize(JavaPlugin instance) {
        plugin = instance;

        registerCommands();
        registerListeners();
        registerPlaceholders();
        loadData();
        scheduleT();

        plugin.getLogger().info("Sistema de Boosters inicializado correctamente");
    }

    private static void scheduleT() {
        BoosterTaskScheduler.initialize(plugin);
    }

    private static void registerCommands() {
        plugin.getCommand("booster").setExecutor(new BoosterCommand());
    }

    private static void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new BoosterListener(), plugin);
    }

    private static void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BoosterPlaceholder().register();
            plugin.getLogger().info("PlaceholderAPI hook registrado");
        }
    }

    private static void loadData() {
        BoosterStorage.loadAllData();
    }

    public static void shutdown() {
        BoosterStorage.saveAllData();
        BoosterTaskScheduler.cancelTasks();
        plugin.getLogger().info("Sistema de Boosters guardado");
    }
}