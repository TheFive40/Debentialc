package org.debentialc.boosters.integration;

import noppes.npcs.api.entity.IDBCPlayer;
import noppes.npcs.scripted.NpcAPI;
import org.bukkit.entity.Player;
import org.debentialc.boosters.managers.GlobalBoosterManager;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.service.CC;

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
            int bonusTPs = totalTPs - baseTPs;

            dbcPlayer.setTP(dbcPlayer.getTP() + totalTPs);

            if (showMessage) {
                sendTPBoosterMessage(player, baseTPs, bonusTPs, globalMultiplier, personalMultiplier, combinedMultiplier);
            }

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

    private static void sendTPBoosterMessage(Player player, int baseTPs, int bonusTPs,
                                             double globalMult, double personalMult, double combinedMult) {
        if (bonusTPs <= 0) {
            player.sendMessage(CC.translate("&a+ " + baseTPs + " TPs"));
            return;
        }

        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l⚡ BOOSTER DE TPS APLICADO ⚡"));
        player.sendMessage("");
        player.sendMessage(CC.translate("  &eTPs Base: &a+" + baseTPs));
        player.sendMessage(CC.translate("  &eBonus: &6+" + bonusTPs + " TPs"));
        player.sendMessage(CC.translate("  &eTotal: &b+" + (baseTPs + bonusTPs) + " TPs"));
        player.sendMessage("");

        if (globalMult > 1.0) {
            String globalPercent = String.format("%.0f%%", (globalMult - 1.0) * 100);
            player.sendMessage(CC.translate("  &6⚡ Booster Global: &a+" + globalPercent));
        }

        if (personalMult > 1.0) {
            String personalPercent = String.format("%.0f%%", (personalMult - 1.0) * 100);
            player.sendMessage(CC.translate("  &b⚡ Booster Personal: &a+" + personalPercent));
        }

        String totalPercent = String.format("%.0f%%", (combinedMult - 1.0) * 100);
        player.sendMessage(CC.translate("  &a⚡ Multiplicador Total: &6x" + String.format("%.2f", combinedMult) + " &7(+" + totalPercent + ")"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
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