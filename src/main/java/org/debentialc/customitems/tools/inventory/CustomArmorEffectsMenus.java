package org.debentialc.customitems.tools.inventory;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.customitems.commands.RegisterItem;
import org.debentialc.service.CC;
import org.debentialc.customitems.tools.ci.CustomArmor;

import java.util.Arrays;

public class CustomArmorEffectsMenus {

    public static SmartInventory createEffectSelectionMenu(String armorId) {
        CustomArmor armor = RegisterItem.items.get(armorId);
        if (armor == null) return null;

        return SmartInventory.builder()
                .id("ca_effect_select_" + armorId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        ItemStack titleItem = new ItemStack(Material.PAPER);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&6&lSelecciona Efecto"));
                        titleMeta.setLore(Arrays.asList(
                                CC.translate("&7Armadura: &f" + armorId),
                                CC.translate("&7Elige un efecto especial")
                        ));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        String[] effects = {"HEALTHREGEN", "KIREGEN", "STAMINAREGEN"};
                        String[] effectNames = {"Regeneración de Vida", "Regeneración de Ki", "Regeneración de Stamina"};
                        Material[] effectMaterials = {
                                Material.REDSTONE,
                                Material.LAPIS_BLOCK,
                                Material.GOLD_BLOCK
                        };

                        int row = 1;
                        int col = 2;

                        for (int i = 0; i < effects.length; i++) {
                            final String effect = effects[i];
                            ItemStack effectButton = new ItemStack(effectMaterials[i]);
                            ItemMeta effectMeta = effectButton.getItemMeta();
                            effectMeta.setDisplayName(CC.translate("&e&l" + effectNames[i]));
                            effectMeta.setLore(Arrays.asList(
                                    CC.translate("&7Efecto: &f" + effect),
                                    CC.translate("&a[CLICK PARA SELECCIONAR]")
                            ));
                            effectButton.setItemMeta(effectMeta);

                            contents.set(row, col, ClickableItem.of(effectButton, e -> {
                                player.closeInventory();
                                EffectInputManager.startEffectInput(player, armorId, effect, "armor");
                            }));

                            col += 2;
                            if (col > 7) {
                                col = 2;
                                row++;
                            }
                        }

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 0, ClickableItem.of(backButton, e -> {
                            CustomArmorMenus.openEditArmorMenu(armorId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&6&lEfectos"))
                .build();
    }

    private static ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(CC.translate("&8"));
        glass.setItemMeta(glassMeta);
        return glass;
    }
}