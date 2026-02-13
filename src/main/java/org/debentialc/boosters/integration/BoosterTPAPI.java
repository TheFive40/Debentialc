package org.debentialc.boosters.integration;

import noppes.npcs.api.entity.IDBCPlayer;
import noppes.npcs.scripted.NpcAPI;
import org.bukkit.entity.Player;
import org.debentialc.boosters.managers.GlobalBoosterManager;
import org.debentialc.boosters.managers.PersonalBoosterManager;

public class BoosterTPAPI {

    public static void giveTPsWithBooster(Player player, int baseTPs) {
        giveTPsWithBooster(player, baseTPs, true);
    }

    public static void giveTPsWithBooster(Player player, int baseTPs, boolean showMessage) {
        if (player == null || baseTPs <= 0) return;

        try {
            IDBCPlayer dbcPlayer = NpcAPI.Instance().getPlayer(player.getName()).getDBCPlayer();
            if (dbcPlayer == null) return;

            double globalMultiplier = GlobalBoosterManager.getCurrentMultiplier();
            double personalMultiplier = PersonalBoosterManager.getActiveMultiplier(player.getUniqueId());
            double combinedMultiplier = globalMultiplier * personalMultiplier;

            int totalTPs = (int) Math.round(baseTPs * combinedMultiplier);

            dbcPlayer.setTP(dbcPlayer.getTP() + totalTPs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int calculateBoostedTPs(Player player, int baseTPs) {
        if (player == null || baseTPs <= 0) return baseTPs;

        try {
            double globalMultiplier = GlobalBoosterManager.getCurrentMultiplier();
            double personalMultiplier = PersonalBoosterManager.getActiveMultiplier(player.getUniqueId());
            double combinedMultiplier = globalMultiplier * personalMultiplier;

            return (int) Math.round(baseTPs * combinedMultiplier);

        } catch (Exception e) {
            return baseTPs;
        }
    }

    public static double getCombinedMultiplier(Player player) {
        if (player == null) return 1.0;

        double globalMultiplier = GlobalBoosterManager.getCurrentMultiplier();
        double personalMultiplier = PersonalBoosterManager.getActiveMultiplier(player.getUniqueId());

        return globalMultiplier * personalMultiplier;
    }

    public static boolean hasActiveBooster(Player player) {
        if (player == null) return false;

        boolean hasGlobal = GlobalBoosterManager.isBoosterActive();
        boolean hasPersonal = PersonalBoosterManager.getActiveBooster(player.getUniqueId()) != null;

        return hasGlobal || hasPersonal;
    }

    public static void giveTPsSilent(Player player, int baseTPs) {
        giveTPsWithBooster(player, baseTPs, false);
    }

    public static int getBonusTPs(Player player, int baseTPs) {
        if (player == null || baseTPs <= 0) return 0;

        int boostedTPs = calculateBoostedTPs(player, baseTPs);
        return boostedTPs - baseTPs;
    }
}