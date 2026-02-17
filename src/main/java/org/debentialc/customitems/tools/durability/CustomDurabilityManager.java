package org.debentialc.customitems.tools.durability;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.service.CC;

import java.util.ArrayList;
import java.util.List;

public class CustomDurabilityManager {

    private static final String UNBREAKABLE_KEY = "§8[UNBREAKABLE]";

    public static void setUnbreakable(ItemStack item, boolean unbreakable) {
        if (item == null || item.getTypeId() == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.removeIf(line -> line.equals(UNBREAKABLE_KEY));

        if (unbreakable) {
            lore.add(UNBREAKABLE_KEY);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        if (unbreakable && item.getType().getMaxDurability() > 0 && !isModItem(item)) {
            item.setDurability((short) 0);
        }
    }

    public static boolean isUnbreakable(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return false;
        if (!item.hasItemMeta()) return false;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;

        return lore.contains(UNBREAKABLE_KEY);
    }

    public static void setCustomMaxDurability(ItemStack item, int maxDurability) {
        if (item == null || item.getTypeId() == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        setCustomDurability(item, maxDurability, maxDurability);
    }

    public static void setCustomDurability(ItemStack item, int current, int max) {
        if (item == null || item.getTypeId() == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        current = Math.max(0, Math.min(current, max));

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.removeIf(line -> {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            return cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)");
        });

        double percentage = (double) current / (double) max * 100;
        String durabilityLine = CC.translate("&a" + current + "/" + max + " (" + String.format("%.0f", percentage) + "%)");
        lore.add(0, durabilityLine);

        meta.setLore(lore);
        item.setItemMeta(meta);

        syncVisualDurability(item, current, max);
    }

    public static int getCustomDurability(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return 0;
        if (!item.hasItemMeta()) return 0;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return 0;

        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            if (cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)")) {
                try {
                    String[] parts = cleanLine.split("/");
                    return Integer.parseInt(parts[0].trim());
                } catch (Exception e) {
                    return 0;
                }
            }
        }

        return 0;
    }

    public static int getCustomMaxDurability(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return 0;
        if (!item.hasItemMeta()) return 0;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return 0;

        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            if (cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)")) {
                try {
                    String[] parts = cleanLine.split("/");
                    String maxPart = parts[1].split(" ")[0].trim();
                    return Integer.parseInt(maxPart);
                } catch (Exception e) {
                    return 0;
                }
            }
        }

        return 0;
    }

    public static boolean hasCustomDurability(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return false;
        if (!item.hasItemMeta()) return false;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return false;

        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            if (cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)")) {
                return true;
            }
        }

        return false;
    }

    public static boolean damageItem(ItemStack item, int damage) {
        if (isUnbreakable(item)) return false;
        if (!hasCustomDurability(item)) return false;

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);

        current -= damage;

        if (current <= 0) {
            return true;
        }

        setCustomDurability(item, current, max);
        return false;
    }

    public static void repairItem(ItemStack item, int amount) {
        if (!hasCustomDurability(item)) return;

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);

        current = Math.min(current + amount, max);
        setCustomDurability(item, current, max);
    }

    public static void repairItemFull(ItemStack item) {
        if (!hasCustomDurability(item)) return;

        int max = getCustomMaxDurability(item);
        setCustomDurability(item, max, max);
    }

    private static void syncVisualDurability(ItemStack item, int current, int max) {
        if (item == null || item.getTypeId() == 0) return;

        if (isModItem(item)) {
            return;
        }

        short vanillaMaxDurability = item.getType().getMaxDurability();

        if (vanillaMaxDurability <= 0) {
            return;
        }

        double percentage = (double) current / (double) max;
        short vanillaDurability = (short) (vanillaMaxDurability - (int) (vanillaMaxDurability * percentage));
        item.setDurability(vanillaDurability);
    }

    public static void syncVisualDurabilityForModItem(ItemStack item, int current, int max) {
        if (item == null || item.getTypeId() == 0) return;
        if (isModItem(item)) return;

        short vanillaMax = item.getType().getMaxDurability();
        if (vanillaMax <= 0) return;

        double percentage = (double) current / (double) max;
        short mapped = (short) (vanillaMax - (int) (vanillaMax * percentage));
        item.setDurability(mapped);
    }

    public static boolean isModItem(ItemStack item) {
        if (item == null) return false;
        return item.getDurability() > 0 && item.getType().getMaxDurability() == 0;
    }

    public static String getDurabilityText(ItemStack item) {
        if (!hasCustomDurability(item)) return "";

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);
        double percentage = (double) current / (double) max * 100;

        return CC.translate("&a" + current + "/" + max + " (" + String.format("%.0f", percentage) + "%)");
    }

    public static void addDurabilityToLore(ItemStack item) {
    }

    public static void removeDurabilityFromLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;

        lore.removeIf(line -> {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            return cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)");
        });

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static void updateDurabilityLore(ItemStack item) {
        if (!hasCustomDurability(item)) return;
        if (isModItem(item)) return;

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);

        setCustomDurability(item, current, max);
    }
}