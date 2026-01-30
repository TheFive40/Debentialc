package org.example.tools.fragments;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;

import java.util.Set;

public class FragmentManager {
    private static FragmentManager instance;
    private TierConfig tierConfig;
    private CustomArmorStorage armorStorage;

    public FragmentManager() {
        this.tierConfig = new TierConfig();
        this.armorStorage = new CustomArmorStorage();
    }

    public static FragmentManager getInstance() {
        if (instance == null) {
            instance = new FragmentManager();
        }
        return instance;
    }

    /**
     * Aplica un fragmento a una pieza de armadura
     * @return true si se aplicó exitosamente
     */
    public boolean applyFragment(Player player, ItemStack fragment, ItemStack armor) {
        // Validar fragmento
        if (!ArmorFragment.isFragment(fragment)) {
            player.sendMessage(CC.translate("&c✗ Este no es un fragmento válido"));
            return false;
        }

        // Validar que el item no sea aire
        if (armor == null || armor.getType() == Material.AIR) {
            player.sendMessage(CC.translate("&c✗ No hay armadura equipada en ese slot"));
            return false;
        }

        String attribute = ArmorFragment.getFragmentAttribute(fragment);
        String operation = ArmorFragment.getFragmentOperation(fragment);
        String valueRaw = ArmorFragment.getFragmentValueRaw(fragment);
        double value = ArmorFragment.getFragmentValue(fragment);

        CustomizedArmor customArmor;

        if (CustomizedArmor.isCustomized(armor)) {
            // Cargar desde el ItemStack directamente para tener los valores actuales
            customArmor = CustomizedArmor.fromItemStack(armor);

            if (customArmor == null) {
                player.sendMessage(CC.translate("&c✗ Error al cargar la armadura personalizada"));
                return false;
            }
        } else {
            customArmor = convertVanillaArmor(armor);
            player.sendMessage(CC.translate("&a✓ Armadura convertida a personalizada"));
        }

        // Obtener valor actual
        int currentValue = customArmor.getAttributeValue(attribute);
        int newValue;

        // CALCULAR NUEVO VALOR SEGÚN OPERACIÓN
        switch (operation) {
            case "+":
                // SUMA: currentValue + value
                newValue = currentValue + (int) value;
                break;
            case "-":
                // RESTA: currentValue - value
                newValue = currentValue - (int) value;
                break;
            case "*":
                // MULTIPLICADOR: Convertir a valor escalado y SUMAR
                // value = 1.15 (del fragmento), escalado = 115
                // currentValue = 115 (ya guardado previamente)
                // newValue = 115 + 115 = 230 (que sería 30% total al DBC)

                int scaledFragmentValue = (int) Math.round(value * 100);

                // Si NO hay valor previo con operación *, simplemente usar el del fragmento
                String currentOp = customArmor.getOperations().get(attribute);
                if (currentOp == null || !currentOp.equals("*")) {
                    // Primera vez con multiplicador en este atributo
                    newValue = scaledFragmentValue;
                } else {
                    // Ya existe multiplicador, SUMAR
                    newValue = currentValue + scaledFragmentValue;
                }
                break;
            default:
                newValue = currentValue + (int) value;
                break;
        }

        // VALIDAR LÍMITES DEL TIER
        // IMPORTANTE: Para multiplicadores, validamos el resultado final
        int limit = tierConfig.getLimit(customArmor.getTier(), attribute);

        if (newValue > limit) {
            player.sendMessage(CC.translate("&c✗ El valor excedería el límite"));
            player.sendMessage(CC.translate("&7Actual: &f" + currentValue + " &7| Nuevo: &f" + newValue + " &7| Límite: &f" + limit));
            player.sendMessage(CC.translate("&7Tier: &f" + customArmor.getTier()));

            // Mostrar detalle de la operación
            if (operation.equals("*")) {
                player.sendMessage(CC.translate("&7Multiplicador: &f" + value + "x &7(" + valueRaw + ")"));
            }

            return false;
        }

        // Validar que no sea negativo
        if (newValue < 0) {
            player.sendMessage(CC.translate("&c✗ El atributo no puede ser negativo"));
            player.sendMessage(CC.translate("&7Actual: &f" + currentValue + " &7| Operación: &f" + operation + valueRaw));
            return false;
        }

        // APLICAR EL FRAGMENTO
        // Para multiplicadores, guardamos el valor escalado (1.15 * 100 = 115)
        // Esto permite almacenar como entero y luego dividir por 100 al enviar al DBC
        if (operation.equals("*")) {
            // Guardar valor * 100 para mantener precisión
            int scaledValue = (int) Math.round(value * 100);
            customArmor.getAttributes().put(attribute, scaledValue);
        } else {
            customArmor.getAttributes().put(attribute, newValue);
        }

        // Guardar la operación
        customArmor.getOperations().put(attribute, operation);

        customArmor.applyToItemStack(armor);

        // Guardar en almacenamiento
        armorStorage.saveArmor(customArmor);

        // Consumir el fragmento
        if (fragment.getAmount() > 1) {
            fragment.setAmount(fragment.getAmount() - 1);
        } else {
            player.setItemInHand(new ItemStack(Material.AIR));
        }

        // Feedback detallado
        String operationSymbol;
        String actualValue;

        if (operation.equals("*")) {
            operationSymbol = valueRaw; // Mostrar "15%" tal cual
            // El valor guardado es escalado (115), mostrar el multiplicador real
            int scaledValue = (int) Math.round(value * 100);
            actualValue = String.format("%.2f", scaledValue / 100.0); // 115 -> "1.15"
        } else if (operation.equals("-")) {
            operationSymbol = "-" + valueRaw;
            actualValue = String.valueOf(newValue);
        } else {
            operationSymbol = "+" + valueRaw;
            actualValue = String.valueOf(newValue);
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Fragmento aplicado exitosamente"));
        player.sendMessage(CC.translate("&7Atributo: &f" + attribute + " &7Operación: &f" + operationSymbol));

        if (operation.equals("*")) {
            player.sendMessage(CC.translate("&7Multiplicador DBC: &fx" + actualValue));
            player.sendMessage(CC.translate("&7Valor en lore: &f" + valueRaw));
        } else {
            player.sendMessage(CC.translate("&7Antes: &f" + currentValue + " &7→ Ahora: &f" + newValue));
        }

        player.sendMessage(CC.translate("&7Límite del tier: &f" + limit));
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);

        return true;
    }

