package org.debentialc.customitems.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.debentialc.service.CC;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;
import org.debentialc.customitems.tools.fragments.FragmentManager;
import org.debentialc.customitems.tools.fragments.TierFragment;
import org.debentialc.customitems.tools.permissions.Permissions;

import java.io.IOException;
import java.util.Map;

/**
 * Comando para crear fragmentos de tier que permiten upgradear armaduras
 *
 * EJEMPLOS DE USO:
 * - /fragment tier TIER_2 388/0    -> Crea fragmento para upgradear a TIER_2
 * - /fragment tier TIER_3 133/0    -> Crea fragmento para upgradear a TIER_3
 * - /fragment tier VIP 264/0       -> Crea fragmento para upgradear a VIP
 */
public class FragmentTierCommand extends BaseCommand {

    @Command(name = "fragmenttier", aliases = {"fragmenttier", "ftier"}, permission = Permissions.COMMAND + "fragment")
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
                if (command.length() < 3) {
                    player.sendMessage(CC.translate("&cUso: /ftier create <tier_objetivo> <material>"));
                    player.sendMessage(CC.translate("&7Ejemplo: /ftier create TIER_2 388/0"));
                    return;
                }
                createTierFragment(player, command.getArgs(1), command.getArgs(2));
                break;

            case "give":
                if (command.length() < 4) {
                    player.sendMessage(CC.translate("&cUso: /ftier give <jugador> <tier_objetivo> <cantidad>"));
                    return;
                }
                giveTierFragment(player, command.getArgs(1), command.getArgs(2), command.getArgs(3));
                break;

            case "info":
                showFragmentInfo(player);
                break;

            case "list":
                listAvailableTiers(player);
                break;

