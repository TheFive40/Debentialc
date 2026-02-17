package org.debentialc.claims;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.debentialc.Main;
import org.debentialc.claims.managers.TerrainManager;

public class ClaimsModule {

    public static void initialize(Main plugin) {
        setupEconomy(plugin);
        TerrainManager.getInstance();
        plugin.getLogger().info("[Claims] Sistema de terrenos inicializado.");
    }

    private static void setupEconomy(Main plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("[Claims] Vault no encontrado. El sistema de compra no estará disponible.");
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