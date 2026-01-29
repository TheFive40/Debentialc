package org.example.tools.fragments;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;

import java.util.Set;

/**
 * Gestor principal del sistema de fragmentos de armadura
 */
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

        // Verificar si la armadura ya está personalizada
        CustomizedArmor customArmor;

        if (CustomizedArmor.isCustomized(armor)) {
            // Cargar armadura existente
            String hash = CustomizedArmor.getHash(armor);
            customArmor = armorStorage.loadArmor(hash);

            if (customArmor == null) {
                player.sendMessage(CC.translate("&c✗ Error al cargar la armadura personalizada"));
                return false;
            }
        } else {
            // Convertir armadura a custom
            customArmor = convertVanillaArmor(armor);
            player.sendMessage(CC.translate("&a✓ Armadura convertida a personalizada"));
        }

        // Calcular nuevo valor según operación
        int currentValue = customArmor.getAttributeValue(attribute);
        int newValue;

        switch (operation) {
            case "+":
                // Suma
                newValue = currentValue + (int) value;
                break;
            case "-":
                // Resta
                newValue = currentValue - (int) value;
                break;
            case "*":
                // Multiplicativo (porcentaje)
                newValue = currentValue + (int) (currentValue * value);
                break;
            default:
                newValue = currentValue + (int) value;
                break;
        }

        // Para validar límites, usamos el nuevo valor
        if (!tierConfig.canApply(customArmor.getTier(), attribute, 0, newValue)) {
            int limit = tierConfig.getLimit(customArmor.getTier(), attribute);
            player.sendMessage(CC.translate("&c✗ El valor excedería el límite"));
            player.sendMessage(CC.translate("&7Actual: &f" + currentValue + " &7| Nuevo: &f" + newValue + " &7| Límite: &f" + limit));
            player.sendMessage(CC.translate("&7Tier: &f" + customArmor.getTier()));
            return false;
        }

        // Si el nuevo valor es negativo, no permitir
        if (newValue < 0) {
            player.sendMessage(CC.translate("&c✗ El atributo no puede ser negativo"));
            player.sendMessage(CC.translate("&7Actual: &f" + currentValue + " &7| Operación: &f" + operation + valueRaw));
            return false;
        }

        // Aplicar el fragmento
        customArmor.getAttributes().put(attribute, newValue);
        customArmor.applyToItemStack(armor);

        // Guardar en almacenamiento
        armorStorage.saveArmor(customArmor);

        // Consumir el fragmento
        if (fragment.getAmount() > 1) {
            fragment.setAmount(fragment.getAmount() - 1);
        } else {
            player.setItemInHand(new ItemStack(Material.AIR));
        }

        // Feedback
        String operationSymbol = operation.equals("*") ? valueRaw : operation + valueRaw;
        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Fragmento aplicado exitosamente"));
        player.sendMessage(CC.translate("&7Atributo: &f" + attribute + " &7Operación: &f" + operationSymbol));
        player.sendMessage(CC.translate("&7Antes: &f" + currentValue + " &7→ Ahora: &f" + newValue));
        player.sendMessage(CC.translate("&7Límite del tier: &f" + tierConfig.getLimit(customArmor.getTier(), attribute)));
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);

        return true;
    }

    /**
     * Convierte una armadura en armadura personalizada
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

        return customArmor;
    }

    /**
     * Obtiene el slot de armadura según el material ID
     * Compatible con armaduras de mods
     */
    private String getArmorSlotFromMaterial(int materialId) {
        // IDs vanilla de armadura
        // Helmets: 298, 302, 306, 310, 314
        // Chestplates: 299, 303, 307, 311, 315
        // Leggings: 300, 304, 308, 312, 316
        // Boots: 301, 305, 309, 313, 317

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

        // Para armaduras de mods, intentar detectar por nombre del tipo
        // Si no se puede detectar, usar UNKNOWN
        return "UNKNOWN";
    }

    /**
     * Obtiene información de una armadura personalizada
     */
    public CustomizedArmor getCustomArmor(ItemStack item) {
        if (!CustomizedArmor.isCustomized(item)) return null;

        String hash = CustomizedArmor.getHash(item);
        return armorStorage.loadArmor(hash);
    }

    /**
     * Obtiene la configuración de tiers
     */
    public TierConfig getTierConfig() {
        return tierConfig;
    }

    /**
     * Obtiene el almacenamiento
     */
    public CustomArmorStorage getArmorStorage() {
        return armorStorage;
    }

    /**
     * Recarga toda la configuración
     */
    public void reload() {
        tierConfig.reload();
        armorStorage.reload();
    }
}