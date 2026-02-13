package org.debentialc.customitems.tools.inventory;

import org.bukkit.entity.Player;
import org.debentialc.service.CC;
import org.debentialc.customitems.tools.ci.CustomItem;
import org.debentialc.customitems.tools.ci.CustomArmor;
import org.debentialc.customitems.commands.CustomItemCommand;
import org.debentialc.customitems.commands.RegisterItem;
import org.debentialc.customitems.tools.storage.CustomItemStorage;
import org.debentialc.customitems.tools.storage.CustomArmorStorage;

import java.util.HashMap;
import java.util.UUID;

/**
 * Gestiona el cambio de material ID (número de Minecraft) para items y armaduras
 * Soporta: 345 (solo ID) o 345/5 (ID con data value)
 */
public class ItemIdChangeManager {

    public static class MaterialIdChangeState {
        public String itemId;
        public String type; // "item" o "armor"
        public int oldMaterialId;

        public MaterialIdChangeState(String itemId, String type, int oldMaterialId) {
            this.itemId = itemId;
            this.type = type;
            this.oldMaterialId = oldMaterialId;
        }
    }

    private static final HashMap<UUID, MaterialIdChangeState> playersChangingMaterialId = new HashMap<>();

    /**
     * Inicia el proceso de cambio de material ID
     */
    public static void startMaterialIdChange(Player player, String itemId, String type) {
        int oldMaterialId = 0;

        if ("item".equals(type) && CustomItemCommand.items.containsKey(itemId)) {
            oldMaterialId = CustomItemCommand.items.get(itemId).getMaterial();
        } else if ("armor".equals(type) && RegisterItem.items.containsKey(itemId)) {
            oldMaterialId = RegisterItem.items.get(itemId).getMaterial();
        }

        playersChangingMaterialId.put(player.getUniqueId(), new MaterialIdChangeState(itemId, type, oldMaterialId));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(CC.translate("&6&l  Cambiar Material ID"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Material ID actual: &f" + oldMaterialId));
        player.sendMessage(CC.translate("&7  Ingresa el nuevo ID del material"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Formato aceptado:"));
        player.sendMessage(CC.translate("&f    345        &7(solo el ID)"));
        player.sendMessage(CC.translate("&f    345/5      &7(ID con data value)"));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7  Escribe &c'cancelar' &7para abortar"));
        player.sendMessage(CC.translate("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    public static boolean isChangingMaterialId(Player player) {
        return playersChangingMaterialId.containsKey(player.getUniqueId());
    }

    /**
     * Procesa el cambio de material ID
     */
    public static void processMaterialIdChange(Player player, String input) {
        MaterialIdChangeState state = playersChangingMaterialId.get(player.getUniqueId());
        if (state == null) return;

        String materialInput = input.trim();

        // Validar que no esté vacío
        if (materialInput.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ El Material ID no puede estar vacío"));
            player.sendMessage("");
            startMaterialIdChange(player, state.itemId, state.type);
            return;
        }

        // Validar formato: solo números y opcionalmente una barra con números
        if (!materialInput.matches("\\d+(/\\d+)?")) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Formato inválido"));
            player.sendMessage(CC.translate("&7  Formato correcto: 345 o 345/5"));
            player.sendMessage("");
            startMaterialIdChange(player, state.itemId, state.type);
            return;
        }

        // Extraer el ID del material
        int newMaterialId;
        try {
            // Si tiene barra (345/5), extraer solo el ID (345)
            if (materialInput.contains("/")) {
                String[] parts = materialInput.split("/");
                newMaterialId = Integer.parseInt(parts[0]);
            } else {
                newMaterialId = Integer.parseInt(materialInput);
            }

            // Validar que sea un número positivo
            if (newMaterialId <= 0) {
                player.sendMessage("");
                player.sendMessage(CC.translate("&c✗ El Material ID debe ser mayor a 0"));
                player.sendMessage("");
                startMaterialIdChange(player, state.itemId, state.type);
                return;
            }

            // Validar rango válido de IDs de Minecraft (0-255 en versiones antiguas)
            if (newMaterialId > 255) {
                player.sendMessage("");
                player.sendMessage(CC.translate("&c⚠ Advertencia: Este ID podría no ser válido"));
                player.sendMessage(CC.translate("&7  Los IDs válidos generalmente están entre 1-255"));
                player.sendMessage(CC.translate("&7  Continuando de todas formas..."));
                player.sendMessage("");
            }

        } catch (NumberFormatException e) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ El Material ID debe ser un número válido"));
            player.sendMessage("");
            startMaterialIdChange(player, state.itemId, state.type);
            return;
        }

        // Validar que no sea igual al anterior
        if (newMaterialId == state.oldMaterialId) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ El nuevo Material ID es igual al anterior"));
            player.sendMessage("");
            startMaterialIdChange(player, state.itemId, state.type);
            return;
        }

        // Proceder con el cambio
        if ("item".equals(state.type)) {
            changeMaterialIdItem(player, state.itemId, newMaterialId, materialInput);
        } else if ("armor".equals(state.type)) {
            changeMaterialIdArmor(player, state.itemId, newMaterialId, materialInput);
        }

        finishMaterialIdChange(player);
    }

    /**
     * Cambia el Material ID de un item custom
     */
    private static void changeMaterialIdItem(Player player, String itemId, int newMaterialId, String originalInput) {
        if (!CustomItemCommand.items.containsKey(itemId)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Item no encontrado"));
            player.sendMessage("");
            return;
        }

        CustomItem item = CustomItemCommand.items.get(itemId);
        int oldMaterialId = item.getMaterial();

        // Cambiar el material
        item.setMaterial(newMaterialId);

        // Guardar en almacenamiento
        CustomItemStorage storage = new CustomItemStorage();
        storage.saveItem(item);

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Material ID cambiado exitosamente"));
        player.sendMessage(CC.translate("&7Anterior: &f" + oldMaterialId));
        player.sendMessage(CC.translate("&7Nuevo: &f" + originalInput));
        player.sendMessage("");

        // Reabrir menú
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(
                org.debentialc.Main.instance,
                () -> {
                    CustomItemMenus.openEditItemMenu(itemId).open(player);
                },
                1L
        );
    }

    /**
     * Cambia el Material ID de una armadura custom
     */
    private static void changeMaterialIdArmor(Player player, String armorId, int newMaterialId, String originalInput) {
        if (!RegisterItem.items.containsKey(armorId)) {
            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ Armadura no encontrada"));
            player.sendMessage("");
            return;
        }

        CustomArmor armor = RegisterItem.items.get(armorId);
        int oldMaterialId = armor.getMaterial();

        // Cambiar el material
        armor.setMaterial(newMaterialId);

        // Guardar en almacenamiento
        CustomArmorStorage storage = new CustomArmorStorage();
        storage.saveArmor(armor);

        // Actualizar en memoria
        RegisterItem.items.put(armorId, armor);

        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ Material ID cambiado exitosamente"));
        player.sendMessage(CC.translate("&7Anterior: &f" + oldMaterialId));
        player.sendMessage(CC.translate("&7Nuevo: &f" + originalInput));
        player.sendMessage("");

        // Reabrir menú
        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(
                org.debentialc.Main.instance,
                () -> {
                    CustomArmorMenus.openEditArmorMenu(armorId).open(player);
                },
                1L
        );
    }

    public static void cancelMaterialIdChange(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&c✗ Cancelado"));
        player.sendMessage("");
        finishMaterialIdChange(player);
    }

    private static void finishMaterialIdChange(Player player) {
        playersChangingMaterialId.remove(player.getUniqueId());
    }
}