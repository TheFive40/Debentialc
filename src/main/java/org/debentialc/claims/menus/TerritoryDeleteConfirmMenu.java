package org.debentialc.claims.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

import java.util.Arrays;

public class TerritoryDeleteConfirmMenu {

    public static SmartInventory createConfirmMenu(final Terrain terrain, final Player admin) {
        return SmartInventory.builder()
                .id("territory_delete_confirm_" + terrain.getId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 14)));

                        ItemStack warnItem = new ItemStack(Material.TNT);
                        ItemMeta warnMeta = warnItem.getItemMeta();
                        warnMeta.setDisplayName(CC.translate("&c&l¿Eliminar Territorio?"));
                        warnMeta.setLore(Arrays.asList(
                                CC.translate("&7Esta acción no puede deshacerse"),
                                CC.translate("&7El terreno &f" + terrain.getId() + " &7será"),
                                CC.translate("&7eliminado permanentemente"),
                                "",
                                terrain.isCommitted()
                                        ? CC.translate("&7Los bordes serán removidos del mundo")
                                        : CC.translate("&7El terreno no estaba generado")
                        ));
                        warnItem.setItemMeta(warnMeta);
                        contents.set(1, 4, ClickableItem.empty(warnItem));

                        ItemStack yesButton = new ItemStack(Material.EMERALD_BLOCK);
                        ItemMeta yesMeta = yesButton.getItemMeta();
                        yesMeta.setDisplayName(CC.translate("&a&lSÍ, ELIMINAR"));
                        yesButton.setItemMeta(yesMeta);
                        contents.set(2, 3, ClickableItem.of(yesButton, e -> {
                            boolean deleted = TerrainManager.getInstance().deleteTerrain(terrain.getId());
                            if (deleted) {
                                player.sendMessage(CC.translate("&7Terreno &f" + terrain.getId() + " &7eliminado. Los bordes fueron removidos del mundo."));
                            } else {
                                player.sendMessage(CC.translate("&7No se pudo eliminar el terreno."));
                            }
                            TerritoryAdminMenu.createAdminMenu(player).open(player);
                        }));

                        ItemStack noButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta noMeta = noButton.getItemMeta();
                        noMeta.setDisplayName(CC.translate("&c&lCANCELAR"));
                        noButton.setItemMeta(noMeta);
                        contents.set(2, 5, ClickableItem.of(noButton, e -> {
                            TerritoryAdminInfoMenu.createAdminInfoMenu(terrain, player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&c&lConfirmar Eliminación"))
                .build();
    }
}