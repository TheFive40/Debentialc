package org.debentialc.customitems.tools.ci;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.debentialc.Main;
import org.debentialc.customitems.tools.CC;

import java.util.*;

public class EffectsManager {

    private static final Map<Player, Hologram> playerHolograms = new HashMap<>();
    public static void spawnHologram ( Player player, String text,double y,double z ) {
        Location loc = player.getLocation ( );
        loc.setY ( loc.getY ( ) + y );
        loc.setZ ( loc.getZ ( ) + z );
        Hologram hologram = HolographicDisplaysAPI.createHologram (Main.instance, loc, CC.translate ( text ) );
        Bukkit.getScheduler ( ).runTaskLater ( Main.instance, hologram::delete, 1L );
    }
    public static void removeHologram(Player player) {
        if (playerHolograms.containsKey(player)) {
            Hologram hologram = playerHolograms.remove(player);
            hologram.delete();
        }
    }
}