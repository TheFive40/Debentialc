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

public class LeaseOfferTerrainMenu {

    public static SmartInventory createMenu(final Player owner) {
        return SmartInventory.builder()
                .id("lease_offer_terrain_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 4)));

                        List<Terrain> eligible = new ArrayList<Terrain>();
                        for (Terrain t : TerrainManager.getInstance().getAll().values()) {
                            if (t.isOwner(player.getUniqueId()) && t.getChunks() >= LeaseManager.MIN_CHUNKS_TO_LEASE
                                    && t.isCommitted() && !LeaseManager.getInstance().isSubTerrain(t.getId())) {
                                eligible.add(t);
                            }
                        }

                        ItemStack titleItem = new ItemStack(Material.GRASS);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&a&lTus Terrenos para Arrendar"));
                        List<String> titleLore = new ArrayList<String>();
                        titleLore.add(CC.translate("&7Mínimo &f" + LeaseManager.MIN_CHUNKS_TO_LEASE + " chunks &7para poder subarrendar"));
                        titleLore.add(CC.translate("&7Elegibles: &f" + eligible.size()));
                        titleMeta.setLore(titleLore);
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        if (eligible.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.ANVIL);
                            ItemMeta emptyMeta = emptyItem.getItemMeta();
                            emptyMeta.setDisplayName(CC.translate("&7Ningún terreno elegible"));
                            emptyMeta.setLore(Arrays.asList(
                                    CC.translate("&7Necesitas ser dueño de un terreno"),
                                    CC.translate("&7con al menos &f" + LeaseManager.MIN_CHUNKS_TO_LEASE + " chunks &7generado")
                            ));
                            emptyItem.setItemMeta(emptyMeta);
                            contents.set(2, 4, ClickableItem.empty(emptyItem));
                        }

                        int row = 1;
                        int col = 1;
                        for (Terrain terrain : eligible) {
                            int activeLeases = 0;
                            for (LeaseContract c : LeaseManager.getInstance().getContractsByOwner(player.getUniqueId())) {
                                if (c.getParentTerrainId().equals(terrain.getId())
                                        && c.getStatus() == org.debentialc.claims.models.LeaseContract.ContractStatus.ACTIVE) {
                                    activeLeases++;
                                }
                            }

                            ItemStack item = new ItemStack(Material.GRASS);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(CC.translate("&f&l" + terrain.getId()));
                            List<String> lore = new ArrayList<String>();
                            lore.add(CC.translate("&7Tamaño: &f" + terrain.getChunks() + " chunks"));
                            lore.add(CC.translate("&7Disponibles para subarrendar: &f" + (terrain.getChunks() - 1) + " chunks max"));
                            lore.add(CC.translate("&7Contratos activos: &f" + activeLeases));
                            lore.add("");
                            lore.add(CC.translate("&7Usa &f/lease offer " + terrain.getId() + " <jugador> <chunks> <precio> <días>"));
                            lore.add(CC.translate("&7o el comando en el chat para ofrecer"));
                            meta.setLore(lore);
                            item.setItemMeta(meta);

                            final Terrain t = terrain;
                            contents.set(row, col, ClickableItem.of(item, e -> {
                                player.closeInventory();
                                player.sendMessage(CC.translate("&8&m                                   "));
                                player.sendMessage(CC.translate("  &6&lOfrecer Arrendamiento: &f" + t.getId()));
                                player.sendMessage(CC.translate("  &7Chunks disponibles: &f1 a " + (t.getChunks() - 1)));
                                player.sendMessage(CC.translate("  &7Ciclo mín: &f" + LeaseManager.MIN_CYCLE_DAYS + " días &7| Máx: &f" + LeaseManager.MAX_CYCLE_DAYS + " días"));
                                player.sendMessage(CC.translate("  &7Uso: &f/lease offer " + t.getId() + " <jugador> <chunks> <precio> <días>"));
                                player.sendMessage(CC.translate("&8&m                                   "));
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
                .title(CC.translate("&a&lTerrenos Arrendables"))
                .build();
    }
}