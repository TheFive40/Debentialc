package org.debentialc.customitems.tools.durability;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.service.CC;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de durabilidad personalizada para items
 * Usa el formato visible "X/Y (Z%)" para almacenar y extraer datos
 */
public class CustomDurabilityManager {

    private static final String UNBREAKABLE_KEY = "§8[UNBREAKABLE]";

    /**
     * Establece un item como irrompible
     */
    public static void setUnbreakable(ItemStack item, boolean unbreakable) {
        if (item == null || item.getTypeId() == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        // Remover tag anterior de irrompible
        lore.removeIf(line -> line.equals(UNBREAKABLE_KEY));

        // Agregar tag si es irrompible
        if (unbreakable) {
            lore.add(UNBREAKABLE_KEY);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        // Si es irrompible, restaurar durabilidad visual al máximo
        if (unbreakable && item.getType().getMaxDurability() > 0) {
            item.setDurability((short) 0);
        }
    }

    /**
     * Verifica si un item es irrompible
     */
    public static boolean isUnbreakable(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return false;
        if (!item.hasItemMeta()) return false;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;

        return lore.contains(UNBREAKABLE_KEY);
    }

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

        // Remover línea de durabilidad anterior si existe
        lore.removeIf(line -> {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            return cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)");
        });

        // Agregar nueva línea de durabilidad al principio
        double percentage = (double) current / (double) max * 100;
        String durabilityLine = CC.translate("&a" + current + "/" + max + " (" + String.format("%.0f", percentage) + "%)");
        lore.add(0, durabilityLine);

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
        if (lore == null || lore.isEmpty()) return 0;

        // Buscar línea con patrón X/Y (Z%)
        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", ""); // Remover códigos de color
            if (cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)")) {
                try {
                    // Extraer el número antes de la barra "/"
                    String[] parts = cleanLine.split("/");
                    return Integer.parseInt(parts[0].trim());
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
        if (lore == null || lore.isEmpty()) return 0;

        // Buscar línea con patrón X/Y (Z%)
        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", ""); // Remover códigos de color
            if (cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)")) {
                try {
                    // Extraer el número después de la barra "/" y antes del espacio
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

    /**
     * Verifica si un item tiene durabilidad personalizada
     */
    public static boolean hasCustomDurability(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return false;
        if (!item.hasItemMeta()) return false;

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return false;

        // Buscar línea con patrón X/Y (Z%)
        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            if (cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)")) {
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
        // Si el item es irrompible, no hacer nada
        if (isUnbreakable(item)) {
            return false;
        }

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
     * Formato: "500/500 (100%)" siempre en verde
     */
    public static String getDurabilityText(ItemStack item) {
        if (!hasCustomDurability(item)) return "";

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);
        double percentage = (double) current / (double) max * 100;

        // Siempre en verde, formato simple
        return CC.translate("&a" + current + "/" + max + " (" + String.format("%.0f", percentage) + "%)");
    }

    /**
     * Agrega el texto de durabilidad al lore visible del item
     */
    public static void addDurabilityToLore(ItemStack item) {
        if (!hasCustomDurability(item)) {
            // Si no tiene durabilidad custom aún, no hacer nada
            // setCustomDurability() ya agrega la línea automáticamente
            return;
        }
        // La línea de durabilidad ya está en el lore gracias a setCustomDurability()
        // Este método solo se mantiene por compatibilidad de API
    }

    /**
     * Remueve el texto de durabilidad del lore visible
     * Busca líneas con el patrón: "número/número (número%)"
     */
    public static void removeDurabilityFromLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;

        // Remover líneas que coincidan con el patrón de durabilidad
        // Formato: "§aX/Y (Z%)" o sin color
        lore.removeIf(line -> {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", ""); // Remover códigos de color
            return cleanLine.matches("\\d+/\\d+ \\(\\d+%\\)");
        });

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Actualiza el texto de durabilidad en el lore
     */
    public static void updateDurabilityLore(ItemStack item) {
        if (!hasCustomDurability(item)) return;

        int current = getCustomDurability(item);
        int max = getCustomMaxDurability(item);

        // setCustomDurability ya maneja la actualización del lore
        setCustomDurability(item, current, max);
    }
}