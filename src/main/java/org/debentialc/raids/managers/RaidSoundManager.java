package org.debentialc.raids.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.List;

public class RaidSoundManager {


    public static void playRaidStartSound(Player player) {
        player.playSound(player.getLocation(), Sound.EXPLODE, 2.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.WITHER_HURT, 1.5f, 0.9f);
    }

    public static void playWaveStartSound(Player player) {
        player.playSound(player.getLocation(), Sound.WITHER_HURT, 1.5f, 1.0f);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.8f);
    }

    public static void playWaveWarningSound(Player player) {
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.5f);
    }

    public static void playWaveCompleteSound(Player player) {
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5f, 1.5f);
    }

    public static void playVictorySound(Player player) {
        player.playSound(player.getLocation(), Sound.EXPLODE, 2.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 2.0f, 1.2f);
    }

    public static void playFailureSound(Player player) {
        player.playSound(player.getLocation(), Sound.WITHER_HURT, 1.5f, 0.5f);
    }

    public static void playPlayerDeathSound(Player deadPlayer) {
        deadPlayer.playSound(deadPlayer.getLocation(), Sound.BURP, 1.0f, 0.8f);
    }

    public static void playDamageSound(Player player) {
        player.playSound(player.getLocation(), Sound.SKELETON_HURT, 1.0f, 1.0f);
    }

    public static void playEnemyApproachingSound(Player player) {
        player.playSound(player.getLocation(), Sound.SKELETON_HURT, 0.8f, 0.8f);
    }

    public static void playAttackSound(Player player) {
        player.playSound(player.getLocation(), Sound.WITHER_HURT, 1.0f, 1.2f);
    }


    public static void playTensionSound(Player player) {
        player.playSound(player.getLocation(), Sound.CLICK, 0.6f, 0.5f);
    }

    public static void playBuffSound(Player player) {
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
    }

    public static void playDebuffSound(Player player) {
        player.playSound(player.getLocation(), Sound.WITHER_HURT, 0.8f, 0.7f);
    }

    public static void playAlertSound(Player player) {
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.5f);
    }


    public static void playGroupSound(List<Player> players, Sound sound, float volume, float pitch) {
        for (int i = 0; i < players.size(); i++) {
            Player player = (Player) players.get(i);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static void playGroupRaidStartSound(List<Player> players) {
        playGroupSound(players, Sound.EXPLODE, 2.0f, 1.0f);
    }

    public static void playGroupVictorySound(List<Player> players) {
        playGroupSound(players, Sound.EXPLODE, 2.0f, 1.0f);
        playGroupSound(players, Sound.SUCCESSFUL_HIT, 2.0f, 1.2f);
    }

    public static void playGroupFailureSound(List<Player> players) {
        playGroupSound(players, Sound.WITHER_HURT, 1.5f, 0.5f);
    }

    public static void playGroupWaveSound(List<Player> players) {
        playGroupSound(players, Sound.WITHER_HURT, 1.5f, 1.0f);
    }
}