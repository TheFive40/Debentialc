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
import java.util.Map;

public class TerritoryMenu {

    public static SmartInventory createMainMenu(Player player) {
        return SmartInventory.builder()
                .id("territory_main_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane((short) 7)));

                        Map<String, Terrain> all = TerrainManager.getInstance().getAll();
                        int total = all.size();
                        int forSale = 0;
                        int owned = 0;
                        int uncommitted = 0;
                        for (Terrain t : all.values()) {
                            if (!t.isCommitted()) {
                                uncommitted++;
                            } else if (t.hasOwner()) {
                                owned++;
                            } else {
                                forSale++;
                            }
                        }

                        ItemStack infoItem = new ItemStack(Material.MAP);
                        ItemMeta infoMeta = infoItem.getItemMeta();
                        infoMeta.setDisplayName(CC.translate("&6&lSistema de Territorios"));
                        List<String> infoLore = new ArrayList<String>();
                        infoLore.add(CC.translate("&7Territorios totales: &f" + total));
                        infoLore.add(CC.translate("&7En venta: &a" + forSale));
                        infoLore.add(CC.translate("&7Ocupados: &c" + owned));
                        infoLore.add(CC.translate("&7Sin generar: &8" + uncommitted));
                        infoMeta.setLore(infoLore);
                        infoItem.setItemMeta(infoMeta);
                        contents.set(0, 4, ClickableItem.empty(infoItem));

                        ItemStack listButton = new ItemStack(Material.BOOK);
                        ItemMeta listMeta = listButton.getItemMeta();
                        listMeta.setDisplayName(CC.translate("&e&lVer Territorios"));
                        listMeta.setLore(Arrays.asList(
                                CC.translate("&7Lista de todos los territorios"),
                                CC.translate("&7registrados en el servidor"),
                                "",
                                CC.translate("&7Total: &f" + total),
                                "",
                                CC.translate("&e[CLICK PARA VER]")
                        ));
                        listButton.setItemMeta(listMeta);
                        contents.set(1, 2, ClickableItem.of(listButton, e -> {
                            TerritoryListMenu.createListMenu(1).open(player);
                        }));

                        ItemStack myTerrainButton = new ItemStack(Material.CHEST);
                        ItemMeta myMeta = myTerrainButton.getItemMeta();
                        myMeta.setDisplayName(CC.translate("&b&lMis Territorios"));
                        List<String> myLore = new ArrayList<String>();
                        myLore.add(CC.translate("&7Ve los territorios que posees"));
                        myLore.add("");
                        int myCount = 0;
                        for (Terrain t : all.values()) {
                            if (t.isOwner(player.getUniqueId())) myCount++;
                        }
                        myLore.add(CC.translate("&7Tus territorios: &f" + myCount));
                        myLore.add("");
                        myLore.add(CC.translate("&b[CLICK PARA VER]"));
                        myMeta.setLore(myLore);
                        myTerrainButton.setItemMeta(myMeta);
                        contents.set(1, 4, ClickableItem.of(myTerrainButton, e -> {
                            TerritoryMyMenu.createMyMenu(player).open(player);
                        }));

                        ItemStack infoHereButton = new ItemStack(Material.COMPASS);
                        ItemMeta hereMetaBtn = infoHereButton.getItemMeta();
                        hereMetaBtn.setDisplayName(CC.translate("&a&lTerreno Aquí"));
                        hereMetaBtn.setLore(Arrays.asList(
                                CC.translate("&7Muestra información del terreno"),
                                CC.translate("&7en tu posición actual"),
                                "",
                                CC.translate("&a[CLICK PARA VER]")
                        ));
                        infoHereButton.setItemMeta(hereMetaBtn);
                        contents.set(1, 6, ClickableItem.of(infoHereButton, e -> {
                            Terrain terrain = TerrainManager.getInstance().getTerrainAt(player.getLocation());
                            if (terrain == null) {
                                player.sendMessage(CC.translate("&7No estás en ningún territorio."));
                                return;
                            }
                            TerritoryInfoMenu.createInfoMenu(terrain, player).open(player);
                        }));

                        if (player.hasPermission(ClaimsPermissions.TERRAIN_CREATE)
                                || player.hasPermission(ClaimsPermissions.TERRAIN_DELETE)
                                || player.hasPermission(ClaimsPermissions.TERRAIN_COMMIT)) {

                            ItemStack adminButton = new ItemStack(Material.COMMAND);
                            ItemMeta adminMeta = adminButton.getItemMeta();
                            adminMeta.setDisplayName(CC.translate("&c&lAdministración"));
                            adminMeta.setLore(Arrays.asList(
                                    CC.translate("&7Gestiona territorios del servidor"),
                                    CC.translate("&7Crear, eliminar, asignar precios"),
                                    "",
                                    CC.translate("&c[CLICK PARA GESTIONAR]")
                            ));
                            adminButton.setItemMeta(adminMeta);
                            contents.set(2, 4, ClickableItem.of(adminButton, e -> {
                                TerritoryAdminMenu.createAdminMenu(player).open(player);
                            }));
                        }

                        ItemStack closeButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta closeMeta = closeButton.getItemMeta();
                        closeMeta.setDisplayName(CC.translate("&c&lCerrar"));
                        closeButton.setItemMeta(closeMeta);
                        contents.set(3, 4, ClickableItem.of(closeButton, e -> {
                            player.closeInventory();
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&6&lTerritorios"))
                .build();
    }

    static ItemStack createGlassPane(short color) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(CC.translate("&8"));
        glass.setItemMeta(glassMeta);
        return glass;
    }
}