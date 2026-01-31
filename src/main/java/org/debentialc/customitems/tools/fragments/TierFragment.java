package org.debentialc.customitems.tools.fragments;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.customitems.tools.CC;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un fragmento de upgrade de tier
 * Permite subir armaduras de un tier a otro secuencialmente
 */
@Getter
@Setter
public class TierFragment {
    private String id;
    private String targetTier; // Tier al que upgradea
    private String displayName;
    private int materialId;
    private short materialData;
    private List<String> lore;

    // Tag oculto que identifica al fragmento de tier
    private static final String TIER_FRAGMENT_TAG = "§8[TIER_FRAGMENT:%s]"; // targetTier

    public TierFragment(String id, String targetTier) {
        this.id = id;
        this.targetTier = targetTier.toUpperCase();
        this.lore = new ArrayList<>();
    }

    /**
     * Establece el material usando formato ID/DATA
     */
    public void setMaterial(String materialStr) {
        String[] parts = materialStr.split("/");
        this.materialId = Integer.parseInt(parts[0]);
        this.materialData = parts.length > 1 ? Short.parseShort(parts[1]) : 0;
    }

    /**
     * Convierte el fragmento en ItemStack
     */
    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(materialId, 1, materialData);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(CC.translate(displayName));

        List<String> finalLore = new ArrayList<>();
        if (lore != null) {
            finalLore.addAll(lore);
        }

        // Agregar tag oculto al final
        finalLore.add(String.format(TIER_FRAGMENT_TAG, targetTier));

        meta.setLore(finalLore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Verifica si un ItemStack es un fragmento de tier
     */
    public static boolean isTierFragment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;

        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.contains("[TIER_FRAGMENT:")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extrae el tier objetivo de un fragmento de tier
     */
    public static String getTargetTier(ItemStack item) {
        if (!isTierFragment(item)) return null;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[TIER_FRAGMENT:")) {
                // Formato: §8[TIER_FRAGMENT:TIER_2]
                String clean = line.replace("§8[TIER_FRAGMENT:", "").replace("]", "");
                return clean;
            }
        }

        return null;
    }

    /**
     * Obtiene el número de tier (TIER_1 = 1, TIER_2 = 2, etc.)
     * Retorna -1 si no se puede extraer
     */
    public static int getTierNumber(String tierName) {
        if (tierName == null) return -1;

        // Extraer número de tiers con formato TIER_X
        if (tierName.matches("^TIER_\\d+$")) {
            try {
                return Integer.parseInt(tierName.substring(5));
            } catch (Exception e) {
                return -1;
            }
        }

        // Para tiers especiales como VIP, retornar un número alto
        if (tierName.equalsIgnoreCase("VIP")) {
            return 999; // Tier especial, siempre el más alto
        }

        return -1;
    }

    /**
     * Valida que el tier actual de la armadura pueda upgradearse con este fragmento
     * @param currentTier Tier actual de la armadura
     * @param fragmentTargetTier Tier objetivo del fragmento
     * @return true si puede upgradearse
     */
    public static boolean canUpgrade(String currentTier, String fragmentTargetTier) {
        int currentNum = getTierNumber(currentTier);
        int targetNum = getTierNumber(fragmentTargetTier);

        if (currentNum == -1 || targetNum == -1) {
            return false;
        }

        // El tier objetivo debe ser exactamente 1 nivel superior
        return targetNum == currentNum + 1;
    }
}