    /**
     * Convierte una armadura vanilla en armadura personalizada
     */
    private CustomizedArmor convertVanillaArmor(ItemStack armor) {
        // Generar hash único
        Set<String> existingHashes = armorStorage.getRegisteredHashes();
        String hash = HashGenerator.generateUniqueHash(existingHashes);

        // Obtener tier por defecto
        String defaultTier = tierConfig.getDefaultTier();

        // Crear armadura personalizada
        CustomizedArmor customArmor = new CustomizedArmor(hash, defaultTier);
        customArmor.setMaterialType(armor.getTypeId());
        customArmor.setArmorSlot(getArmorSlotFromMaterial(armor.getTypeId()));

        String displayName;
        if (armor.hasItemMeta() && armor.getItemMeta().hasDisplayName()) {
            displayName = armor.getItemMeta().getDisplayName();
        } else {
            displayName = getDefaultArmorName(armor.getTypeId());
        }
        customArmor.setDisplayName(displayName);

        return customArmor;
    }

    private String getArmorSlotFromMaterial(int materialId) {
        if (materialId == 298 || materialId == 302 || materialId == 306 ||
                materialId == 310 || materialId == 314) {
            return "HELMET";
        } else if (materialId == 299 || materialId == 303 || materialId == 307 ||
                materialId == 311 || materialId == 315) {
            return "CHESTPLATE";
        } else if (materialId == 300 || materialId == 304 || materialId == 308 ||
                materialId == 312 || materialId == 316) {
            return "LEGGINGS";
        } else if (materialId == 301 || materialId == 305 || materialId == 309 ||
                materialId == 313 || materialId == 317) {
            return "BOOTS";
        }
        return "UNKNOWN";
    }

    private String getDefaultArmorName(int materialId) {
        String material = "";
        String piece = "";

        if (materialId >= 298 && materialId <= 301) {
            material = "Leather";
        } else if (materialId >= 302 && materialId <= 305) {
            material = "Chain";
        } else if (materialId >= 306 && materialId <= 309) {
            material = "Iron";
        } else if (materialId >= 310 && materialId <= 313) {
            material = "Diamond";
        } else if (materialId >= 314 && materialId <= 317) {
            material = "Gold";
        } else {
            material = "Custom";
        }

        int remainder = materialId % 4;
        switch (remainder) {
            case 2: piece = "Helmet"; break;
            case 3: piece = "Chestplate"; break;
            case 0: piece = "Leggings"; break;
            case 1: piece = "Boots"; break;
            default: piece = "Armor"; break;
        }

        return CC.translate("&7" + material + " " + piece);
    }

    public CustomizedArmor getCustomArmor(ItemStack item) {
        if (!CustomizedArmor.isCustomized(item)) return null;
        String hash = CustomizedArmor.getHash(item);
        return armorStorage.loadArmor(hash);
    }

    public TierConfig getTierConfig() {
        return tierConfig;
    }

    public CustomArmorStorage getArmorStorage() {
        return armorStorage;
    }

    public void reload() {
        tierConfig.reload();
        armorStorage.reload();
    }
}