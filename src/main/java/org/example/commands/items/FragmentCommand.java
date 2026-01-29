package org.example.commands.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.commands.BaseCommand;
import org.example.tools.commands.Command;
import org.example.tools.commands.CommandArgs;
import org.example.tools.fragments.ArmorFragment;
import org.example.tools.permissions.Permissions;

import java.io.IOException;
import java.util.Arrays;

/**
 * Comando para crear fragmentos de mejora
 */
public class FragmentCommand extends BaseCommand {

    @Command(name = "fragment", aliases = {"fragment", "frag"}, permission = Permissions.COMMAND + "fragment")
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
                if (command.length() < 4) {
                    player.sendMessage(CC.translate("&cUso: /fragment create <atributo> <valor> <material>"));
                    player.sendMessage(CC.translate("&7Ejemplo: /fragment create STR 500 4149/0"));
                    player.sendMessage(CC.translate("&7Ejemplo: /fragment create CON -100 4149/2"));
                    player.sendMessage(CC.translate("&7Ejemplo: /fragment create DEX 15% 4149/4"));
                    return;
                }
                createFragment(player, command.getArgs(1), command.getArgs(2), command.getArgs(3));
                break;

            case "give":
                if (command.length() < 5) {
                    player.sendMessage(CC.translate("&cUso: /fragment give <jugador> <atributo> <valor> <cantidad>"));
                    return;
                }
                giveFragment(player, command.getArgs(1), command.getArgs(2), command.getArgs(3), command.getArgs(4));
                break;

            case "info":
                showFragmentInfo(player);
                break;

