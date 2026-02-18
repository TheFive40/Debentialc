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

public class TerritoryMyMenu {

    public static SmartInventory createMyMenu(final Player owner) {
        return SmartInventory.builder()
                .id("territory_my_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 3)));

                        List<Terrain> myTerrains = new ArrayList<Terrain>();
                        List<Terrain> memberTerrains = new ArrayList<Terrain>();

                        for (Terrain t : TerrainManager.getInstance().getAll().values()) {
                            if (t.isOwner(player.getUniqueId())) {
                                myTerrains.add(t);
                            } else if (t.isMember(player.getUniqueId())) {
                                memberTerrains.add(t);
                            }
                        }

                        ItemStack titleItem = new ItemStack(Material.CHEST);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&b&lMis Territorios"));
                        List<String> titleLore = new ArrayList<String>();
                        titleLore.add(CC.translate("&7Propietario de: &f" + myTerrains.size() + " terreno(s)"));
                        titleLore.add(CC.translate("&7Miembro de: &f" + memberTerrains.size() + " terreno(s)"));
                        titleMeta.setLore(titleLore);
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        if (myTerrains.isEmpty() && memberTerrains.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.ANVIL);
                            ItemMeta emptyMeta = emptyItem.getItemMeta();
                            emptyMeta.setDisplayName(CC.translate("&cNo tienes territorios"));
                            emptyMeta.setLore(Arrays.asList(
                                    CC.translate("&7Compra un terreno haciendo clic en"),
                                    CC.translate("&7el cartel o desde el menú de lista")
                            ));
                            emptyItem.setItemMeta(emptyMeta);
                            contents.set(2, 4, ClickableItem.empty(emptyItem));
                        }

                        int row = 1;
                        int col = 1;

                        for (Terrain terrain : myTerrains) {
                            ItemStack terrainItem = new ItemStack(Material.GRASS);
                            ItemMeta terrainMeta = terrainItem.getItemMeta();
                            terrainMeta.setDisplayName(CC.translate("&f&l" + terrain.getId()));
                            List<String> lore = new ArrayList<String>();
                            lore.add(CC.translate("&7Tamaño: &f" + terrain.getChunks() + " chunk(s)"));
                            lore.add(CC.translate("&7Miembros: &f" + terrain.getMembers().size()));
                            if (terrain.getOrigin() != null) {
                                lore.add(CC.translate("&7Pos: &f" + terrain.getOrigin().getBlockX() + ", " + terrain.getOrigin().getBlockZ()));
                            }
                            lore.add("");
                            lore.add(CC.translate("&6&l[PROPIETARIO]"));
                            lore.add(CC.translate("&a[CLICK PARA VER]"));
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
                                if (row >= 3) break;
                            }
                        }

                        if (!memberTerrains.isEmpty() && row < 3) {
                            row = 3;
                            col = 1;
                            for (Terrain terrain : memberTerrains) {
                                ItemStack memberItem = new ItemStack(Material.DIRT);
                                ItemMeta memberMeta = memberItem.getItemMeta();
                                memberMeta.setDisplayName(CC.translate("&7&l" + terrain.getId()));
                                List<String> lore = new ArrayList<String>();
                                lore.add(CC.translate("&7Propietario: &f" + terrain.getOwnerName()));
                                List<Terrain.MemberRole> roles = terrain.getMembers().get(player.getUniqueId());
                                if (roles != null) {
                                    StringBuilder rolesStr = new StringBuilder();
                                    for (Terrain.MemberRole role : roles) {
                                        if (rolesStr.length() > 0) rolesStr.append(", ");
                                        rolesStr.append(role.name().toLowerCase());
                                    }
                                    lore.add(CC.translate("&7Tu rol: &f" + rolesStr.toString()));
                                }
                                lore.add("");
                                lore.add(CC.translate("&8&l[MIEMBRO]"));
                                lore.add(CC.translate("&a[CLICK PARA VER]"));
                                memberMeta.setLore(lore);
                                memberItem.setItemMeta(memberMeta);

                                final Terrain t = terrain;
                                contents.set(row, col, ClickableItem.of(memberItem, e -> {
                                    TerritoryInfoMenu.createInfoMenu(t, player).open(player);
                                }));

                                col++;
                                if (col >= 8) break;
                            }
                        }

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 4, ClickableItem.of(backButton, e -> {
                            TerritoryMenu.createMainMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&b&lMis Territorios"))
                .build();
    }
}