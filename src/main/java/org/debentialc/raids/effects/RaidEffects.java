package org.debentialc.raids.effects;

import org.bukkit.Location;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.List;


public class RaidEffects {

    public static void raidStartEffect(List<Player> players, Location centerLocation) {
        for (int i = 0; i < 5; i++) {
            centerLocation.getWorld().playEffect(centerLocation, Effect.SMOKE, 0);
        }

        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.EXPLODE, 2.0f, 1.0f);
        }
    }

    public static void waveStartingEffect(List<Player> players, int waveNumber, Location location) {
        for (int i = 0; i < 10; i++) {
            location.getWorld().playEffect(location, Effect.POTION_BREAK, 0);
        }

        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.5f);
        }
    }

    public static void waveActiveEffect(List<Player> players, int waveNumber, int totalWaves) {
        for (Player player : players) {
            player.playSound(player.getLocation(), Sound.WITHER_HURT, 1.0f, 1.0f);
        }
    }

    public static void waveCompleteEffect(List<Player> players, int waveNumber) {
        for (Player player : players) {
            Location loc = player.getLocation();
            loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
            player.playSound(loc, Sound.LEVEL_UP, 1.0f, 1.5f);
        }
    }

    public static void raidVictoryEffect(List<Player> players, String raidName) {
        if (players.isEmpty()) {
            return;
        }

        Location centerLoc = players.get(0).getLocation();

        for (int i = 0; i < 10; i++) {
            centerLoc.getWorld().playEffect(centerLoc, Effect.MOBSPAWNER_FLAMES, 0);
        }

        for (Player player : players) {
            Location loc = player.getLocation();
            player.playSound(loc, Sound.EXPLODE, 2.0f, 1.0f);
            player.playSound(loc, Sound.SUCCESSFUL_HIT, 2.0f, 1.2f);
        }
    }

    public static void raidFailureEffect(List<Player> players) {
        for (Player player : players) {
            Location loc = player.getLocation();

            for (int i = 0; i < 15; i++) {
                loc.getWorld().playEffect(loc, Effect.SMOKE, 0);
            }

            player.playSound(loc, Sound.WITHER_HURT, 1.0f, 0.5f);
        }
    }

    public static void playerDeathEffect(Player deadPlayer, Location location) {
        location.getWorld().playEffect(location, Effect.SMOKE, 0);
        location.getWorld().playEffect(location, Effect.POTION_BREAK, 0);
        deadPlayer.playSound(location, Sound.BURP, 1.0f, 0.8f);
    }

    public static void playerRemovedEffect(Player player) {
        Location loc = player.getLocation();
        loc.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 0);
    }
}