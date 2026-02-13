package org.debentialc.raids.managers;

import org.bukkit.entity.Player;
import java.util.List;

/**
 * RaidTitleManager - Títulos para 1.7.10
 * Usa solo sendMessage() ya que sendTitle no existe
 */
public class RaidTitleManager {


    public static void showRaidStart(Player player, String raidName) {
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§l⚔ RAID INICIADA ⚔");
        player.sendMessage("§e" + raidName);
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }

    public static void showWaveStart(Player player, int waveNumber, int totalWaves) {
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(String.format("§6§l🌊 OLEADA %d/%d 🌊", waveNumber, totalWaves));
        player.sendMessage("§c¡Los enemigos avanzan!");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }

    public static void showWaveComplete(Player player, int waveNumber) {
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(String.format("§a§l✓ OLEADA %d COMPLETADA ✓", waveNumber));
        player.sendMessage("§7Preparándose para la siguiente...");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }

    public static void showVictory(Player player, String raidName) {
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§l🏆 ¡RAID COMPLETADA! 🏆");
        player.sendMessage("§a" + raidName);
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }

    public static void showFailure(Player player) {
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§c§l✗ RAID FALLIDA ✗");
        player.sendMessage("§7Todos fueron derrotados...");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }

    public static void showPlayerDeath(Player player) {
        player.sendMessage("");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§c§lHAS SIDO DERROTADO");
        player.sendMessage("§7No puedes regresar a esta raid");
        player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
    }

    public static void showWarning(Player player, String message) {
        player.sendMessage("");
        player.sendMessage("§c⚠ ADVERTENCIA ⚠");
        player.sendMessage("§f" + message);
        player.sendMessage("");
    }

    public static void showInfo(Player player, String title, String subtitle) {
        player.sendMessage("");
        player.sendMessage("§bℹ " + title);
        player.sendMessage("§f" + subtitle);
        player.sendMessage("");
    }


    public static void sendGroupTitle(List<Player> players, String title, String subtitle) {
        for (int i = 0; i < players.size(); i++) {
            Player player = (Player) players.get(i);
            player.sendMessage("");
            player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage(title);
            player.sendMessage(subtitle);
            player.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("");
        }
    }

    public static void showGroupRaidStart(List<Player> players, String raidName) {
        sendGroupTitle(players,
                "§6§l⚔ RAID INICIADA ⚔",
                "§e" + raidName);
    }

    public static void showGroupWave(List<Player> players, int waveNumber, int totalWaves) {
        sendGroupTitle(players,
                String.format("§6§l🌊 OLEADA %d/%d 🌊", waveNumber, totalWaves),
                "§c¡Los enemigos avanzan!");
    }

    public static void showGroupVictory(List<Player> players, String raidName) {
        sendGroupTitle(players,
                "§6§l🏆 ¡RAID COMPLETADA! 🏆",
                "§a" + raidName);
    }

    public static void showGroupFailure(List<Player> players) {
        sendGroupTitle(players,
                "§c§l✗ RAID FALLIDA ✗",
                "§7Todos fueron derrotados...");
    }


    public static void sendGroupActionBar(List<Player> players, String message) {
        for (int i = 0; i < players.size(); i++) {
            Player player = (Player) players.get(i);
            player.sendMessage("§e" + message);
        }
    }

    public static void showProgressBar(Player player, int current, int total) {
        int percentage = (current * 100) / total;
        String bar = createProgressBar(percentage, 20);
        player.sendMessage(String.format("§eProgreso: §f%s §7(%d%%)", bar, percentage));
    }

    public static void showGroupProgressBar(List<Player> players, int current, int total) {
        for (int i = 0; i < players.size(); i++) {
            Player player = (Player) players.get(i);
            showProgressBar(player, current, total);
        }
    }

    public static void showWaveProgress(Player player, int currentWave, int totalWaves, int enemiesRemaining) {
        String message = String.format("Oleada: %d/%d | Enemigos: %d",
                currentWave, totalWaves, enemiesRemaining);
        player.sendMessage("§6" + message);
    }

    public static void showGroupWaveProgress(List<Player> players, int currentWave, int totalWaves, int enemiesRemaining) {
        for (int i = 0; i < players.size(); i++) {
            Player player = (Player) players.get(i);
            showWaveProgress(player, currentWave, totalWaves, enemiesRemaining);
        }
    }


    private static String createProgressBar(int percentage, int length) {
        int filled = (percentage * length) / 100;
        StringBuffer bar = new StringBuffer();

        bar.append("§a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }

        bar.append("§7");
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }

        return bar.toString();
    }

    public static String getPercentageText(int percentage) {
        if (percentage >= 75) {
            return "§a" + percentage + "%";
        } else if (percentage >= 50) {
            return "§e" + percentage + "%";
        } else if (percentage >= 25) {
            return "§6" + percentage + "%";
        } else {
            return "§c" + percentage + "%";
        }
    }
}