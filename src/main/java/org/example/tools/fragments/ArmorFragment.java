package org.example.tools.fragments;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.tools.CC;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un fragmento de mejora de atributo
 */
@Getter
@Setter
public class ArmorFragment {
    private String id;
    private String attribute; // STR, CON, DEX, WIL, MND, SPI
    private String value; // Valor con operador: "500", "-500", "15%"
    private String operation; // "+", "-", "*"
    private double numericValue; // Valor numérico procesado
    private String displayName;
    private int materialId;
    private short materialData;
    private List<String> lore;

    // Tag oculto que identifica al fragmento en el lore
    private static final String FRAGMENT_TAG = "§8[FRAGMENT:%s:%s:%s]"; // attr:op:value

    public ArmorFragment(String id, String attribute, String value) {
        this.id = id;
        this.attribute = attribute.toUpperCase();
        this.value = value;
        this.lore = new ArrayList<>();
        parseValue(value);
    }

    /**
     * Parsea el valor para determinar operación y valor numérico
     * Ejemplos: "500" -> +500, "-500" -> -500, "15%" -> *1.15
     */
    private void parseValue(String value) {
        value = value.trim();

        if (value.endsWith("%")) {
            // Multiplicativo: "15%" -> operación "*", valor 0.15
            this.operation = "*";
            String numStr = value.substring(0, value.length() - 1);
            this.numericValue = Double.parseDouble(numStr) / 100.0;
        } else if (value.startsWith("-")) {
            // Resta: "-500" -> operación "-", valor 500
            this.operation = "-";
            this.numericValue = Math.abs(Double.parseDouble(value));
        } else {
            // Suma: "500" -> operación "+", valor 500
            this.operation = "+";
            this.numericValue = Double.parseDouble(value);
        }
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
        finalLore.add(String.format(FRAGMENT_TAG, attribute, operation, value));

        meta.setLore(finalLore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Verifica si un ItemStack es un fragmento válido
     */
    public static boolean isFragment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;

        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.contains("[FRAGMENT:")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extrae el atributo de un fragmento ItemStack
     */
    public static String getFragmentAttribute(ItemStack item) {
        if (!isFragment(item)) return null;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[FRAGMENT:")) {
                // Formato: §8[FRAGMENT:STR:+:500]
                String clean = line.replace("§8[FRAGMENT:", "").replace("]", "");
                String[] parts = clean.split(":");
                return parts[0];
            }
        }

        return null;
    }

    /**
     * Extrae la operación de un fragmento ItemStack
     */
    public static String getFragmentOperation(ItemStack item) {
        if (!isFragment(item)) return null;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[FRAGMENT:")) {
                String clean = line.replace("§8[FRAGMENT:", "").replace("]", "");
                String[] parts = clean.split(":");
                return parts.length > 1 ? parts[1] : "+";
            }
        }

        return "+";
    }

    /**
     * Extrae el valor raw de un fragmento ItemStack
     */
    public static String getFragmentValueRaw(ItemStack item) {
        if (!isFragment(item)) return "0";

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("[FRAGMENT:")) {
                String clean = line.replace("§8[FRAGMENT:", "").replace("]", "");
                String[] parts = clean.split(":");
                return parts.length > 2 ? parts[2] : "0";
            }
        }

        return "0";
    }

    /**
     * Extrae el valor numérico procesado de un fragmento
     */
    public static double getFragmentValue(ItemStack item) {
        String raw = getFragmentValueRaw(item);
        String op = getFragmentOperation(item);

        try {
            if (raw.endsWith("%")) {
                // Multiplicativo
                String numStr = raw.substring(0, raw.length() - 1);
                return Double.parseDouble(numStr) / 100.0;
            } else {
                return Math.abs(Double.parseDouble(raw));
            }
        } catch (Exception e) {
            return 0;
        }
    }
}