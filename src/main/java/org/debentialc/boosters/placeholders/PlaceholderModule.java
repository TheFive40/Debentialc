package org.debentialc.boosters.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.debentialc.placeholder.DebentialcPlaceHolder;

public class PlaceholderModule {

    private static DebentialcPlaceHolder placeholderExpansion;

    public static void initialize(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new DebentialcPlaceHolder();

            if (placeholderExpansion.register()) {
                plugin.getLogger().info("PlaceholderAPI registrado exitosamente");
            } else {
                plugin.getLogger().warning("No se pudo registrar PlaceholderAPI");
            }
        } else {
            plugin.getLogger().info("PlaceholderAPI no encontrado - Placeholders no disponibles");
        }
    }

    public static void shutdown() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
    }
}