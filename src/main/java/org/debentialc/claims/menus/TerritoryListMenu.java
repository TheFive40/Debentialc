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
import java.util.Map;

public class TerritoryListMenu {

    public static SmartInventory createListMenu(final int page) {
        return SmartInventory.builder()
                .id("territory_list_" + page)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        List<Terrain> terrains = new ArrayList<Terrain>(TerrainManager.getInstance().getAll().values());
                        int pageSize = 21;
                        int totalPages = Math.max(1, (int) Math.ceil((double) terrains.size() / pageSize));

                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 7)));

                        if (terrains.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.ANVIL);
                            ItemMeta emptyMeta = emptyItem.getItemMeta();
                            emptyMeta.setDisplayName(CC.translate("&cNo hay territorios registrados"));
                            emptyMeta.setLore(Arrays.asList(
                                    CC.translate("&7Un administrador debe crear territorios")
                            ));
                            emptyItem.setItemMeta(emptyMeta);
                            contents.set(2, 4, ClickableItem.empty(emptyItem));
                        } else {
                            int start = (page - 1) * pageSize;
                            int end = Math.min(start + pageSize, terrains.size());

                            int row = 1;
                            int col = 1;

                            for (int i = start; i < end; i++) {
                                Terrain terrain = terrains.get(i);

                                Material mat;
                                if (!terrain.isCommitted()) {
                                    mat = Material.DIRT;
                                } else if (terrain.hasOwner()) {
                                    mat = Material.GRASS;
                                } else {
                                    mat = Material.EMERALD_BLOCK;
                                }

                                ItemStack terrainItem = new ItemStack(mat);
                                ItemMeta terrainMeta = terrainItem.getItemMeta();
                                terrainMeta.setDisplayName(CC.translate("&f&l" + terrain.getId()));

                                List<String> lore = new ArrayList<String>();
                                lore.add(CC.translate("&7Tamaño: &f" + terrain.getChunks() + " chunk(s) &8(" + terrain.getSizeInBlocks() + "x" + terrain.getSizeInBlocks() + ")"));
                                lore.add(CC.translate("&7Precio: &f$" + (int) terrain.getPrice()));
                                if (!terrain.isCommitted()) {
                                    lore.add(CC.translate("&7Estado: &8Sin generar"));
                                } else if (terrain.hasOwner()) {
                                    lore.add(CC.translate("&7Propietario: &f" + terrain.getOwnerName()));
                                    if (!terrain.getMembers().isEmpty()) {
                                        lore.add(CC.translate("&7Miembros: &f" + terrain.getMembers().size()));
                                    }
                                } else {
                                    lore.add(CC.translate("&7Estado: &aEn venta"));
                                }
                                lore.add("");
                                lore.add(CC.translate("&a[CLICK PARA VER INFO]"));

                                terrainMeta.setLore(lore);
                                terrainItem.setItemMeta(terrainMeta);

                                final Terrain t = terrain;
                                contents.set(row, col, ClickableItem.of(terrainItem, e -> {
                                    TerritoryInfoMenu.createInfoMenu(t, player).open(player);
                                }));

                                col++;
                                if (col >= 8) {
                                    col = 1;
                                    row++;
                                    if (row >= 4) break;
                                }
                            }
                        }

                        if (page > 1) {
                            ItemStack prevButton = new ItemStack(Material.ARROW);
                            ItemMeta prevMeta = prevButton.getItemMeta();
                            prevMeta.setDisplayName(CC.translate("&b← Anterior"));
                            prevButton.setItemMeta(prevMeta);
                            contents.set(4, 2, ClickableItem.of(prevButton, e -> {
                                createListMenu(page - 1).open(player);
                            }));
                        }

                        ItemStack pageItem = new ItemStack(Material.BOOK);
                        ItemMeta pageMeta = pageItem.getItemMeta();
                        pageMeta.setDisplayName(CC.translate("&f&lPágina " + page + "/" + Math.max(1, (int) Math.ceil((double) TerrainManager.getInstance().getAll().size() / pageSize))));
                        pageItem.setItemMeta(pageMeta);
                        contents.set(4, 4, ClickableItem.empty(pageItem));

                        if (page < Math.max(1, (int) Math.ceil((double) TerrainManager.getInstance().getAll().size() / pageSize))) {
                            ItemStack nextButton = new ItemStack(Material.ARROW);
                            ItemMeta nextMeta = nextButton.getItemMeta();
                            nextMeta.setDisplayName(CC.translate("&bSiguiente →"));
                            nextButton.setItemMeta(nextMeta);
                            contents.set(4, 6, ClickableItem.of(nextButton, e -> {
                                createListMenu(page + 1).open(player);
                            }));
                        }

                        ItemStack backButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&c← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 8, ClickableItem.of(backButton, e -> {
                            TerritoryMenu.createMainMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&6&lTerritorios #" + page))
                .build();
    }
}