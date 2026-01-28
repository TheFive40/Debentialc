package org.example.tools.durability;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.tools.CC;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de durabilidad personalizada para items
 * Simula durabilidad extendida usando NBT tags personalizados
 */
public class CustomDurabilityManager {

    // Keys para NBT (usando lore como fallback para 1.7.10)
    private static final String CUSTOM_DURABILITY_KEY = "§8[DUR:";
    private static final String CUSTOM_MAX_DURABILITY_KEY = "§8[MAX:";

    /**
     * Establece la durabilidad máxima personalizada de un item
     * @param item ItemStack a modificar
     * @param maxDurability Durabilidad máxima personalizada
     */
    public static void setCustomMaxDurability(ItemStack item, int maxDurability) {
        if (item == null || item.getTypeId() == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Establecer durabilidad actual igual a la máxima al inicio
        setCustomDurability(item, maxDurability, maxDurability);
    }

    /**
     * Establece la durabilidad actual y máxima del item
     * @param item ItemStack a modificar
     * @param current Durabilidad actual
     * @param max Durabilidad máxima
     */
    public static void setCustomDurability(ItemStack item, int current, int max) {
        if (item == null || item.getTypeId() == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Asegurar que current no exceda max ni sea negativo
        current = Math.max(0, Math.min(current, max));

        // Obtener lore existente
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        // Remover datos antiguos de durabilidad del lore
        lore.removeIf(line -> line.startsWith(CUSTOM_DURABILITY_KEY) || line.startsWith(CUSTOM_MAX_DURABILITY_KEY));

        // Agregar nuevos datos de durabilidad (ocultos en el lore)
        lore.add(CUSTOM_DURABILITY_KEY + current + "]");
        lore.add(CUSTOM_MAX_DURABILITY_KEY + max + "]");

        meta.setLore(lore);
        item.setItemMeta(meta);

        // Sincronizar barra de durabilidad visual
        syncVisualDurability(item, current, max);
    }

    /**
     * Obtiene la durabilidad actual del item
     */
    public static int getCustomDurability(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return 0;
        if (!item.hasItemMeta()) return 0;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            if (line.startsWith(CUSTOM_DURABILITY_KEY)) {
                try {
                    String value = line.substring(CUSTOM_DURABILITY_KEY.length(), line.length() - 1);
                    return Integer.parseInt(value);
                } catch (Exception e) {
                    return 0;
                }
            }
        }

        return 0;
    }

    /**
     * Obtiene la durabilidad máxima del item
     */
    public static int getCustomMaxDurability(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return 0;
        if (!item.hasItemMeta()) return 0;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            if (line.startsWith(CUSTOM_MAX_DURABILITY_KEY)) {
                try {
                    String value = line.substring(CUSTOM_MAX_DURABILITY_KEY.length(), line.length() - 1);
                    return Integer.parseInt(value);
                } catch (Exception e) {
                    return 0;
                }
            }
        }

        return 0;
    }

    /**
     * Verifica si un item tiene durabilidad personalizada
     */
    public static boolean hasCustomDurability(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return false;
        if (!item.hasItemMeta()) return false;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;

        for (String line : lore) {
            if (line.startsWith(CUSTOM_DURABILITY_KEY)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Daña el item (reduce durabilidad)
     * @param item Item a dañar
     * @param damage Cantidad de daño
     * @return true si el item se rompió
     */
    public static boolean damageItem(ItemStack item, int damage) {
        if (!hasCustomDurability(item)) return false;

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);

        current -= damage;

        if (current <= 0) {
            // Item roto
            return true;
        }

        setCustomDurability(item, current, max);
        return false;
    }

    /**
     * Repara el item (aumenta durabilidad)
     */
    public static void repairItem(ItemStack item, int amount) {
        if (!hasCustomDurability(item)) return;

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);

        current = Math.min(current + amount, max);
        setCustomDurability(item, current, max);
    }

    /**
     * Repara completamente el item
     */
    public static void repairItemFull(ItemStack item) {
        if (!hasCustomDurability(item)) return;

        int max = getCustomMaxDurability(item);
        setCustomDurability(item, max, max);
    }

    /**
     * Sincroniza la barra de durabilidad visual con la durabilidad custom
     * Mapea la durabilidad custom a la durabilidad vanilla del material
     */
    private static void syncVisualDurability(ItemStack item, int current, int max) {
        if (item == null || item.getTypeId() == 0) return;

        // Obtener durabilidad máxima del material vanilla
        short vanillaMaxDurability = item.getType().getMaxDurability();

        if (vanillaMaxDurability == 0) {
            // Item no tiene durabilidad vanilla (como comida, bloques, etc)
            return;
        }

        // Calcular porcentaje de durabilidad custom
        double percentage = (double) current / (double) max;

        // Mapear a durabilidad vanilla
        short vanillaDurability = (short) (vanillaMaxDurability - (vanillaMaxDurability * percentage));

        // Aplicar durabilidad visual
        item.setDurability(vanillaDurability);
    }

    /**
     * Obtiene un texto formateado de la durabilidad para mostrar
     */
    public static String getDurabilityText(ItemStack item) {
        if (!hasCustomDurability(item)) return "";

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);
        double percentage = (double) current / (double) max * 100;

        String color;
        if (percentage > 75) {
            color = "&a"; // Verde
        } else if (percentage > 50) {
            color = "&e"; // Amarillo
        } else if (percentage > 25) {
            color = "&6"; // Naranja
        } else {
            color = "&c"; // Rojo
        }

        return CC.translate(color + "Durabilidad: " + current + "/" + max + " (" + String.format("%.1f", percentage) + "%)");
    }

    /**
     * Agrega el texto de durabilidad al lore visible del item
     */
    public static void addDurabilityToLore(ItemStack item) {
        if (!hasCustomDurability(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        // Remover texto de durabilidad anterior del lore visible
        lore.removeIf(line -> line.contains("Durabilidad:"));

        // Agregar nuevo texto de durabilidad al principio del lore visible
        String durabilityText = getDurabilityText(item);
        lore.add(0, durabilityText);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Remueve el texto de durabilidad del lore visible
     */
    public static void removeDurabilityFromLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;

        lore.removeIf(line -> line.contains("Durabilidad:"));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Actualiza el texto de durabilidad en el lore si existe
     */
    public static void updateDurabilityLore(ItemStack item) {
        if (!hasCustomDurability(item)) return;

        removeDurabilityFromLore(item);
        addDurabilityToLore(item);
    }
}