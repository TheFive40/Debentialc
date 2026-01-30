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
 *
 * SISTEMA DE VALORES:
 * - Porcentajes (ej: "20%"): Se convierten a multiplicador 1.20 internamente
 * - Positivos (ej: "500"): Operación de suma (+500)
 * - Negativos (ej: "-200"): Operación de resta (-200)
 */
@Getter
@Setter
public class ArmorFragment {
    private String id;
    private String attribute; // STR, CON, DEX, WIL, MND, SPI
    private String value; // Valor con operador: "500", "-500", "15%"
    private String operation; // "+", "-", "*"
    private double numericValue; // Valor numérico REAL a aplicar
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
     *
     * SISTEMA CORRECTO:
     * - "15%" -> operación "*", valor numérico 1.15 (multiplicador para DBC)
     * - "-20%" -> operación "*", valor numérico 0.80 (multiplicador para DBC)
     * - "500" -> operación "+", valor 500
     * - "-200" -> operación "-", valor 200
     */
    private void parseValue(String value) {
        value = value.trim();

        if (value.endsWith("%")) {
            // MULTIPLICATIVO: Convertir % a multiplicador
            this.operation = "*";
            String numStr = value.substring(0, value.length() - 1);
            double percentage = Double.parseDouble(numStr);

            // CONVERSIÓN A MULTIPLICADOR:
            // 15% -> 1.0 + (15/100) = 1.15
            // -20% -> 1.0 + (-20/100) = 0.80
            // 100% -> 1.0 + (100/100) = 2.0
            this.numericValue = 1.0 + (percentage / 100.0);
        } else if (value.startsWith("-")) {
            // RESTA: "-500" -> operación "-", valor 500
            this.operation = "-";
            this.numericValue = Math.abs(Double.parseDouble(value));
        } else {
            // SUMA: "500" -> operación "+", valor 500
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
     * Extrae el valor RAW (original) de un fragmento ItemStack
     * Este es el valor que se muestra al usuario (ej: "20%", "500", "-200")
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
     * Extrae el valor numérico REAL procesado de un fragmento
     *
     * SISTEMA CORRECTO PARA DBC:
     * - "15%" retorna 1.15 (multiplicador para DBC)
     * - "-20%" retorna 0.80 (multiplicador para DBC)
     * - "500" retorna 500 (valor aditivo)
     * - "-200" retorna 200 (valor sustractivo)
     */
    public static double getFragmentValue(ItemStack item) {
        String raw = getFragmentValueRaw(item);

        try {
            if (raw.endsWith("%")) {
                // PORCENTAJE: Convertir a multiplicador
                String numStr = raw.substring(0, raw.length() - 1);
                double percentage = Double.parseDouble(numStr);

                // 15% -> 1.0 + (15/100) = 1.15
                // -20% -> 1.0 + (-20/100) = 0.80
                return 1.0 + (percentage / 100.0);
            } else {
                // VALOR DIRECTO: +500 o -200
                return Math.abs(Double.parseDouble(raw));
            }
        } catch (Exception e) {
            return 0.0;
        }
    }
}