            default:
                sendHelp(player);
                break;
        }
    }

    private void createFragment(Player player, String attribute, String value, String materialStr) {
        // Validar atributo
        String[] validAttrs = {"STR", "CON", "DEX", "WIL", "MND", "SPI"};
        if (!Arrays.asList(validAttrs).contains(attribute.toUpperCase())) {
            player.sendMessage(CC.translate("&c✗ Atributo inválido"));
            player.sendMessage(CC.translate("&7Válidos: STR, CON, DEX, WIL, MND, SPI"));
            return;
        }

        // Validar valor (puede ser: 500, -500, 15%)
        try {
            validateValue(value);
        } catch (Exception e) {
            player.sendMessage(CC.translate("&c✗ Valor inválido: " + e.getMessage()));
            player.sendMessage(CC.translate("&7Ejemplos: 500, -100, 15%"));
            return;
        }

        // Validar material (formato ID/DATA)
        if (!materialStr.matches("\\d+(/\\d+)?")) {
            player.sendMessage(CC.translate("&c✗ Formato de material inválido"));
            player.sendMessage(CC.translate("&7Formato: ID/DATA (ej: 4149/0)"));
            return;
        }

        // Crear fragmento
        ArmorFragment fragment = new ArmorFragment(
                "FRAG_" + attribute + "_" + value.replace("%", "P").replace("-", "N"),
                attribute,
                value
        );

        fragment.setMaterial(materialStr);

        // Determinar color y símbolo según operación
        String operationDisplay = getOperationDisplay(value);
        String colorCode = getOperationColor(value);

        fragment.setDisplayName("&3Fragmento de " + getAttributeName(attribute));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7Atributo: &f" + attribute));
        fragment.getLore().add(CC.translate("&7Valor: " + colorCode + operationDisplay));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&e⚡ Clic sobre armadura &7para aplicar"));

        ItemStack fragmentItem = fragment.toItemStack();

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), fragmentItem);
        } else {
            player.getInventory().addItem(fragmentItem);
        }

        player.sendMessage(CC.translate("&a✓ Fragmento creado exitosamente"));
    }

    private void giveFragment(Player sender, String targetName, String attribute, String value, String amountStr) {
        Player target = main.getServer().getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(CC.translate("&c✗ Jugador no encontrado"));
            return;
        }

        // Validar atributo
        String[] validAttrs = {"STR", "CON", "DEX", "WIL", "MND", "SPI"};
        if (!Arrays.asList(validAttrs).contains(attribute.toUpperCase())) {
            sender.sendMessage(CC.translate("&c✗ Atributo inválido"));
            return;
        }

        // Validar valor
        try {
            validateValue(value);
        } catch (Exception e) {
            sender.sendMessage(CC.translate("&c✗ Valor inválido: " + e.getMessage()));
            return;
        }

        // Validar cantidad
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                sender.sendMessage(CC.translate("&c✗ La cantidad debe ser mayor a 0"));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(CC.translate("&c✗ Cantidad inválida"));
            return;
        }

        // Crear fragmento
        ArmorFragment fragment = new ArmorFragment(
                "FRAG_" + attribute + "_" + value.replace("%", "P").replace("-", "N"),
                attribute,
                value
        );

        fragment.setMaterial("388/0"); // Material por defecto (EMERALD)

        String operationDisplay = getOperationDisplay(value);
        String colorCode = getOperationColor(value);

        fragment.setDisplayName("&3Fragmento de " + getAttributeName(attribute));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7Atributo: &f" + attribute));
        fragment.getLore().add(CC.translate("&7Valor: " + colorCode + operationDisplay));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&e⚡ Clic sobre armadura &7para aplicar"));

        ItemStack fragmentItem = fragment.toItemStack();
        fragmentItem.setAmount(amount);

        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItem(target.getLocation(), fragmentItem);
        } else {
            target.getInventory().addItem(fragmentItem);
        }

        sender.sendMessage(CC.translate("&a✓ Fragmento entregado a " + target.getName()));
        target.sendMessage(CC.translate("&a✓ Has recibido x" + amount + " fragmento(s) de " + attribute));
    }

    private void showFragmentInfo(Player player) {
        ItemStack item = player.getItemInHand();

        if (!ArmorFragment.isFragment(item)) {
            player.sendMessage(CC.translate("&c✗ Sostén un fragmento en la mano"));
            return;
        }

        String attr = ArmorFragment.getFragmentAttribute(item);
        String op = ArmorFragment.getFragmentOperation(item);
        String valueRaw = ArmorFragment.getFragmentValueRaw(item);

        String operationDisplay = getOperationDisplay(valueRaw);

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Info del Fragmento"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Atributo: &f" + attr));
        player.sendMessage(CC.translate("&7Operación: &f" + op));
        player.sendMessage(CC.translate("&7Valor: &f" + operationDisplay));
        player.sendMessage(CC.translate("&7Uso: &eClic &7sobre armadura"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Fragmentos - Ayuda"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&e/fragment create <attr> <valor> <material>"));
        player.sendMessage(CC.translate("&7  Crea un fragmento"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fragment give <jugador> <attr> <valor> <cant>"));
        player.sendMessage(CC.translate("&7  Da fragmentos"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/fragment info"));
        player.sendMessage(CC.translate("&7  Info del fragmento en mano"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Atributos: &fSTR, CON, DEX, WIL, MND, SPI"));
        player.sendMessage(CC.translate("&7Valores: &f500 &7(suma), &f-100 &7(resta), &f15% &7(%)"));
        player.sendMessage(CC.translate("&7Material: &fID/DATA &7(ej: 4149/0)"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private String getAttributeName(String attr) {
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

    private void validateValue(String value) throws Exception {
        if (value.endsWith("%")) {
            // Porcentaje
            String numStr = value.substring(0, value.length() - 1);
            Double.parseDouble(numStr);
        } else {
            // Número entero (positivo o negativo)
            Integer.parseInt(value);
        }
    }

    private String getOperationDisplay(String value) {
        if (value.endsWith("%")) {
            return value;
        } else if (value.startsWith("-")) {
            return value;
        } else {
            return "+" + value;
        }
    }

    private String getOperationColor(String value) {
        if (value.endsWith("%")) {
            return "&b"; // Azul para multiplicativo
        } else if (value.startsWith("-")) {
            return "&c"; // Rojo para resta
        } else {
            return "&a"; // Verde para suma
        }
    }
}