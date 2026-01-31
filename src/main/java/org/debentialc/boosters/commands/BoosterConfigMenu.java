package org.debentialc.boosters.commands;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.debentialc.boosters.core.BoosterSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoosterConfigMenu {

    public static Inventory createMainMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Booster Configuration");

        ItemStack global = createMenuItem(Material.GOLD_BLOCK, "§6Global Booster Settings",
                "§7Click to configure global boosters");
        ItemStack personal = createMenuItem(Material.DIAMOND, "§6Personal Booster Levels",
                "§7Configure personal booster multipliers");
        ItemStack ranks = createMenuItem(Material.EMERALD, "§6Rank Multipliers",
                "§7Configure rank-based multipliers");
        ItemStack durations = createMenuItem(Material.OBSIDIAN, "§6Durations",
                "§7Configure booster durations");

        inv.setItem(11, global);
        inv.setItem(13, personal);
        inv.setItem(15, ranks);
        inv.setItem(22, durations);

        return inv;
    }

    public static Inventory createGlobalBoosterMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Global Booster Configuration");

        double current = BoosterSettings.getGlobalMultiplier();
        ItemStack current_item = createMenuItem(Material.GOLD_NUGGET,
                "§6Current Multiplier: §a" + String.format("%.2f", current) + "x",
                "§7Right-click to modify");

        inv.setItem(13, current_item);

        ItemStack back = createMenuItem(Material.ARROW, "§7Back", "");
        inv.setItem(26, back);

        return inv;
    }

    public static Inventory createPersonalBoosterMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Personal Booster Levels");

        Map<Integer, Double> levels = BoosterSettings.getAllPersonalBoosterLevels();
        int slot = 10;

        for (int i = 1; i <= 5; i++) {
            double mult = levels.getOrDefault(i, 0.0);
            int bonus = (int) ((mult - 1.0) * 100);
            ItemStack item = createMenuItem(Material.DIAMOND,
                    String.format("§6Level %d: §a+%d%%", i, bonus),
                    "§7Multiplier: " + String.format("%.2f", mult) + "x");
            inv.setItem(slot, item);
            slot += 2;
        }

        ItemStack back = createMenuItem(Material.ARROW, "§7Back", "");
        inv.setItem(26, back);

        return inv;
    }

    public static Inventory createRankMultiplierMenu() {
        Inventory inv = Bukkit.createInventory(null, 36, "§6Rank Multipliers");

        Map<String, Double> ranks = BoosterSettings.getAllRankMultipliers();
        int slot = 10;

        for (Map.Entry<String, Double> entry : ranks.entrySet()) {
            String rank = entry.getKey();
            double mult = entry.getValue();
            ItemStack item = createMenuItem(Material.EMERALD,
                    "§6" + capitalize(rank),
                    String.format("§7Multiplier: §a%.2fx", mult));
            inv.setItem(slot, item);
            slot++;
            if ((slot - 9) % 8 == 0) slot += 2;
        }

        ItemStack back = createMenuItem(Material.ARROW, "§7Back", "");
        inv.setItem(35, back);

        return inv;
    }

    public static Inventory createDurationMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Booster Durations");

        long global = BoosterSettings.getGlobalBoosterDuration();
        long personal = BoosterSettings.getPersonalBoosterDuration();
        long storage = BoosterSettings.getPersonalBoosterStorageDays();

        ItemStack globalItem = createMenuItem(Material.PAPER,
                "§6Global Duration: §a" + formatSeconds(global),
                "§7Click to modify");
        ItemStack personalItem = createMenuItem(Material.BOOK,
                "§6Personal Duration: §a" + formatSeconds(personal),
                "§7Click to modify");
        ItemStack storageItem = createMenuItem(Material.BOOKSHELF,
                "§6Storage Days: §a" + storage + " days",
                "§7Click to modify");

        inv.setItem(11, globalItem);
        inv.setItem(13, personalItem);
        inv.setItem(15, storageItem);

        ItemStack back = createMenuItem(Material.ARROW, "§7Back", "");
        inv.setItem(26, back);

        return inv;
    }

    private static ItemStack createMenuItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            if (lore != null && !lore.isEmpty()) {
                loreList.add(lore);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private static String formatSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}