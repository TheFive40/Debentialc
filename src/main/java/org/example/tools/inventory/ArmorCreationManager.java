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

    /**
     * Inicia el proceso de creación de armadura para un jugador
     */
    public static void startArmorCreation(Player player) {
        playersCreatingArmor.put(player.getUniqueId(), true);
        player.sendMessage(CC.translate("&b&l┌─────────────────────────────┐"));
        player.sendMessage(CC.translate("&b&l│  &a&lCREACIÓN DE ARMADURA CUSTOM  &b&l│"));
        player.sendMessage(CC.translate("&b&l├─────────────────────────────┤"));
        player.sendMessage(CC.translate("&b&l│ &7Escribe el nombre de la     &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &7armadura que sostienes      &b&l│"));
        player.sendMessage(CC.translate("&b&l│ &c(Escribe 'cancelar' para abortar)&b&l│"));
        player.sendMessage(CC.translate("&b&l└─────────────────────────────┘"));
    }

    /**
     * Verifica si un jugador está en proceso de crear una armadura
     */
    public static boolean isCreatingArmor(Player player) {
        return playersCreatingArmor.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Procesa el nombre ingresado por el jugador
     */
    public static void processArmorCreation(Player player, String armorName) {
        ItemStack armor = player.getItemInHand();

        // Validaciones
        if (armor == null || armor.getTypeId() == 0) {
            player.sendMessage(CC.translate("&c✗ Debes sostener una armadura en la mano"));
            startArmorCreation(player);
            return;
        }

        if (armor.getItemMeta() == null) {
            player.sendMessage(CC.translate("&c✗ Esta armadura no tiene metadatos (nombre/lore)"));
            startArmorCreation(player);
            return;
        }

        if (armorName.isEmpty()) {
            player.sendMessage(CC.translate("&c✗ El nombre de la armadura no puede estar vacío"));
            startArmorCreation(player);
            return;
        }

        // Verificar que el ID no exista
        String armorId = armorName.toLowerCase().replace(" ", "_");
        if (RegisterItem.items.containsKey(armorId)) {
            player.sendMessage(CC.translate("&c✗ Ya existe una armadura con el ID: &f" + armorId));
            startArmorCreation(player);
            return;
        }

        // Crear la armadura custom
        CustomArmor customArmor = new CustomArmor()
                .setId(armorId)
                .setMaterial(armor.getTypeId())
                .setLore(armor.getItemMeta().getLore())
                .setDisplayName(armor.getItemMeta().getDisplayName());

        RegisterItem.items.put(armorId, customArmor);

        // Mensaje de confirmación
        player.sendMessage(CC.translate("&a✓ Armadura creada correctamente"));
        player.sendMessage(CC.translate("&7ID: &f" + armorId));
        player.sendMessage(CC.translate("&7Nombre: &f" + customArmor.getDisplayName()));

        finishArmorCreation(player);
    }

    /**
     * Cancela el proceso de creación
     */
    public static void cancelArmorCreation(Player player) {
        player.sendMessage(CC.translate("&c✗ Creación de armadura cancelada"));
        finishArmorCreation(player);
    }

    /**
     * Finaliza el proceso de creación
     */
    private static void finishArmorCreation(Player player) {
        playersCreatingArmor.put(player.getUniqueId(), false);
    }
}