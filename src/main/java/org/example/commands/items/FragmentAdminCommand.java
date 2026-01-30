package org.example.commands.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.commands.BaseCommand;
import org.example.tools.commands.Command;
import org.example.tools.commands.CommandArgs;
import org.example.tools.fragments.CustomizedArmor;
import org.example.tools.fragments.FragmentManager;
import org.example.tools.permissions.Permissions;

import java.io.IOException;
import java.util.Map;

/**
 * Comando de administración para el sistema de fragmentos
 * VERSIÓN CORREGIDA: Mantiene operaciones al cambiar de tier
 */
public class FragmentAdminCommand extends BaseCommand {

    @Command(name = "fragmentadmin", aliases = {"fragmentadmin", "fadmin"}, permission = Permissions.COMMAND + "fragmentadmin")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        String arg0 = command.getArgs(0);

        switch (arg0.toLowerCase()) {
            case "info":
                showArmorInfo(player);
                break;

            case "settier":
                if (command.length() < 2) {
                    player.sendMessage(CC.translate("&cUso: /fadmin settier <tier>"));
                    player.sendMessage(CC.translate("&7Tiers: TIER_1, TIER_2, TIER_3, VIP"));
                    return;
                }
                setArmorTier(player, command.getArgs(1));
                break;

            case "reset":
                resetArmor(player);
                break;

            case "stats":
                showStats(player);
                break;

            case "reload":
                reload(player);
                break;

            case "limits":
                showLimits(player);
                break;

            case "setlimit":
                if (command.length() < 4) {
                    player.sendMessage(CC.translate("&cUso: /fadmin setlimit <tier> <atributo> <valor>"));
                    player.sendMessage(CC.translate("&7Ejemplo: /fadmin setlimit TIER_1 STR 50"));
                    return;
                }
                setTierLimit(player, command.getArgs(1), command.getArgs(2), command.getArgs(3));
                break;

            default:
                sendHelp(player);
                break;
        }
    }

    private void showArmorInfo(Player player) {
        ItemStack armor = player.getItemInHand();

        if (!CustomizedArmor.isCustomized(armor)) {
            player.sendMessage(CC.translate("&c✗ Sostén una armadura personalizada en la mano"));
            return;
        }

        CustomizedArmor customArmor = FragmentManager.getInstance().getCustomArmor(armor);

        if (customArmor == null) {
            player.sendMessage(CC.translate("&c✗ Error al cargar la armadura"));
            return;
        }

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Info de Armadura"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Hash: &f" + customArmor.getHash()));
        player.sendMessage(CC.translate("&7Tier: &f" + customArmor.getTier()));
        player.sendMessage(CC.translate("&7Slot: &f" + customArmor.getArmorSlot()));
        player.sendMessage("");

        if (customArmor.getAttributes().isEmpty()) {
            player.sendMessage(CC.translate("&7Sin atributos aplicados"));
        } else {
            player.sendMessage(CC.translate("&3Atributos:"));
            for (Map.Entry<String, Integer> entry : customArmor.getAttributes().entrySet()) {
                int limit = FragmentManager.getInstance().getTierConfig()
                        .getLimit(customArmor.getTier(), entry.getKey());
                int value = entry.getValue();
                String operation = customArmor.getOperations().getOrDefault(entry.getKey(), "+");

                // Formatear según operación
                String displayValue;
                if (operation.equals("*")) {
                    // Mostrar como porcentaje
                    double percentage = (value / 100.0 - 1.0) * 100.0;
                    displayValue = String.format("%+.0f%%", percentage);
                } else {
                    String sign = value >= 0 ? "+" : "";
                    displayValue = sign + value;
                }

                player.sendMessage(CC.translate("&7  " + entry.getKey() + ": &f" +
                        displayValue + " &8(" + operation + ")&7 / " + limit));
            }
        }

        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    /**
     * CORREGIDO: Mantiene las operaciones al cambiar de tier
     */
    private void setArmorTier(Player player, String tier) {
        ItemStack armor = player.getItemInHand();

        if (!CustomizedArmor.isCustomized(armor)) {
            player.sendMessage(CC.translate("&c✗ Sostén una armadura personalizada en la mano"));
            return;
        }

        tier = tier.toUpperCase();

        // Validar tier
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c✗ Tier inválido"));
            player.sendMessage(CC.translate("&7Disponibles: " + String.join(", ", tiers.keySet())));
            return;
        }

        // Cargar armadura desde ItemStack para obtener valores actuales
        CustomizedArmor customArmor = CustomizedArmor.fromItemStack(armor);

        if (customArmor == null) {
            player.sendMessage(CC.translate("&c✗ Error al cargar la armadura"));
            return;
        }

        String oldTier = customArmor.getTier();

        // IMPORTANTE: Solo cambiar el tier, NO tocar los atributos ni operaciones
        customArmor.setTier(tier);

        // Validar que los valores actuales no excedan los límites del nuevo tier
        boolean exceedsLimits = false;
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append(CC.translate("&c✗ Los siguientes atributos exceden los límites del tier " + tier + ":\n"));

        for (Map.Entry<String, Integer> entry : customArmor.getAttributes().entrySet()) {
            String attr = entry.getKey();
            int currentValue = entry.getValue();
            int newLimit = FragmentManager.getInstance().getTierConfig().getLimit(tier, attr);

            if (currentValue > newLimit) {
                exceedsLimits = true;
                String operation = customArmor.getOperations().getOrDefault(attr, "+");

                String displayValue;
                if (operation.equals("*")) {
                    double percentage = (currentValue / 100.0 - 1.0) * 100.0;
                    displayValue = String.format("%+.0f%%", percentage);
                } else {
                    displayValue = (currentValue >= 0 ? "+" : "") + currentValue;
                }

                errorMsg.append(CC.translate("&7  " + attr + ": &f" + displayValue + " &7(límite: " + newLimit + ")\n"));
            }
        }

        if (exceedsLimits) {
            player.sendMessage(errorMsg.toString());
            player.sendMessage(CC.translate("&7Usa fragmentos negativos para reducir los valores"));
            return;
        }

        // Aplicar cambios al ItemStack (esto actualiza el lore correctamente)
        customArmor.applyToItemStack(armor);

        // Guardar en almacenamiento
        FragmentManager.getInstance().getArmorStorage().saveArmor(customArmor);

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Tier actualizado"));
        player.sendMessage(CC.translate("&7Tier anterior: &f" + oldTier));
        player.sendMessage(CC.translate("&7Tier nuevo: &f" + tier));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Las operaciones y valores se han mantenido"));
        player.sendMessage("");
    }

    private void resetArmor(Player player) {
        ItemStack armor = player.getItemInHand();

        if (!CustomizedArmor.isCustomized(armor)) {
            player.sendMessage(CC.translate("&c✗ Sostén una armadura personalizada en la mano"));
            return;
        }

        String hash = CustomizedArmor.getHash(armor);

        // Eliminar del almacenamiento
        FragmentManager.getInstance().getArmorStorage().deleteArmor(hash);

        // Limpiar lore
        if (armor.hasItemMeta() && armor.getItemMeta().hasLore()) {
            ItemStack newArmor = armor.clone();
            newArmor.getItemMeta().setLore(null);
            player.setItemInHand(newArmor);
        }

        player.sendMessage(CC.translate("&a✓ Armadura reseteada a estado vanilla"));
    }

    private void showStats(Player player) {
        Map<String, Integer> stats = FragmentManager.getInstance()
                .getArmorStorage().getStats();

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Estadísticas"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Total: &f" +
                stats.getOrDefault("total_armors", 0)));
        player.sendMessage("");

        player.sendMessage(CC.translate("&3Por Tier:"));
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            if (!entry.getKey().equals("total_armors")) {
                player.sendMessage(CC.translate("&7  " + entry.getKey() + ": &f" + entry.getValue()));
            }
        }

        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void reload(Player player) {
        try {
            FragmentManager.getInstance().reload();
            player.sendMessage(CC.translate("&a✓ Sistema de fragmentos recargado"));
        } catch (Exception e) {
            player.sendMessage(CC.translate("&c✗ Error al recargar: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void showLimits(Player player) {
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Límites por Tier"));
        player.sendMessage(CC.translate("&8&m--------------------"));

        for (Map.Entry<String, Map<String, Integer>> tierEntry : tiers.entrySet()) {
            player.sendMessage(CC.translate("&3" + tierEntry.getKey() + ":"));

            for (Map.Entry<String, Integer> attrEntry : tierEntry.getValue().entrySet()) {
                player.sendMessage(CC.translate("&7  " + attrEntry.getKey() +
                        ": &f" + attrEntry.getValue()));
            }

            player.sendMessage("");
        }

        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Admin Fragmentos"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&e/fadmin info"));
        player.sendMessage(CC.translate("&7  Info de armadura en mano"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fadmin settier <tier>"));
        player.sendMessage(CC.translate("&7  Cambia tier (mantiene valores)"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fadmin reset"));
        player.sendMessage(CC.translate("&7  Resetea a vanilla"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fadmin stats"));
        player.sendMessage(CC.translate("&7  Estadísticas"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fadmin limits"));
        player.sendMessage(CC.translate("&7  Límites por tier"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fadmin setlimit <tier> <attr> <valor>"));
        player.sendMessage(CC.translate("&7  Modifica límite de tier"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fadmin reload"));
        player.sendMessage(CC.translate("&7  Recarga config"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void setTierLimit(Player player, String tier, String attribute, String valueStr) {
        tier = tier.toUpperCase();
        attribute = attribute.toUpperCase();

        // Validar tier
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c✗ Tier inválido"));
            player.sendMessage(CC.translate("&7Disponibles: " + String.join(", ", tiers.keySet())));
            return;
        }

        // Validar atributo
        String[] validAttrs = {"STR", "CON", "DEX", "WIL", "MND", "SPI"};
        boolean validAttr = false;
        for (String attr : validAttrs) {
            if (attr.equals(attribute)) {
                validAttr = true;
                break;
            }
        }

        if (!validAttr) {
            player.sendMessage(CC.translate("&c✗ Atributo inválido"));
            player.sendMessage(CC.translate("&7Válidos: STR, CON, DEX, WIL, MND, SPI"));
            return;
        }

        // Validar valor
        int value;
        try {
            value = Integer.parseInt(valueStr);
            if (value < 0) {
                player.sendMessage(CC.translate("&c✗ El valor debe ser mayor o igual a 0"));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&c✗ Valor inválido"));
            return;
        }

        // Aplicar cambio
        boolean success = FragmentManager.getInstance().getTierConfig()
                .setLimit(tier, attribute, value);

        if (success) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Límite actualizado"));
            player.sendMessage(CC.translate("&7Tier: &f" + tier));
            player.sendMessage(CC.translate("&7Atributo: &f" + attribute));
            player.sendMessage(CC.translate("&7Nuevo límite: &f" + value));
            player.sendMessage("");
        } else {
            player.sendMessage(CC.translate("&c✗ Error al actualizar el límite"));
        }
    }
}