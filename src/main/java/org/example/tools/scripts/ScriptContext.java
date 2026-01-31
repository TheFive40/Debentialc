package org.example.tools.scripts;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Contexto de ejecución para scripts
 * Contiene todas las variables que estarán disponibles para el script
 */
public class ScriptContext {
    private Player player;
    private Location location;
    private ItemStack item;
    private World world;
    private String eventType; // "use", "interact", "consume", etc.

    public ScriptContext(Player player) {
        this.player = player;
        if (player != null) {
            this.location = player.getLocation();
            this.world = player.getWorld();
        }
    }

    public ScriptContext setPlayer(Player player) {
        this.player = player;
        return this;
    }

    public ScriptContext setLocation(Location location) {
        this.location = location;
        return this;
    }

    public ScriptContext setItem(ItemStack item) {
        this.item = item;
        return this;
    }

    public ScriptContext setWorld(World world) {
        this.world = world;
        return this;
    }

    public ScriptContext setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack getItem() {
        return item;
    }

    public World getWorld() {
        return world;
    }

    public String getEventType() {
        return eventType;
    }
}