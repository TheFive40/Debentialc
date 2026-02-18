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

public class TerritoryAdminMenu {

    public static SmartInventory createAdminMenu(final Player admin) {
        return SmartInventory.builder()
                .id("territory_admin_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 14)));

                        Map<String, Terrain> all = TerrainManager.getInstance().getAll();
                        int total = all.size();
                        int uncommitted = 0;
                        int forSale = 0;
                        int owned = 0;
                        for (Terrain t : all.values()) {
                            if (!t.isCommitted()) uncommitted++;
                            else if (t.hasOwner()) owned++;
                            else forSale++;
                        }

                        ItemStack titleItem = new ItemStack(Material.COMMAND);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&c&lPanel de Administración"));
                        List<String> titleLore = new ArrayList<String>();
                        titleLore.add(CC.translate("&7Total: &f" + total + " territorio(s)"));
                        titleLore.add(CC.translate("&7Sin generar: &8" + uncommitted));
                        titleLore.add(CC.translate("&7En venta: &a" + forSale));
                        titleLore.add(CC.translate("&7Ocupados: &c" + owned));
                        titleMeta.setLore(titleLore);
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        ItemStack pendingButton = new ItemStack(Material.DIRT);
                        ItemMeta pendingMeta = pendingButton.getItemMeta();
                        pendingMeta.setDisplayName(CC.translate("&8&lSin Generar &7(" + uncommitted + ")"));
                        pendingMeta.setLore(Arrays.asList(
                                CC.translate("&7Territorios creados pero no"),
                                CC.translate("&7colocados en el mundo todavía"),
                                "",
                                CC.translate("&8[CLICK PARA VER]")
                        ));
                        pendingButton.setItemMeta(pendingMeta);
                        contents.set(1, 2, ClickableItem.of(pendingButton, e -> {
                            TerritoryAdminFilterMenu.createFilterMenu("uncommitted", player).open(player);
                        }));

                        ItemStack saleButton = new ItemStack(Material.EMERALD_BLOCK);
                        ItemMeta saleMeta = saleButton.getItemMeta();
                        saleMeta.setDisplayName(CC.translate("&a&lEn Venta &7(" + forSale + ")"));
                        saleMeta.setLore(Arrays.asList(
                                CC.translate("&7Territorios disponibles para comprar"),
                                "",
                                CC.translate("&a[CLICK PARA VER]")
                        ));
                        saleButton.setItemMeta(saleMeta);
                        contents.set(1, 4, ClickableItem.of(saleButton, e -> {
                            TerritoryAdminFilterMenu.createFilterMenu("forsale", player).open(player);
                        }));

                        ItemStack ownedButton = new ItemStack(Material.GRASS);
                        ItemMeta ownedMeta = ownedButton.getItemMeta();
                        ownedMeta.setDisplayName(CC.translate("&c&lOcupados &7(" + owned + ")"));
                        ownedMeta.setLore(Arrays.asList(
                                CC.translate("&7Territorios con propietario asignado"),
                                "",
                                CC.translate("&c[CLICK PARA VER]")
                        ));
                        ownedButton.setItemMeta(ownedMeta);
                        contents.set(1, 6, ClickableItem.of(ownedButton, e -> {
                            TerritoryAdminFilterMenu.createFilterMenu("owned", player).open(player);
                        }));

                        ItemStack helpItem = new ItemStack(Material.PAPER);
                        ItemMeta helpMeta = helpItem.getItemMeta();
                        helpMeta.setDisplayName(CC.translate("&7&lComandos Disponibles"));
                        helpMeta.setLore(Arrays.asList(
                                CC.translate("&f/terrain create <id> <chunks>"),
                                CC.translate("&f/terrain price <id> <precio>"),
                                CC.translate("&f/terrain commit <id>"),
                                CC.translate("&f/terrain delete <id>"),
                                CC.translate("&f/terrain dissolve <id>"),
                                CC.translate("&f/terrain transfer <id> <jugador>"),
                                "",
                                CC.translate("&7Usa los menús para gestionar")
                        ));
                        helpItem.setItemMeta(helpMeta);
                        contents.set(2, 4, ClickableItem.empty(helpItem));

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 4, ClickableItem.of(backButton, e -> {
                            TerritoryMenu.createMainMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&c&lAdmin - Territorios"))
                .build();
    }
}