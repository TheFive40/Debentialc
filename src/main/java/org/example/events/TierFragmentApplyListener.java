package org.example.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.example.tools.CC;
import org.example.tools.fragments.CustomizedArmor;
import org.example.tools.fragments.FragmentManager;
import org.example.tools.fragments.TierFragment;

/**
 * Listener para manejar la aplicación de fragmentos de tier a armaduras
 * VERSIÓN CORREGIDA: Validación correcta de porcentajes y operaciones permitidas
 */
public class TierFragmentApplyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTierFragmentUse(PlayerInteractEvent event) {
        // Solo clic derecho
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();

        // Verificar si es un fragmento de tier
        if (!TierFragment.isTierFragment(itemInHand)) {
            return;
        }

        // Cancelar el evento
        event.setCancelled(true);

        // Buscar pieza de armadura equipada
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack targetArmor = null;
        int targetSlot = -1;

        // Buscar primera pieza equipada (helmet -> chest -> legs -> boots)
        for (int i = 3; i >= 0; i--) {
            if (armorContents[i] != null && armorContents[i].getType() != Material.AIR) {
                targetArmor = armorContents[i];
                targetSlot = i;
                break;
            }
        }

        if (targetArmor == null) {
            player.sendMessage(CC.translate("&c✗ No tienes ninguna armadura equipada"));
            player.sendMessage(CC.translate("&7Equipa al menos una pieza de armadura"));
            return;
        }

        // Verificar que sea armadura personalizada
        if (!CustomizedArmor.isCustomized(targetArmor)) {
            player.sendMessage(CC.translate("&c✗ Esta armadura no está personalizada"));
            player.sendMessage(CC.translate("&7Solo se pueden upgradear armaduras con fragmentos aplicados"));
            return;
        }

        // Obtener datos
        String targetTier = TierFragment.getTargetTier(itemInHand);
        String currentTier = CustomizedArmor.getTier(targetArmor);

        // Validar upgrade secuencial
        if (!TierFragment.canUpgrade(currentTier, targetTier)) {
            int currentNum = TierFragment.getTierNumber(currentTier);
            int targetNum = TierFragment.getTierNumber(targetTier);

            player.sendMessage("");
            player.sendMessage(CC.translate("&c✗ No se puede aplicar este fragmento"));
            player.sendMessage(CC.translate("&7Tier actual: &f" + currentTier));
            player.sendMessage(CC.translate("&7Tier del fragmento: &f" + targetTier));
            player.sendMessage("");

            if (targetNum <= currentNum) {
                player.sendMessage(CC.translate("&7La armadura ya tiene un tier igual o superior"));
            } else {
                player.sendMessage(CC.translate("&7Los upgrades deben ser secuenciales"));
                player.sendMessage(CC.translate("&7Necesitas: &fTIER_" + (currentNum + 1)));
            }
            player.sendMessage("");
            return;
        }

        // Validar límites del nuevo tier
        CustomizedArmor customArmor = CustomizedArmor.fromItemStack(targetArmor);

        if (customArmor == null) {
            player.sendMessage(CC.translate("&c✗ Error al cargar la armadura"));
            return;
        }

        // VALIDACIÓN CORREGIDA: Usar exceedsLimit que maneja correctamente los multiplicadores
        boolean exceedsLimits = false;
        StringBuilder errorMsg = new StringBuilder();

        for (java.util.Map.Entry<String, Integer> entry : customArmor.getAttributes().entrySet()) {
            String attr = entry.getKey();
            int currentValue = entry.getValue();
            String operation = customArmor.getOperations().getOrDefault(attr, "+");

            // Usar el método corregido
            if (FragmentManager.getInstance().getTierConfig().exceedsLimit(targetTier, attr, currentValue, operation)) {
                exceedsLimits = true;
                if (errorMsg.length() == 0) {
                    errorMsg.append(CC.translate("&c✗ Atributos exceden límites del nuevo tier:\n&7"));
                } else {
                    errorMsg.append(", ");
                }

                int newLimit = FragmentManager.getInstance().getTierConfig().getLimit(targetTier, attr);

                String displayValue;
                if (operation.equals("*")) {
                    double percentage = (currentValue / 100.0 - 1.0) * 100.0;
                    displayValue = String.format("%+.0f%%", percentage);
                } else {
                    displayValue = (currentValue >= 0 ? "+" : "") + currentValue;
                }

                errorMsg.append(attr).append(": ").append(displayValue);
                errorMsg.append(" (límite: ").append(newLimit).append(")");
            }
        }

        if (exceedsLimits) {
            player.sendMessage("");
            player.sendMessage(errorMsg.toString());
            player.sendMessage("");
            player.sendMessage(CC.translate("&7Usa fragmentos negativos para reducir valores"));
            player.sendMessage("");
            return;
        }

        // VALIDACIÓN NUEVA: Verificar operaciones permitidas en el nuevo tier
        boolean hasInvalidOperations = false;
        StringBuilder opErrorMsg = new StringBuilder();

        for (java.util.Map.Entry<String, String> entry : customArmor.getOperations().entrySet()) {
            String attr = entry.getKey();
            String operation = entry.getValue();

            if (!FragmentManager.getInstance().getTierConfig().isOperationAllowed(targetTier, operation)) {
                hasInvalidOperations = true;
                if (opErrorMsg.length() == 0) {
                    opErrorMsg.append(CC.translate("&c✗ Operaciones NO permitidas en " + targetTier + ":\n&7"));
                } else {
                    opErrorMsg.append(", ");
                }

                opErrorMsg.append(attr).append(" (").append(operation).append(")");
            }
        }

        if (hasInvalidOperations) {
            player.sendMessage("");
            player.sendMessage(opErrorMsg.toString());
            player.sendMessage("");
            player.sendMessage(CC.translate("&7Operaciones permitidas: &f" +
                    String.join(", ", FragmentManager.getInstance().getTierConfig().getAllowedOperations(targetTier))));
            player.sendMessage("");
            return;
        }

        // APLICAR UPGRADE
        String oldTier = customArmor.getTier();
        customArmor.setTier(targetTier);

        // Actualizar ItemStack con el nuevo tier
        customArmor.applyToItemStack(targetArmor);

        // Guardar en almacenamiento
        FragmentManager.getInstance().getArmorStorage().saveArmor(customArmor);

        // Actualizar armadura en el slot
        armorContents[targetSlot] = targetArmor;
        player.getInventory().setArmorContents(armorContents);
        player.updateInventory();

        // Consumir el fragmento
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            player.setItemInHand(new ItemStack(Material.AIR));
        }

        // Feedback visual y sonoro
        player.sendMessage("");
        player.sendMessage(CC.translate("&a✓ ¡Armadura upgradeada exitosamente!"));
        player.sendMessage(CC.translate("&7Tier anterior: &f" + oldTier));
        player.sendMessage(CC.translate("&7Tier nuevo: &a" + targetTier));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Todos los stats y operaciones se mantuvieron"));
        player.sendMessage("");

        // Efectos
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        player.getWorld().strikeLightningEffect(player.getLocation());
    }
}