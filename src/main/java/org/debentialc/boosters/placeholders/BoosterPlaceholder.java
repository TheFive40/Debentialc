package org.debentialc.boosters.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.debentialc.boosters.managers.GlobalBoosterManager;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.boosters.models.PersonalBooster;

public class BoosterPlaceholder extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "boosters";
    }

    @Override
    public String getAuthor() {
        return "Debentialc";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        switch (identifier) {
            case "global_active":
                return GlobalBoosterManager.isBoosterActive() ? "§aActive" : "§cInactive";

            case "global_multiplier":
                double global = GlobalBoosterManager.getCurrentMultiplier();
                return String.format("§6%.2fx", global);

            case "global_percentage":
                double globalMult = GlobalBoosterManager.getCurrentMultiplier();
                int percentage = (int) ((globalMult - 1.0) * 100);
                return percentage > 0 ? "§a+" + percentage + "%" : "§c0%";

            case "global_time":
                long seconds = GlobalBoosterManager.getRemainingSeconds();
                if (seconds <= 0) return "§cExpired";
                return formatTime(seconds);

            case "personal_active":
                PersonalBooster personal = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
                return personal != null ? "§aActive" : "§cInactive";

            case "personal_multiplier":
                PersonalBooster pb = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
                if (pb != null) {
                    return String.format("§6%.2fx", pb.getMultiplier());
                }
                return "§61.00x";

            case "personal_percentage":
                PersonalBooster pboost = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
                if (pboost != null) {
                    int pct = pboost.getPercentageBonus();
                    return pct > 0 ? "§a+" + pct + "%" : "§c0%";
                }
                return "§c0%";

            case "personal_level":
                PersonalBooster plevel = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
                return plevel != null ? "§6" + plevel.getLevelName() : "§cNone";

            case "personal_time":
                PersonalBooster ptime = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
                if (ptime != null) {
                    long remaining = ptime.getActivationTimeRemaining(900);
                    return remaining > 0 ? formatTime(remaining) : "§cExpired";
                }
                return "§cInactive";

            case "personal_count":
                int count = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId()).size();
                return "§6" + count;

            case "combined_multiplier":
                double combined = GlobalBoosterManager.getCurrentMultiplier() *
                        PersonalBoosterManager.getActiveMultiplier(player.getUniqueId());
                return String.format("§6%.2fx", combined);

            default:
                return null;
        }
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("§6%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("§6%dm %ds", minutes, secs);
        } else {
            return String.format("§6%ds", secs);
        }
    }
}