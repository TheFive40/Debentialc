package org.debentialc.customitems.tools.nbt;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.service.CC;
import org.debentialc.customitems.tools.ci.CustomItem;

import java.util.List;

public class NbtItemBuilder {

    public static ItemStack buildItemStack(CustomItem customItem) {
        ItemStack itemStack = new ItemStack(customItem.getMaterial(), 1, customItem.getDurabilityData());

        if (customItem.getNbtData() != null && !customItem.getNbtData().isEmpty()) {
            NbtHandler nbt = new NbtHandler(itemStack);
            nbt.setCompoundFromString(customItem.getNbtData());
            itemStack = nbt.getItemStack();
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (customItem.getDisplayName() != null) {
                meta.setDisplayName(CC.translate(customItem.getDisplayName()));
            }
            if (customItem.getLore() != null) {
                meta.setLore(customItem.getLore());
            }
            itemStack.setItemMeta(meta);
        }

        if (customItem.isUnbreakable()) {
            applyUnbreakableNbt(itemStack);
        }

        if (customItem.getAttackDamage() >= 0) {
            applyAttackDamageNbt(itemStack, customItem.getAttackDamage());
        }

        if (customItem.getMaxDurability() > 0) {
            org.debentialc.customitems.tools.durability.CustomDurabilityManager
                    .setCustomMaxDurability(itemStack, customItem.getMaxDurability());
        }

        return itemStack;
    }

    public static void applyUnbreakableNbt(ItemStack item) {
        NbtHandler nbt = new NbtHandler(item);
        nbt.setBoolean("Unbreakable", true);
        ItemStack result = nbt.getItemStack();
        item.setItemMeta(result.getItemMeta());
        copyNbtToItem(item, result);
    }

    public static void removeUnbreakableNbt(ItemStack item) {
        NbtHandler nbt = new NbtHandler(item);
        nbt.setBoolean("Unbreakable", false);
        ItemStack result = nbt.getItemStack();
        copyNbtToItem(item, result);
    }

    public static void applyAttackDamageNbt(ItemStack item, int damage) {
        NbtHandler nbt = new NbtHandler(item);
        nbt.changeDamage(damage);
        ItemStack result = nbt.getItemStack();
        copyNbtToItem(item, result);
    }

    public static boolean isUnbreakableViaNbt(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return false;
        NbtHandler nbt = new NbtHandler(item);
        if (!nbt.hasNBT()) return false;
        return nbt.getBoolean("Unbreakable");
    }

    private static void copyNbtToItem(ItemStack target, ItemStack source) {
        if (source == null || target == null) return;
        NbtHandler sourceNbt = new NbtHandler(source);
        if (!sourceNbt.hasNBT()) return;
        NbtHandler targetNbt = new NbtHandler(target);
        targetNbt.setCompoundFromString(sourceNbt.getCompound().toString());
        ItemStack built = targetNbt.getItemStack();
        target.setItemMeta(built.getItemMeta());
    }

    public static String extractNbtString(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return null;
        NbtHandler nbt = new NbtHandler(item);
        if (!nbt.hasNBT() || nbt.getCompound() == null) return null;
        return nbt.getCompound().toString();
    }
}