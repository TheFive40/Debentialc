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

import java.util.Arrays;
import java.util.List;

public class LeasePendingMenu {

    public static SmartInventory createPendingMenu(final Player viewer) {
        return SmartInventory.builder()
                .id("lease_pending_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 14)));

                        List<LeaseContract> pending = LeaseManager.getInstance().getPendingForPlayer(player.getUniqueId());

                        ItemStack titleItem = new ItemStack(Material.GOLD_INGOT);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&e&lContratos Pendientes"));
                        titleMeta.setLore(Arrays.asList(CC.translate("&7Requieren tu atención: &f" + pending.size())));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        if (pending.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.ANVIL);
                            ItemMeta emptyMeta = emptyItem.getItemMeta();
                            emptyMeta.setDisplayName(CC.translate("&7No hay contratos pendientes"));
                            emptyItem.setItemMeta(emptyMeta);
                            contents.set(2, 4, ClickableItem.empty(emptyItem));
                        }

                        int row = 1;
                        int col = 1;
                        for (LeaseContract contract : pending) {
                            boolean isOwner = contract.getOwnerId().equals(player.getUniqueId());
                            ItemStack item = LeaseContractListMenu.makeContractItem(contract, isOwner);
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
                .title(CC.translate("&e&lPendientes"))
                .build();
    }
}