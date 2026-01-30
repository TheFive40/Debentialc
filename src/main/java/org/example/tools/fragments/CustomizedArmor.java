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
    private String hash;
    private String tier;
    private Map<String, Integer> attributes;
    private Map<String, String> operations;
    private int materialType;
    private String armorSlot;
    private String displayName;

    private static final String HASH_TAG = "§8[ID:%s]";
    private static final String TIER_TAG = "§8[TIER:%s]";
    private static final String ATTR_TAG = "§8[ATTR:%s:%s:%d]"; // attr:op:value

    public CustomizedArmor() {
        this.attributes = new HashMap<>();
        this.operations = new HashMap<>();
    }

    public CustomizedArmor(String hash, String tier) {
        this.hash = hash;
        this.tier = tier;
        this.attributes = new HashMap<>();
        this.operations = new HashMap<>();
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
     * Lee los tags ocultos del formato: §8[ATTR:STR:*:115]
     *
     * IMPORTANTE: Retorna el valor TAL CUAL está guardado (115, 500, etc.)
     * NO hace ninguna conversión aquí
     */
    public static Map<String, Integer> getAttributes(ItemStack item) {
        Map<String, Integer> attributes = new HashMap<>();

        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return attributes;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[ATTR:")) {
                // Formato: §8[ATTR:STR:*:115] o §8[ATTR:CON:+:500]
                String clean = line.replace("§8[ATTR:", "").replace("]", "");
                String[] parts = clean.split(":");
                try {
                    if (parts.length >= 3) {
                        // Formato nuevo con operación: [ATTR:STR:*:115]
                        // parts[0] = "STR"
                        // parts[1] = "*" (operación)
                        // parts[2] = "115" (valor guardado)
                        attributes.put(parts[0], Integer.parseInt(parts[2]));
                    } else if (parts.length >= 2) {
                        // Formato viejo sin operación: [ATTR:STR:500]
                        attributes.put(parts[0], Integer.parseInt(parts[1]));
                    }
                } catch (Exception e) {
                    // Ignorar líneas mal formateadas
                }
            }
        }

        return attributes;
    }

    /**
     * Extrae las OPERACIONES de una armadura ItemStack
     * Retorna Map<String, String> donde key es el stat (STR, CON, etc) y value es la operación (+, -, *)
     */
    public static Map<String, String> getOperations(ItemStack item) {
        Map<String, String> operations = new HashMap<>();

        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return operations;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[ATTR:")) {
                String clean = line.replace("§8[ATTR:", "").replace("]", "");
                String[] parts = clean.split(":");
                try {
                    if (parts.length >= 3) {
                        operations.put(parts[0], parts[1]);
                    } else if (parts.length >= 2) {
                        operations.put(parts[0], "+");
                    }
                } catch (Exception e) {
                }
            }
        }

        return operations;
    }

    /**
     * Aplica los tags de personalización al lore de un ItemStack
     *
     * CORRECCIÓN: Para multiplicadores, mostrar el porcentaje correcto en el lore
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

            String cleanLine = line.replace("§7", "").replace("§f", "").replace("&7", "").replace("&f", "");
            if (cleanLine.contains("  • ") && (
                    cleanLine.contains("STR:") ||
                            cleanLine.contains("CON:") ||
                            cleanLine.contains("DEX:") ||
                            cleanLine.contains("WIL:") ||
                            cleanLine.contains("MND:") ||
                            cleanLine.contains("SPI:"))) {
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
                int storedValue = entry.getValue();
                String operation = operations.getOrDefault(entry.getKey(), "+");

                // CORRECCIÓN: Formatear correctamente según la operación
                String displayValue;
                if (operation.equals("*")) {
                    // MULTIPLICADOR: Convertir de vuelta a porcentaje
                    // storedValue = 115 -> mostrar 15%
                    double multiplier = storedValue / 100.0; // 115 -> 1.15
                    double percentage = (multiplier - 1.0) * 100.0; // 1.15 -> 15

                    if (percentage >= 0) {
                        displayValue = "&b+" + String.format("%.0f", percentage) + "%";
                    } else {
                        displayValue = "&b" + String.format("%.0f", percentage) + "%";
                    }
                } else if (operation.equals("-")) {
                    // Resta: mostrar con signo negativo
                    displayValue = "&c-" + storedValue;
                } else {
                    // Suma: mostrar con signo positivo
                    displayValue = "&a+" + storedValue;
                }

                lore.add(CC.translate("&7  • " + attrName + ": " + displayValue));
            }
            lore.add(CC.translate("&8&m--------------------"));
        }

        // PASO 3: Agregar tags ocultos al final
        lore.add(String.format(HASH_TAG, hash));
        lore.add(String.format(TIER_TAG, tier));

        for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
            String attr = entry.getKey();
            int value = entry.getValue();
            String op = operations.getOrDefault(attr, "+");
            lore.add(String.format(ATTR_TAG, attr, op, value));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Crea un CustomizedArmor desde un ItemStack existente
     * Lee todos los datos de los tags ocultos del lore
     */
    public static CustomizedArmor fromItemStack(ItemStack item) {
        String hash = getHash(item);
        String tier = getTier(item);

        if (hash == null || tier == null) return null;

        CustomizedArmor armor = new CustomizedArmor(hash, tier);
        armor.setMaterialType(item.getTypeId());
        armor.setArmorSlot(getArmorSlotFromItem(item));

        // Cargar atributos Y operaciones
        armor.attributes.putAll(getAttributes(item));
        armor.operations.putAll(getOperations(item));

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
            case "STR": return "STR";
            case "CON": return "CON";
            case "DEX": return "DEX";
            case "WIL": return "WIL";
            case "MND": return "MND";
            case "SPI": return "SPI";
            default: return attr;
        }
    }
}