            default:
                sendHelp(player);
                break;
        }
    }

    private void createTierFragment(Player player, String targetTier, String materialStr) {
        targetTier = targetTier.toUpperCase();

        // Validar que el tier existe
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(targetTier)) {
            player.sendMessage(CC.translate("&c✗ Tier inválido: " + targetTier));
            player.sendMessage(CC.translate("&7Disponibles: " + String.join(", ", tiers.keySet())));
            return;
        }

        // Validar material (formato ID/DATA)
        if (!materialStr.matches("\\d+(/\\d+)?")) {
            player.sendMessage(CC.translate("&c✗ Formato de material inválido"));
            player.sendMessage(CC.translate("&7Formato: ID/DATA (ej: 388/0)"));
            return;
        }

        // Crear fragmento de tier
        TierFragment fragment = new TierFragment(
                "TIER_FRAG_" + targetTier,
                targetTier
        );

        fragment.setMaterial(materialStr);

        // Obtener color según tier
        String tierColor = getTierColor(targetTier);
        int tierNum = TierFragment.getTierNumber(targetTier);
        String tierDisplay = tierNum != 999 ? "Tier " + tierNum : targetTier;

        fragment.setDisplayName(tierColor + "⬆ Fragmento de Upgrade - " + tierDisplay);
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7Upgradea armadura a:"));
        fragment.getLore().add(CC.translate("&f  " + tierColor + "➤ " + tierDisplay));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7⚠ Requiere tier previo"));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&e⚡ Clic sobre armadura &7para aplicar"));

        ItemStack fragmentItem = fragment.toItemStack();

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), fragmentItem);
        } else {
            player.getInventory().addItem(fragmentItem);
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Fragmento de tier creado"));
        player.sendMessage(CC.translate("&7Tier objetivo: " + tierColor + tierDisplay));
        player.sendMessage("");
    }

    private void giveTierFragment(Player sender, String targetName, String targetTier, String amountStr) {
        Player target = main.getServer().getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(CC.translate("&c✗ Jugador no encontrado"));
            return;
        }

        targetTier = targetTier.toUpperCase();

        // Validar tier
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        if (!tiers.containsKey(targetTier)) {
            sender.sendMessage(CC.translate("&c✗ Tier inválido"));
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
        TierFragment fragment = new TierFragment(
                "TIER_FRAG_" + targetTier,
                targetTier
        );

        fragment.setMaterial("388/0"); // Material por defecto (EMERALD)

        String tierColor = getTierColor(targetTier);
        int tierNum = TierFragment.getTierNumber(targetTier);
        String tierDisplay = tierNum != 999 ? "Tier " + tierNum : targetTier;

        fragment.setDisplayName(tierColor + "⬆ Fragmento de Upgrade - " + tierDisplay);
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7Upgradea armadura a:"));
        fragment.getLore().add(CC.translate("&f  " + tierColor + "➤ " + tierDisplay));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7⚠ Requiere tier previo"));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&e⚡ Clic sobre armadura &7para aplicar"));

        ItemStack fragmentItem = fragment.toItemStack();
        fragmentItem.setAmount(amount);

        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItem(target.getLocation(), fragmentItem);
        } else {
            target.getInventory().addItem(fragmentItem);
        }

        sender.sendMessage(CC.translate("&a✓ Fragmento de tier entregado a " + target.getName()));
        target.sendMessage(CC.translate("&a✓ Has recibido x" + amount + " fragmento(s) de upgrade a " + tierDisplay));
    }

    private void showFragmentInfo(Player player) {
        ItemStack item = player.getItemInHand();

        if (!TierFragment.isTierFragment(item)) {
            player.sendMessage(CC.translate("&c✗ Sostén un fragmento de tier en la mano"));
            return;
        }

        String targetTier = TierFragment.getTargetTier(item);
        int tierNum = TierFragment.getTierNumber(targetTier);
        String tierDisplay = tierNum != 999 ? "Tier " + tierNum : targetTier;
        String tierColor = getTierColor(targetTier);

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Info del Fragmento de Tier"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Tier objetivo: " + tierColor + tierDisplay));
        player.sendMessage(CC.translate("&7Tier previo requerido: &f" + getPreviousTierName(targetTier)));
        player.sendMessage(CC.translate("&7Uso: &eClic &7sobre armadura"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void listAvailableTiers(Player player) {
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                .getTierConfig().getAllTiers();

        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Tiers Disponibles"));
        player.sendMessage(CC.translate("&8&m--------------------"));

        for (String tierName : tiers.keySet()) {
            int tierNum = TierFragment.getTierNumber(tierName);
            String tierColor = getTierColor(tierName);
            String display = tierNum != 999 ? "Tier " + tierNum : tierName;

            player.sendMessage(CC.translate(tierColor + "  • " + display));
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&7Usa: &e/ftier create <tier> <material>"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Fragmentos de Tier - Ayuda"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&e/ftier create <tier> <material>"));
        player.sendMessage(CC.translate("&7  Crea fragmento de tier"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/ftier give <jugador> <tier> <cant>"));
        player.sendMessage(CC.translate("&7  Da fragmentos de tier"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/ftier info"));
        player.sendMessage(CC.translate("&7  Info del fragmento en mano"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&e/ftier list"));
        player.sendMessage(CC.translate("&7  Lista tiers disponibles"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&3Importante:"));
        player.sendMessage(CC.translate("&7• Los upgrades son secuenciales"));
        player.sendMessage(CC.translate("&7• No se puede saltar tiers"));
        player.sendMessage(CC.translate("&7• Se mantienen stats y operaciones"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Material: &fID/DATA &7(ej: 388/0)"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    /**
     * Obtiene el color asociado a un tier
     */
    private String getTierColor(String tier) {
        int tierNum = TierFragment.getTierNumber(tier);

        switch (tierNum) {
            case 1: return "&7"; // Gris
            case 2: return "&a"; // Verde
            case 3: return "&b"; // Aqua
            case 4: return "&d"; // Rosa
            case 5: return "&6"; // Oro
            case 999: return "&5"; // Púrpura (VIP)
            default: return "&f"; // Blanco
        }
    }

    /**
     * Obtiene el nombre del tier previo requerido
     */
    private String getPreviousTierName(String tier) {
        int tierNum = TierFragment.getTierNumber(tier);

        if (tierNum == 1) {
            return "Ninguno (vanilla)";
        } else if (tierNum == 999) {
            Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance()
                    .getTierConfig().getAllTiers();

            int maxTier = 0;
            for (String t : tiers.keySet()) {
                int num = TierFragment.getTierNumber(t);
                if (num != 999 && num > maxTier) {
                    maxTier = num;
                }
            }

            return "TIER_" + maxTier;
        } else {
            return "TIER_" + (tierNum - 1);
        }
    }
}