package org.debentialc.customitems.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.debentialc.customitems.tools.CC;
import org.debentialc.customitems.tools.commands.BaseCommand;
import org.debentialc.customitems.tools.commands.Command;
import org.debentialc.customitems.tools.commands.CommandArgs;
import org.debentialc.customitems.tools.fragments.CustomizedArmor;
import org.debentialc.customitems.tools.fragments.FragmentManager;
import org.debentialc.customitems.tools.permissions.Permissions;

import java.io.IOException;

/**
 * Comando de debug para verificar el sistema de fragmentos
 */
public class FragmentDebugCommand extends BaseCommand {

    @Command(name = "fdebug", aliases = {"fdebug"}, permission = Permissions.COMMAND + "fragmentadmin")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        player.sendMessage(CC.translate("&3=== DEBUG FRAGMENTOS ==="));
        player.sendMessage("");

        // Verificar armadura equipada
        ItemStack[] armor = player.getInventory().getArmorContents();
        player.sendMessage(CC.translate("&7Armadura equipada:"));

        for (int i = 0; i < 4; i++) {
            String slot = getSlotName(i);
            if (armor[i] != null && armor[i].getTypeId() != 0) {
                player.sendMessage(CC.translate("&7  " + slot + ": &f" + armor[i].getType() + " (ID: " + armor[i].getTypeId() + ")"));

                // Ver si tiene lore
                if (armor[i].hasItemMeta() && armor[i].getItemMeta().hasLore()) {
                    player.sendMessage(CC.translate("&7    Lore: " + armor[i].getItemMeta().getLore().size() + " líneas"));

                    // Verificar si es custom
                    if (CustomizedArmor.isCustomized(armor[i])) {
                        String hash = CustomizedArmor.getHash(armor[i]);
                        String tier = CustomizedArmor.getTier(armor[i]);
                        player.sendMessage(CC.translate("&7    &a✓ ES CUSTOM"));
                        player.sendMessage(CC.translate("&7    Hash: &f" + hash));
                        player.sendMessage(CC.translate("&7    Tier: &f" + tier));

                        // Ver atributos
                        CustomizedArmor customArmor = FragmentManager.getInstance().getCustomArmor(armor[i]);
                        if (customArmor != null && !customArmor.getAttributes().isEmpty()) {
                            player.sendMessage(CC.translate("&7    Atributos:"));
                            customArmor.getAttributes().forEach((attr, val) -> {
                                player.sendMessage(CC.translate("&7      " + attr + ": &f" + val));
                            });
                        }
                    } else {
                        player.sendMessage(CC.translate("&7    &c✗ NO ES CUSTOM"));
                    }
                } else {
                    player.sendMessage(CC.translate("&7    &cSin lore"));
                }
            } else {
                player.sendMessage(CC.translate("&7  " + slot + ": &cVacío"));
            }
            player.sendMessage("");
        }

        player.sendMessage(CC.translate("&3====================="));
    }

    private String getSlotName(int slot) {
        switch (slot) {
            case 0: return "Botas";
            case 1: return "Pantalones";
            case 2: return "Pechera";
            case 3: return "Casco";
            default: return "Desconocido";
        }
    }
}