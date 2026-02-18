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
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.service.CC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaseContractListMenu {

    public static SmartInventory createOwnerList(final Player owner) {
        return SmartInventory.builder()
                .id("lease_owner_list")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 4)));

                        List<LeaseContract> contracts = LeaseManager.getInstance().getContractsByOwner(player.getUniqueId());

                        ItemStack titleItem = new ItemStack(Material.GOLD_BLOCK);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&6&lMis Contratos (Dueño)"));
                        titleMeta.setLore(Arrays.asList(CC.translate("&7Total: &f" + contracts.size())));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        if (contracts.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.ANVIL);
                            ItemMeta emptyMeta = emptyItem.getItemMeta();
                            emptyMeta.setDisplayName(CC.translate("&7No tienes contratos como dueño"));
                            emptyItem.setItemMeta(emptyMeta);
                            contents.set(2, 4, ClickableItem.empty(emptyItem));
                        }

                        int row = 1;
                        int col = 1;
                        for (LeaseContract contract : contracts) {
                            ItemStack item = makeContractItem(contract, true);
                            final LeaseContract c = contract;
                            contents.set(row, col, ClickableItem.of(item, e -> {
                                LeaseContractDetailMenu.createDetailMenu(c, player).open(player);
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
                            LeaseMenu.createMainMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&6&lContratos (Dueño)"))
                .build();
    }

    public static SmartInventory createTenantList(final Player tenant) {
        return SmartInventory.builder()
                .id("lease_tenant_list")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 3)));

                        List<LeaseContract> contracts = LeaseManager.getInstance().getContractsByTenant(player.getUniqueId());

                        ItemStack titleItem = new ItemStack(Material.BED);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&b&lMis Arrendamientos (Inquilino)"));
                        titleMeta.setLore(Arrays.asList(CC.translate("&7Total: &f" + contracts.size())));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        if (contracts.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.ANVIL);
                            ItemMeta emptyMeta = emptyItem.getItemMeta();
                            emptyMeta.setDisplayName(CC.translate("&7No tienes contratos como inquilino"));
                            emptyItem.setItemMeta(emptyMeta);
                            contents.set(2, 4, ClickableItem.empty(emptyItem));
                        }

                        int row = 1;
                        int col = 1;
                        for (LeaseContract contract : contracts) {
                            ItemStack item = makeContractItem(contract, false);
                            final LeaseContract c = contract;
                            contents.set(row, col, ClickableItem.of(item, e -> {
                                LeaseContractDetailMenu.createDetailMenu(c, player).open(player);
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
                            LeaseMenu.createMainMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&b&lArrendamientos (Inquilino)"))
                .build();
    }

    static ItemStack makeContractItem(LeaseContract contract, boolean asOwner) {
        Material mat;
        LeaseContract.ContractStatus status = contract.getStatus();
        if (status == LeaseContract.ContractStatus.ACTIVE) {
            mat = Material.EMERALD;
        } else if (status == LeaseContract.ContractStatus.GRACE_PERIOD) {
            mat = Material.REDSTONE;
        } else if (status == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN) {
            mat = Material.GOLD_INGOT;
        } else if (status == LeaseContract.ContractStatus.PENDING_OWNER || status == LeaseContract.ContractStatus.PENDING_TENANT) {
            mat = Material.PAPER;
        } else {
            mat = Material.COAL;
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String otherParty = asOwner ? contract.getTenantName() : contract.getOwnerName();
        meta.setDisplayName(CC.translate("&f" + (contract.getSubTerrainId() != null ? contract.getSubTerrainId() : contract.getContractId())));

        List<String> lore = new ArrayList<String>();
        lore.add(CC.translate(asOwner ? "&7Inquilino: &f" + otherParty : "&7Propietario: &f" + otherParty));
        lore.add(CC.translate("&7Terreno padre: &f" + contract.getParentTerrainId()));
        lore.add(CC.translate("&7Chunks: &f" + contract.getChunks()));
        lore.add(CC.translate("&7Pago: &f$" + (int) contract.getPricePerCycle() + " &7c/" + contract.getCycleDays() + " días"));
        lore.add(CC.translate("&7Estado: " + formatStatus(status)));
        if (status == LeaseContract.ContractStatus.ACTIVE) {
            lore.add(CC.translate("&7Próximo pago en: &f" + contract.getDaysUntilPayment() + " día(s)"));
        }
        if (status == LeaseContract.ContractStatus.GRACE_PERIOD) {
            lore.add(CC.translate("&cGracia termina en: &f" + contract.getDaysUntilGraceEnd() + " día(s)"));
        }
        lore.add("");
        lore.add(CC.translate("&a[CLICK PARA VER DETALLE]"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    static String formatStatus(LeaseContract.ContractStatus status) {
        switch (status) {
            case PENDING_OWNER: return "&ePendiente (dueño)";
            case PENDING_TENANT: return "&ePendiente (inquilino)";
            case AWAITING_SUBTERRAIN: return "&6Esperando sub-terreno";
            case ACTIVE: return "&aActivo";
            case GRACE_PERIOD: return "&cPeríodo de gracia";
            case EXPIRED: return "&8Expirado";
            case CANCELLED: return "&8Cancelado";
            default: return "&7-";
        }
    }
}