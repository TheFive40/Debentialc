package org.debentialc.customitems.commands;

import org.bukkit.entity.Player;
import org.debentialc.customitems.tools.CC;
import org.debentialc.customitems.tools.commands.BaseCommand;
import org.debentialc.customitems.tools.commands.Command;
import org.debentialc.customitems.tools.commands.CommandArgs;
import org.debentialc.customitems.tools.fragments.FragmentManager;
import org.debentialc.customitems.tools.permissions.Permissions;

import java.io.IOException;
import java.util.Map;

/**
 * Comando para gestionar tiers de fragmentos
 */
public class TierManagerCommand extends BaseCommand {

    @Command(name = "tiermanager", aliases = {"tiermanager", "tm"}, permission = Permissions.COMMAND + "fragmentadmin")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        String arg0 = command.getArgs(0);

        switch (arg0.toLowerCase()) {
            case "create":
                if (command.length() < 2) {
                    player.sendMessage(CC.translate("&cUso: /tm create <nombre_tier>"));
                    player.sendMessage(CC.translate("&7Ejemplo: /tm create TIER_4"));
                    return;
                }
                createTier(player, command.getArgs(1));
                break;

            case "setlimit":
                if (command.length() < 4) {
                    player.sendMessage(CC.translate("&cUso: /tm setlimit <tier> <atributo> <valor>"));
                    player.sendMessage(CC.translate("&7Ejemplo: /tm setlimit TIER_4 STR 100"));
                    return;
                }
                setTierLimit(player, command.getArgs(1), command.getArgs(2), command.getArgs(3));
                break;

            case "setdefault":
                if (command.length() < 2) {
                    player.sendMessage(CC.translate("&cUso: /tm setdefault <tier>"));
                    player.sendMessage(CC.translate("&7Ejemplo: /tm setdefault TIER_1"));
                    return;
                }
                setDefaultTier(player, command.getArgs(1));
                break;

            case "list":
                listTiers(player);
                break;

            case "info":
                if (command.length() < 2) {
                    player.sendMessage(CC.translate("&cUso: /tm info <tier>"));
                    return;
                }
                showTierInfo(player, command.getArgs(1));
                break;

            case "delete":
                if (command.length() < 2) {
                    player.sendMessage(CC.translate("&cUso: /tm delete <tier>"));
                    player.sendMessage(CC.translate("&c⚠ ADVERTENCIA: Esto afectará todas las armaduras con este tier"));
                    return;
                }
                deleteTier(player, command.getArgs(1));
                break;

            case "reload":
                reload(player);
                break;

            default:
                sendHelp(player);
                break;
        }
    }

    private void createTier(Player player, String tierName) {
        tierName = tierName.toUpperCase();

        if (!tierName.matches("^[A-Z0-9_]+$")) {
            player.sendMessage(CC.translate("&c✗ Nombre inválido"));
            player.sendMessage(CC.translate("&7Solo letras mayúsculas, números y guiones bajos"));
            return;
        }

        Map<String, Map<String, Integer>> allTiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (allTiers.containsKey(tierName)) {
            player.sendMessage(CC.translate("&c✗ El tier &f" + tierName + " &cya existe"));
            return;
        }

        String[] attributes = {"STR", "CON", "DEX", "WIL", "MND", "SPI"};
        int defaultLimit = 10;

        boolean success = true;
        for (String attr : attributes) {
            boolean result = FragmentManager.getInstance().getTierConfig()
                    .setLimit(tierName, attr, defaultLimit);
            if (!result) {
                success = false;
                break;
            }
        }

        if (success) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Tier creado exitosamente"));
            player.sendMessage(CC.translate("&7Nombre: &f" + tierName));
            player.sendMessage(CC.translate("&7Límite inicial: &f" + defaultLimit + " &7(todos los atributos)"));
            player.sendMessage("");
            player.sendMessage(CC.translate("&7Usa &e/tm setlimit " + tierName + " <attr> <valor>"));
            player.sendMessage(CC.translate("&7para ajustar los límites"));
            player.sendMessage("");
        } else {
            player.sendMessage(CC.translate("&c✗ Error al crear el tier"));
        }
    }

    private void setTierLimit(Player player, String tier, String attribute, String valueStr) {
        tier = tier.toUpperCase();
        attribute = attribute.toUpperCase();

        // Validar tier
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c✗ Tier no encontrado: &f" + tier));
            player.sendMessage(CC.translate("&7Usa &e/tm list &7para ver tiers disponibles"));
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

    private void setDefaultTier(Player player, String tier) {
        tier = tier.toUpperCase();

        // Validar tier
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c✗ Tier no encontrado: &f" + tier));
            return;
        }

        boolean success = FragmentManager.getInstance().getTierConfig()
                .setDefaultTier(tier);

        if (success) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Tier por defecto actualizado"));
            player.sendMessage(CC.translate("&7Nuevo tier por defecto: &f" + tier));
            player.sendMessage("");
            player.sendMessage(CC.translate("&7Las nuevas armaduras vanilla convertidas"));
            player.sendMessage(CC.translate("&7usarán este tier automáticamente"));
            player.sendMessage("");
        } else {
            player.sendMessage(CC.translate("&c✗ Error al actualizar tier por defecto"));
        }
    }

    private void listTiers(Player player) {
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        String defaultTier = FragmentManager.getInstance().getTierConfig().getDefaultTier();

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Tiers Disponibles"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Tier por defecto: &f" + defaultTier));
        player.sendMessage("");

        for (String tierName : tiers.keySet()) {
            String prefix = tierName.equals(defaultTier) ? "&a★ " : "&7  ";
            player.sendMessage(CC.translate(prefix + "&f" + tierName));
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&7Usa &e/tm info <tier> &7para ver detalles"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void showTierInfo(Player player, String tier) {
        tier = tier.toUpperCase();

        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c✗ Tier no encontrado: &f" + tier));
            return;
        }

        Map<String, Integer> limits = tiers.get(tier);
        String defaultTier = FragmentManager.getInstance().getTierConfig().getDefaultTier();
        boolean isDefault = tier.equals(defaultTier);

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Información del Tier"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Nombre: &f" + tier + (isDefault ? " &a(Predeterminado)" : "")));
        player.sendMessage("");
        player.sendMessage(CC.translate("&3Límites por atributo:"));

        for (Map.Entry<String, Integer> entry : limits.entrySet()) {
            player.sendMessage(CC.translate("&7  " + entry.getKey() + ": &f" + entry.getValue()));
        }

        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void deleteTier(Player player, String tier) {
        tier = tier.toUpperCase();

        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c✗ Tier no encontrado: &f" + tier));
            return;
        }

        String defaultTier = FragmentManager.getInstance().getTierConfig().getDefaultTier();
        if (tier.equals(defaultTier)) {
            player.sendMessage(CC.translate("&c✗ No puedes eliminar el tier por defecto"));
            player.sendMessage(CC.translate("&7Primero cambia el tier por defecto con:"));
            player.sendMessage(CC.translate("&e/tm setdefault <otro_tier>"));
            return;
        }

        boolean success = FragmentManager.getInstance().getTierConfig()
                .deleteTier(tier);

        if (success) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&a✓ Tier eliminado"));
            player.sendMessage(CC.translate("&7Tier: &f" + tier));
            player.sendMessage("");
            player.sendMessage(CC.translate("&c⚠ ADVERTENCIA:"));
            player.sendMessage(CC.translate("&7Las armaduras con este tier pueden"));
            player.sendMessage(CC.translate("&7presentar errores. Considera cambiar"));
            player.sendMessage(CC.translate("&7su tier manualmente."));
            player.sendMessage("");
        } else {
            player.sendMessage(CC.translate("&c✗ Error al eliminar el tier"));
        }
    }

    private void reload(Player player) {
        try {
            FragmentManager.getInstance().reload();
            player.sendMessage(CC.translate("&a✓ Configuración de tiers recargada"));
        } catch (Exception e) {
            player.sendMessage(CC.translate("&c✗ Error al recargar: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Tier Manager - Ayuda"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&e/tm create <nombre>"));
        player.sendMessage(CC.translate("&7  Crea un nuevo tier"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/tm setlimit <tier> <attr> <valor>"));
        player.sendMessage(CC.translate("&7  Establece límite de atributo"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/tm setdefault <tier>"));
        player.sendMessage(CC.translate("&7  Establece tier por defecto"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/tm list"));
        player.sendMessage(CC.translate("&7  Lista todos los tiers"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/tm info <tier>"));
        player.sendMessage(CC.translate("&7  Muestra info de un tier"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/tm delete <tier>"));
        player.sendMessage(CC.translate("&7  Elimina un tier"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/tm reload"));
        player.sendMessage(CC.translate("&7  Recarga la configuración"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }
}