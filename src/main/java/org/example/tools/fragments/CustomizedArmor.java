package org.example.tools.fragments;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.tools.CC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa una pieza de armadura personalizada con fragmentos
 */
@Getter
@Setter
public class CustomizedArmor {
    private String hash; // Identificador único de 12 caracteres
    private String tier; // Tier de la armadura (TIER_1, TIER_2, TIER_3, VIP, etc)
    private Map<String, Integer> attributes; // Atributos aplicados (STR:10, CON:5, etc)
    private int materialType; // Tipo de material de la armadura
    private String armorSlot; // HELMET, CHESTPLATE, LEGGINGS, BOOTS
    private String displayName; // Nombre de display de la armadura

    // Tags para identificar en el lore
    private static final String HASH_TAG = "§8[ID:%s]";
    private static final String TIER_TAG = "§8[TIER:%s]";
    private static final String ATTR_TAG = "§8[ATTR:%s:%d]";

    public CustomizedArmor() {
        this.attributes = new HashMap<>();
    }

    public CustomizedArmor(String hash, String tier) {
        this.hash = hash;
        this.tier = tier;
        this.attributes = new HashMap<>();
    }

    /**
     * Agrega un atributo a la armadura
     */
    public void addAttribute(String attribute, int value) {
        int current = attributes.getOrDefault(attribute, 0);
        attributes.put(attribute, current + value);
    }

    /**
     * Obtiene el valor actual de un atributo
     */
    public int getAttributeValue(String attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    /**
     * Verifica si un ItemStack es una armadura personalizada
     */
    public static boolean isCustomized(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;

        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.contains("[ID:")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extrae el hash de una armadura ItemStack
     */
    public static String getHash(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[ID:")) {
                // Formato: §8[ID:xxxxxxxxxxxx]
                String clean = line.replace("§8[ID:", "").replace("]", "");
                return clean;
            }
        }

        return null;
    }

    /**
     * Extrae el tier de una armadura ItemStack
     */
    public static String getTier(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[TIER:")) {
                String clean = line.replace("§8[TIER:", "").replace("]", "");
                return clean;
            }
        }

        return null;
    }

    /**
     * Extrae los atributos de una armadura ItemStack
     */
    public static Map<String, Integer> getAttributes(ItemStack item) {
        Map<String, Integer> attributes = new HashMap<>();

        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return attributes;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[ATTR:")) {
                // Formato: §8[ATTR:STR:10]
                String clean = line.replace("§8[ATTR:", "").replace("]", "");
                String[] parts = clean.split(":");
                try {
                    attributes.put(parts[0], Integer.parseInt(parts[1]));
                } catch (Exception e) {
                    // Ignorar líneas mal formateadas
                }
            }
        }

        return attributes;
    }

    /**
     * Aplica los tags de personalización al lore de un ItemStack
     */
    public void applyToItemStack(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(displayName);
        }


        lore.removeIf(line -> {
            // Tags ocultos
            if (line.contains("[ID:") || line.contains("[TIER:") || line.contains("[ATTR:")) {
                return true;
            }

            // Lore visible de fragmentos
            if (line.contains("&8&m--------------------") || line.contains("§8§m--------------------")) {
                return true;
            }
            if (line.contains("⚔ Atributos:")) {
                return true;
            }

            // Líneas de atributos específicos (formato: "  • Nombre: +valor")
            String cleanLine = line.replace("§7", "").replace("§f", "").replace("&7", "").replace("&f", "");
            if (cleanLine.contains("  • ") && (
                    cleanLine.contains("Fuerza:") ||
                            cleanLine.contains("Constitución:") ||
                            cleanLine.contains("Destreza:") ||
                            cleanLine.contains("Poder de Ki:") ||
                            cleanLine.contains("Mente:") ||
                            cleanLine.contains("Espíritu:"))) {
                return true;
            }

            return false;
        });

        // PASO 2: Agregar el lore visible ACTUALIZADO de atributos
        if (!attributes.isEmpty()) {
            lore.add(CC.translate("&8&m--------------------"));
            lore.add(CC.translate("&3⚔ Atributos:"));
            for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
                String attrName = getAttributeDisplayName(entry.getKey());
                int value = entry.getValue();
                String sign = value >= 0 ? "+" : "";
                lore.add(CC.translate("&7  • " + attrName + ": &f" + sign + value));
            }
            lore.add(CC.translate("&8&m--------------------"));
        }

        // PASO 3: Agregar tags ocultos al final
        lore.add(String.format(HASH_TAG, hash));
        lore.add(String.format(TIER_TAG, tier));

        for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
            lore.add(String.format(ATTR_TAG, entry.getKey(), entry.getValue()));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Crea un CustomizedArmor desde un ItemStack existente
     */
    public static CustomizedArmor fromItemStack(ItemStack item) {
        String hash = getHash(item);
        String tier = getTier(item);

        if (hash == null || tier == null) return null;

        CustomizedArmor armor = new CustomizedArmor(hash, tier);
        armor.setMaterialType(item.getTypeId());
        armor.setArmorSlot(getArmorSlotFromItem(item));
        armor.attributes.putAll(getAttributes(item));

        return armor;
    }

    /**
     * Obtiene el slot de armadura según el material
     */
    private static String getArmorSlotFromItem(ItemStack item) {
        String typeName = item.getType().name();
        if (typeName.contains("HELMET")) return "HELMET";
        if (typeName.contains("CHESTPLATE")) return "CHESTPLATE";
        if (typeName.contains("LEGGINGS")) return "LEGGINGS";
        if (typeName.contains("BOOTS")) return "BOOTS";
        return "UNKNOWN";
    }

    /**
     * Obtiene el nombre de display de un atributo
     */
    private String getAttributeDisplayName(String attr) {
        switch (attr.toUpperCase()) {
            case "STR": return "Fuerza";
            case "CON": return "Constitución";
            case "DEX": return "Destreza";
            case "WIL": return "Voluntad";
            case "MND": return "Mente";
            case "SPI": return "Espíritu";
            default: return attr;
        }
    }
}