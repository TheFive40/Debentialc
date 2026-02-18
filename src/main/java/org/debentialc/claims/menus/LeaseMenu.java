package org.debentialc.claims.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaseMenu {

    public static SmartInventory createMainMenu(final Player viewer) {
        return SmartInventory.builder()
                .id("lease_main_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 11)));

                        List<LeaseContract> asOwner = LeaseManager.getInstance().getContractsByOwner(player.getUniqueId());
                        List<LeaseContract> asTenant = LeaseManager.getInstance().getContractsByTenant(player.getUniqueId());
                        List<LeaseContract> pending = LeaseManager.getInstance().getPendingForPlayer(player.getUniqueId());

                        int activeAsOwner = 0;
                        int activeAsTenant = 0;
                        for (LeaseContract c : asOwner) {
                            if (c.getStatus() == LeaseContract.ContractStatus.ACTIVE) activeAsOwner++;
                        }
                        for (LeaseContract c : asTenant) {
                            if (c.getStatus() == LeaseContract.ContractStatus.ACTIVE) activeAsTenant++;
                        }

                        ItemStack titleItem = new ItemStack(Material.PAPER);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&6&lSistema de Arrendamiento"));
                        List<String> titleLore = new ArrayList<String>();
                        titleLore.add(CC.translate("&7Contratos como dueño: &f" + asOwner.size() + " &8(" + activeAsOwner + " activos)"));
                        titleLore.add(CC.translate("&7Contratos como inquilino: &f" + asTenant.size() + " &8(" + activeAsTenant + " activos)"));
                        if (!pending.isEmpty()) {
                            titleLore.add(CC.translate("&e⚠ Tienes &f" + pending.size() + " &ependiente(s)"));
                        }
                        titleMeta.setLore(titleLore);
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        ItemStack ownerButton = new ItemStack(Material.GOLD_BLOCK);
                        ItemMeta ownerMeta = ownerButton.getItemMeta();
                        ownerMeta.setDisplayName(CC.translate("&6&lMis Contratos (Dueño)"));
                        ownerMeta.setLore(Arrays.asList(
                                CC.translate("&7Contratos donde eres propietario"),
                                CC.translate("&7Total: &f" + asOwner.size()),
                                CC.translate("&7Activos: &f" + activeAsOwner),
                                "",
                                CC.translate("&6[CLICK PARA VER]")
                        ));
                        ownerButton.setItemMeta(ownerMeta);
                        contents.set(1, 2, ClickableItem.of(ownerButton, e -> {
                            LeaseContractListMenu.createOwnerList(player).open(player);
                        }));

                        ItemStack tenantButton = new ItemStack(Material.BED);
                        ItemMeta tenantMeta = tenantButton.getItemMeta();
                        tenantMeta.setDisplayName(CC.translate("&b&lMis Arrendamientos (Inquilino)"));
                        tenantMeta.setLore(Arrays.asList(
                                CC.translate("&7Contratos donde eres inquilino"),
                                CC.translate("&7Total: &f" + asTenant.size()),
                                CC.translate("&7Activos: &f" + activeAsTenant),
                                "",
                                CC.translate("&b[CLICK PARA VER]")
                        ));
                        tenantButton.setItemMeta(tenantMeta);
                        contents.set(1, 4, ClickableItem.of(tenantButton, e -> {
                            LeaseContractListMenu.createTenantList(player).open(player);
                        }));

                        ItemStack pendingButton;
                        if (!pending.isEmpty()) {
                            pendingButton = new ItemStack(Material.GOLD_INGOT);
                        } else {
                            pendingButton = new ItemStack(Material.IRON_INGOT);
                        }
                        ItemMeta pendingMeta = pendingButton.getItemMeta();
                        pendingMeta.setDisplayName(CC.translate(pending.isEmpty() ? "&7Sin pendientes" : "&e&lPendientes &8(" + pending.size() + ")"));
                        pendingMeta.setLore(Arrays.asList(
                                CC.translate("&7Contratos esperando tu respuesta"),
                                CC.translate("&7o que requieren acción tuya"),
                                "",
                                CC.translate(pending.isEmpty() ? "&8No hay nada pendiente" : "&e[CLICK PARA VER]")
                        ));
                        pendingButton.setItemMeta(pendingMeta);
                        if (!pending.isEmpty()) {
                            contents.set(1, 6, ClickableItem.of(pendingButton, e -> {
                                LeasePendingMenu.createPendingMenu(player).open(player);
                            }));
                        } else {
                            contents.set(1, 6, ClickableItem.empty(pendingButton));
                        }

                        ItemStack myTerrains = new ItemStack(Material.GRASS);
                        ItemMeta myTerrainsMeta = myTerrains.getItemMeta();
                        myTerrainsMeta.setDisplayName(CC.translate("&a&lOfrecer Contrato"));
                        myTerrainsMeta.setLore(Arrays.asList(
                                CC.translate("&7Ofrece un sub-terreno a otro jugador"),
                                CC.translate("&7desde uno de tus terrenos"),
                                CC.translate("&8Mínimo " + LeaseManager.MIN_CHUNKS_TO_LEASE + " chunks para arrendar"),
                                "",
                                CC.translate("&a[CLICK PARA VER TUS TERRENOS]")
                        ));
                        myTerrains.setItemMeta(myTerrainsMeta);
                        contents.set(2, 3, ClickableItem.of(myTerrains, e -> {
                            LeaseOfferTerrainMenu.createMenu(player).open(player);
                        }));

                        ItemStack helpButton = new ItemStack(Material.PAPER);
                        ItemMeta helpMeta = helpButton.getItemMeta();
                        helpMeta.setDisplayName(CC.translate("&7&lAyuda"));
                        helpMeta.setLore(Arrays.asList(
                                CC.translate("&f/lease offer <terreno> <jugador> <chunks> <precio> <días>"),
                                CC.translate("&f/lease request <terreno> <chunks> <precio> <días> <dueño>"),
                                CC.translate("&f/lease accept <contractId>"),
                                CC.translate("&f/lease assign <contractId>"),
                                CC.translate("&f/lease cancel <contractId>"),
                                CC.translate("&f/lease pay <contractId>"),
                                "",
                                CC.translate("&7Ciclo mín: &f" + LeaseManager.MIN_CYCLE_DAYS + " días &7| Máx: &f" + LeaseManager.MAX_CYCLE_DAYS + " días"),
                                CC.translate("&7Gracia por impago: &f" + LeaseManager.GRACE_PERIOD_DAYS + " días")
                        ));
                        helpButton.setItemMeta(helpMeta);
                        contents.set(2, 5, ClickableItem.empty(helpButton));

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Territorios"));
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
                .title(CC.translate("&6&lArrendamientos"))
                .build();
    }
}