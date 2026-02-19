package org.debentialc.claims;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.debentialc.Main;
import org.debentialc.claims.events.LeaseProtectionListener;
import org.debentialc.claims.events.LeaseSelectionListener;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.managers.TerrainManager;

public class ClaimsModule {

    public static void initialize(Main plugin) {
        setupEconomy(plugin);
        TerrainManager.getInstance();
        LeaseManager.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new LeaseProtectionListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LeaseSelectionListener(), plugin);
        plugin.getLogger().info("[Claims] Sistema de terrenos inicializado.");
        plugin.getLogger().info("[Claims] Sistema de arrendamiento inicializado.");
    }

    private static void setupEconomy(Main plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("[Claims] Vault no encontrado.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("[Claims] No se encontró proveedor de economía.");
            return;
        }
        TerrainManager.getInstance().setEconomy(rsp.getProvider());
        plugin.getLogger().info("[Claims] Economía cargada correctamente.");
    }
}