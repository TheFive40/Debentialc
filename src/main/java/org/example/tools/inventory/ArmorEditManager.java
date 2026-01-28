package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.ci.CustomArmor;
import org.example.tools.pastebin.PastebinReader;
import org.example.tools.storage.CustomArmorStorage;
import org.example.commands.items.RegisterItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ArmorEditManager {

    public static class ArmorEditState {
        public String armorId;
        public String editType;
        public int lineNumber;

        public ArmorEditState(String armorId, String editType) {
            this.armorId = armorId;
            this.editType = editType;
        }

        public ArmorEditState(String armorId, String editType, int lineNumber) {
            this.armorId = armorId;
            this.editType = editType;
            this.lineNumber = lineNumber;
        }
    }

    private static final HashMap<UUID, ArmorEditState> playersEditing = new HashMap<>();

    public static void startArmorEdit(Player player, String armorId, String editType) {
        playersEditing.put(player.getUniqueId(), new ArmorEditState(armorId, editType));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&b&l  Editar Armadura"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Tipo: &f" + editType.toUpperCase()));
        player.sendMessage(CC.translate("&7  Ingresa el nuevo valor"));
        player.sendMessage(CC.translate("&7  Escribe &c'cancelar' &7para abortar"));
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    public static boolean isEditingArmor(Player player) {
        return playersEditing.containsKey(player.getUniqueId());
    }

    public static void processArmorEdit(Player player, String input) {
        ArmorEditState state = playersEditing.get(player.getUniqueId());
        if (state == null) return;

        if (!RegisterItem.items.containsKey(state.armorId)) {
            player.sendMessage(CC.translate("&c✗ Armadura no encontrada"));
            finishArmorEdit(player);
            return;
        }

        CustomArmor armor = RegisterItem.items.get(state.armorId);
        CustomArmorStorage storage = new CustomArmorStorage();

        switch (state.editType.toLowerCase()) {
            case "rename":
                armor.setDisplayName(CC.translate(input));
                storage.saveArmor(armor);
                player.sendMessage("");
                player.sendMessage(CC.translate("&a✓ Nombre actualizado"));
                player.sendMessage("");
                break;

            case "lore":
                String pasteUrl = input.trim();
                if (!pasteUrl.contains("pastebin.com")) {
                    player.sendMessage("");
                    player.sendMessage(CC.translate("&c✗ URL inválida. Debe ser de pastebin.com"));
                    player.sendMessage("");
                    startArmorEdit(player, state.armorId, "lore");
                    return;
                }

                List<String> lore = fetchPastebinLore(pasteUrl);
                if (lore == null || lore.isEmpty()) {
                    player.sendMessage("");
                    player.sendMessage(CC.translate("&c✗ No se pudo obtener el lore del pastebin"));
                    player.sendMessage("");
                    startArmorEdit(player, state.armorId, "lore");
                    return;
                }

                armor.setLore(lore);
                storage.saveArmor(armor);
                player.sendMessage("");
                player.sendMessage(CC.translate("&a✓ Lore actualizado (" + lore.size() + " líneas)"));
                player.sendMessage("");
                break;

            case "addline":
                List<String> currentLore = armor.getLore();
                if (currentLore == null) {
                    currentLore = new ArrayList<>();
                }
                currentLore.add(CC.translate(input));
                armor.setLore(currentLore);
                storage.saveArmor(armor);
                player.sendMessage("");
                player.sendMessage(CC.translate("&a✓ Línea agregada"));
                player.sendMessage("");
                break;

            case "setline":
                currentLore = armor.getLore();
                if (currentLore == null || state.lineNumber > currentLore.size() || state.lineNumber < 1) {
                    player.sendMessage(CC.translate("&c✗ Número de línea inválido"));
                    finishArmorEdit(player);
                    return;
                }
                currentLore.set(state.lineNumber - 1, CC.translate(input));
                armor.setLore(currentLore);
                storage.saveArmor(armor);
                player.sendMessage("");
                player.sendMessage(CC.translate("&a✓ Línea actualizada"));
                player.sendMessage("");
                break;
        }

        finishArmorEdit(player);
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(org.example.Main.instance, () -> {
            CustomArmorMenus.openEditArmorMenu(state.armorId).open(player);
        }, 1L);
    }

    private static List<String> fetchPastebinLore(String pasteUrl) {
        return PastebinReader.getFromPastebin(pasteUrl);
    }

    public static void cancelArmorEdit(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Edición cancelada"));
        player.sendMessage("");
        finishArmorEdit(player);
    }

    private static void finishArmorEdit(Player player) {
        playersEditing.remove(player.getUniqueId());
    }

    public static void giveCustomArmor(Player player, String armorId) {
        if (!RegisterItem.items.containsKey(armorId)) {
            player.sendMessage(CC.translate("&c✗ Armadura no encontrada"));
            return;
        }

        CustomArmor customArmor = RegisterItem.items.get(armorId);
        ItemStack itemStack = new ItemStack(customArmor.getMaterial());
        org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName(customArmor.getDisplayName());
        if (customArmor.getLore() != null) {
            meta.setLore(customArmor.getLore());
        }
        itemStack.setItemMeta(meta);

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), itemStack);
            player.sendMessage(CC.translate("&a✓ Armadura entregada (inventario lleno)"));
        } else {
            player.getInventory().addItem(itemStack);
            player.sendMessage(CC.translate("&a✓ Armadura entregada"));
        }
    }
}