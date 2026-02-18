package org.debentialc.claims.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerritoryAdminInfoMenu {

    public static SmartInventory createAdminInfoMenu(final Terrain terrain, final Player admin) {
        return SmartInventory.builder()
                .id("territory_admin_info_" + terrain.getId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 14)));

                        ItemStack infoItem = new ItemStack(Material.MAP);
                        ItemMeta infoMeta = infoItem.getItemMeta();
                        infoMeta.setDisplayName(CC.translate("&c&lAdmin: &f" + terrain.getId()));
                        List<String> infoLore = new ArrayList<String>();
                        infoLore.add(CC.translate("&7Tamaño: &f" + terrain.getChunks() + " chunk(s)"));
                        infoLore.add(CC.translate("&7Precio: &f$" + (int) terrain.getPrice()));
                        infoLore.add(CC.translate("&7Generado: " + (terrain.isCommitted() ? "&aSí" : "&cNo")));
                        if (terrain.hasOwner()) {
                            infoLore.add(CC.translate("&7Propietario: &f" + terrain.getOwnerName()));
                        } else {
                            infoLore.add(CC.translate("&7Estado: &aEn venta"));
                        }
                        infoMeta.setLore(infoLore);
                        infoItem.setItemMeta(infoMeta);
                        contents.set(1, 4, ClickableItem.empty(infoItem));

                        if (player.hasPermission(ClaimsPermissions.TERRAIN_COMMIT) && !terrain.isCommitted()) {
                            ItemStack commitButton = new ItemStack(Material.EMERALD_BLOCK);
                            ItemMeta commitMeta = commitButton.getItemMeta();
                            commitMeta.setDisplayName(CC.translate("&a&lGenerar Territorio"));
                            commitMeta.setLore(Arrays.asList(
                                    CC.translate("&7Coloca el territorio en tu posición"),
                                    "",
                                    CC.translate("&a[CLICK PARA GENERAR]")
                            ));
                            commitButton.setItemMeta(commitMeta);
                            contents.set(2, 2, ClickableItem.of(commitButton, e -> {
                                String collision = TerrainManager.getInstance().getCollisionId(terrain.getId(), player, terrain);
                                if (collision != null) {
                                    int buffer = TerrainManager.getInstance().getBuffer();
                                    player.sendMessage(CC.translate("&cNo se puede generar el terreno aquí."));
                                    player.sendMessage(CC.translate("&7Se choca con &f" + collision + " &7o está a menos de &f" + buffer + " &7bloques."));
                                    return;
                                }
                                boolean ok = TerrainManager.getInstance().commitTerrain(terrain.getId(), player);
                                if (ok) {
                                    player.sendMessage(CC.translate("&7Terreno &f" + terrain.getId() + " &7generado."));
                                    createAdminInfoMenu(terrain, player).open(player);
                                } else {
                                    player.sendMessage(CC.translate("&7No se pudo generar el terreno."));
                                }
                            }));
                        }

                        if (player.hasPermission(ClaimsPermissions.TERRAIN_DISSOLVE) && terrain.hasOwner()) {
                            ItemStack dissolveButton = new ItemStack(Material.LAVA_BUCKET);
                            ItemMeta dissolveMeta = dissolveButton.getItemMeta();
                            dissolveMeta.setDisplayName(CC.translate("&6&lDisolver"));
                            dissolveMeta.setLore(Arrays.asList(
                                    CC.translate("&7Quita al propietario y pone"),
                                    CC.translate("&7el terreno en venta de nuevo"),
                                    "",
                                    CC.translate("&6[CLICK PARA DISOLVER]")
                            ));
                            dissolveButton.setItemMeta(dissolveMeta);
                            contents.set(2, 4, ClickableItem.of(dissolveButton, e -> {
                                String prevOwner = terrain.getOwnerName();
                                boolean dissolved = TerrainManager.getInstance().dissolveTerrain(terrain.getId());
                                if (dissolved) {
                                    player.sendMessage(CC.translate("&7Terreno &f" + terrain.getId() + " &7disuelto. El propietario &f" + prevOwner + " &7fue removido."));
                                    createAdminInfoMenu(terrain, player).open(player);
                                } else {
                                    player.sendMessage(CC.translate("&7No se pudo disolver el terreno."));
                                }
                            }));
                        }

                        if (player.hasPermission(ClaimsPermissions.TERRAIN_DELETE)) {
                            ItemStack deleteButton = new ItemStack(Material.TNT);
                            ItemMeta deleteMeta = deleteButton.getItemMeta();
                            deleteMeta.setDisplayName(CC.translate("&c&lEliminar Territorio"));
                            deleteMeta.setLore(Arrays.asList(
                                    CC.translate("&7Elimina el territorio permanentemente"),
                                    CC.translate("&cEsta acción no se puede deshacer"),
                                    "",
                                    CC.translate("&c[CLICK PARA ELIMINAR]")
                            ));
                            deleteButton.setItemMeta(deleteMeta);
                            contents.set(2, 6, ClickableItem.of(deleteButton, e -> {
                                TerritoryDeleteConfirmMenu.createConfirmMenu(terrain, player).open(player);
                            }));
                        }

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 4, ClickableItem.of(backButton, e -> {
                            TerritoryAdminMenu.createAdminMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&c&lAdmin: " + terrain.getId()))
                .build();
    }
}