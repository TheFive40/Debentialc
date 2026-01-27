package org.example.tools.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.ci.CustomArmor;
import org.example.commands.items.RegisterItem;

import java.util.HashMap;
import java.util.UUID;

public class ArmorCreationManager {
    private static final HashMap<UUID, Boolean> playersCreatingArmor = new HashMap<>();

    public static void startArmorCreation(Player player) {
        playersCreatingArmor.put(player.getUniqueId(), true);
        player.sendMessage(CC.translate("&8"));
        player.sendMessage(CC.translate("&b&l» CREAR ARMADURA"));
        player.sendMessage(CC.translate("&8 ━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&7Escribe el ID de la armadura"));
        player.sendMessage(CC.translate("&7(Escribe &c'cancelar'&7 para abortar)"));
        player.sendMessage(CC.translate("&8"));
    }

    public static boolean isCreatingArmor(Player player) {
        return playersCreatingArmor.getOrDefault(player.getUniqueId(), false);
    }

    public static void processArmorCreation(Player player, String armorName) {
        ItemStack armor = player.getItemInHand();

        if (armor == null || armor.getTypeId() == 0) {
            player.sendMessage(CC.translate("&c✗ Debes sostener una armadura en la mano"));
            startArmorCreation(player);
            return;
        }

        if (armor.getItemMeta() == null) {
            player.sendMessage(CC.translate("&c✗ Esta armadura no tiene metadatos"));
            startArmorCreation(player);
            return;
        }

        if (armorName.isEmpty()) {
            player.sendMessage(CC.translate("&c✗ El ID no puede estar vacío"));
            startArmorCreation(player);
            return;
        }

        String armorId = armorName.toLowerCase().replace(" ", "_");
        if (RegisterItem.items.containsKey(armorId)) {
            player.sendMessage(CC.translate("&c✗ Ya existe una armadura con ese ID"));
            startArmorCreation(player);
            return;
        }

        CustomArmor customArmor = new CustomArmor()
                .setId(armorId)
                .setMaterial(armor.getTypeId())
                .setLore(armor.getItemMeta().getLore())
                .setDisplayName(armor.getItemMeta().getDisplayName());

        RegisterItem.items.put(armorId, customArmor);

        // Guardar en BD
        org.example.tools.storage.CustomArmorStorage storage =
                new org.example.tools.storage.CustomArmorStorage();
        storage.saveArmor(customArmor);

        player.sendMessage(CC.translate("&a✓ Armadura creada: &f" + armorId));
        finishArmorCreation(player);
    }

    public static void cancelArmorCreation(Player player) {
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        finishArmorCreation(player);
    }

    private static void finishArmorCreation(Player player) {
        playersCreatingArmor.put(player.getUniqueId(), false);
    }
}