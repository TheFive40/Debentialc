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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerritoryAdminFilterMenu {

    public static SmartInventory createFilterMenu(final String filter, final Player admin) {
        String title;
        if (filter.equals("uncommitted")) {
            title = "&8Sin Generar";
        } else if (filter.equals("forsale")) {
            title = "&aEn Venta";
        } else {
            title = "&cOcupados";
        }
        final String menuTitle = title;

        return SmartInventory.builder()
                .id("territory_admin_filter_" + filter)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 14)));

                        List<Terrain> filtered = new ArrayList<Terrain>();
                        for (Terrain t : TerrainManager.getInstance().getAll().values()) {
                            if (filter.equals("uncommitted") && !t.isCommitted()) {
                                filtered.add(t);
                            } else if (filter.equals("forsale") && t.isCommitted() && !t.hasOwner()) {
                                filtered.add(t);
                            } else if (filter.equals("owned") && t.isCommitted() && t.hasOwner()) {
                                filtered.add(t);
                            }
                        }

                        if (filtered.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.ANVIL);
                            ItemMeta emptyMeta = emptyItem.getItemMeta();
                            emptyMeta.setDisplayName(CC.translate("&cNo hay territorios en esta categoría"));
                            emptyItem.setItemMeta(emptyMeta);
                            contents.set(2, 4, ClickableItem.empty(emptyItem));
                        }

                        int row = 1;
                        int col = 1;

                        for (Terrain terrain : filtered) {
                            Material mat;
                            if (!terrain.isCommitted()) {
                                mat = Material.DIRT;
                            } else if (terrain.hasOwner()) {
                                mat = Material.GRASS;
                            } else {
                                mat = Material.EMERALD_BLOCK;
                            }

                            ItemStack item = new ItemStack(mat);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(CC.translate("&f&l" + terrain.getId()));
                            List<String> lore = new ArrayList<String>();
                            lore.add(CC.translate("&7Tamaño: &f" + terrain.getChunks() + " chunk(s)"));
                            lore.add(CC.translate("&7Precio: &f$" + (int) terrain.getPrice()));
                            if (!terrain.isCommitted()) {
                                lore.add(CC.translate("&7Estado: &8Sin generar"));
                            } else if (terrain.hasOwner()) {
                                lore.add(CC.translate("&7Propietario: &f" + terrain.getOwnerName()));
                            } else {
                                lore.add(CC.translate("&7Estado: &aEn venta"));
                            }
                            lore.add("");
                            lore.add(CC.translate("&a[CLICK] &7Ver opciones admin"));
                            meta.setLore(lore);
                            item.setItemMeta(meta);

                            final Terrain t = terrain;
                            contents.set(row, col, ClickableItem.of(item, e -> {
                                TerritoryAdminInfoMenu.createAdminInfoMenu(t, player).open(player);
                            }));

                            col++;
                            if (col >= 8) {
                                col = 1;
                                row++;
                                if (row >= 4) break;
                            }
                        }

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 4, ClickableItem.of(backButton, e -> {
                            TerritoryAdminMenu.createAdminMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate(menuTitle))
                .build();
    }
}