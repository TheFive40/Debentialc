package org.example.tools.inventory;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.commands.items.CustomItemCommand;
import org.example.tools.CC;
import org.example.tools.ci.CustomItem;

import java.util.*;

public class CustomItemMenus {

    /**
     * Menú principal de items custom
     */
    public static SmartInventory createMainMenu() {
        return SmartInventory.builder()
                .id("ci_menu_main")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        // Botón crear item
                        ItemStack createButton = new ItemStack(Material.EMERALD_BLOCK);
                        ItemMeta createMeta = createButton.getItemMeta();
                        createMeta.setDisplayName(CC.translate("&a&lCrear Item"));
                        createMeta.setLore(Arrays.asList(
                                CC.translate("&7Crea un nuevo item custom"),
                                CC.translate("&7Debes sostener el item en mano")
                        ));
                        createButton.setItemMeta(createMeta);

                        contents.set(1, 1, ClickableItem.of(createButton, e -> {
                            player.closeInventory();
                            ItemCreationManager.startItemCreation(player);
                        }));

                        ItemStack listButton = new ItemStack(Material.BOOK);
                        ItemMeta listMeta = listButton.getItemMeta();
                        listMeta.setDisplayName(CC.translate("&c&lListar Items"));
                        listMeta.setLore(Arrays.asList(
                                CC.translate("&7Ve todos los items registrados"),
                                CC.translate("&7Y edítalos desde el menú")
                        ));
                        listButton.setItemMeta(listMeta);

                        contents.set(1, 4, ClickableItem.of(listButton, e -> {
                            openItemListMenu(1).open(player);
                        }));

                        ItemStack helpButton = new ItemStack(Material.PAPER);
                        ItemMeta helpMeta = helpButton.getItemMeta();
                        helpMeta.setDisplayName(CC.translate("&e&lAyuda"));
                        helpMeta.setLore(Arrays.asList(
                                CC.translate("&7Abre el menú de ayuda"),
                                CC.translate("&7Con todos los comandos disponibles")
                        ));
                        helpButton.setItemMeta(helpMeta);

                        contents.set(1, 7, ClickableItem.of(helpButton, e -> {
                            openHelpMenu().open(player);
                        }));

                        ItemStack closeButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta closeMeta = closeButton.getItemMeta();
                        closeMeta.setDisplayName(CC.translate("&r&lCerrar"));
                        closeButton.setItemMeta(closeMeta);

                        contents.set(2, 4, ClickableItem.of(closeButton, e -> {
                            player.closeInventory();
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(3, 9)
                .title(CC.translate("&c&lGestión de Items Custom"))
                .build();
    }


    public static SmartInventory openItemListMenu(int page) {
        return SmartInventory.builder()
                .id("ci_list_menu_" + page)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        Set<String> itemIds = CustomItemCommand.items.keySet();
                        int pageSize = 21;
                        int totalPages = (int) Math.ceil((double) itemIds.size() / pageSize);

                        if (page < 1) return;
                        if (page > totalPages) return;

                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        List<String> ids = new ArrayList<>(itemIds);
                        int start = (page - 1) * pageSize;
                        int end = Math.min(start + pageSize, ids.size());

                        int row = 1;
                        int col = 1;

                        for (int i = start; i < end; i++) {
                            String id = ids.get(i);
                            CustomItem customItem = CustomItemCommand.items.get(id);

                            ItemStack displayItem = new ItemStack(customItem.getMaterial());
                            ItemMeta meta = displayItem.getItemMeta();
                            meta.setDisplayName(CC.translate("&e" + id));

                            List<String> lore = new ArrayList<>();
                            lore.add(CC.translate("&7Nombre: &f" + customItem.getDisplayName()));
                            lore.add(CC.translate("&7Stats: &f" + customItem.getValueByStat().size()));
                            lore.add(CC.translate("&7Efectos: &f" + customItem.getEffects().size()));
                            lore.add("");
                            lore.add(CC.translate("&a[CLICK PARA EDITAR]"));

                            meta.setLore(lore);
                            displayItem.setItemMeta(meta);

                            String finalId = id;
                            contents.set(row, col, ClickableItem.of(displayItem, e -> {
                                openEditItemMenu(finalId).open(player);
                            }));

                            col++;
                            if (col >= 8) {
                                col = 1;
                                row++;

                                if (row >= 4) {
                                    break;
                                }
                            }
                        }

                        if (page > 1) {
                            ItemStack prevButton = new ItemStack(Material.ARROW);
                            ItemMeta prevMeta = prevButton.getItemMeta();
                            prevMeta.setDisplayName(CC.translate("&b← Anterior"));
                            prevButton.setItemMeta(prevMeta);

                            contents.set(4, 2, ClickableItem.of(prevButton, e -> {
                                openItemListMenu(page - 1).open(player);
                            }));
                        }

                        ItemStack pageButton = new ItemStack(Material.BOOK);
                        ItemMeta pageMeta = pageButton.getItemMeta();
                        pageMeta.setDisplayName(CC.translate("&f&lPágina " + page + "/" + totalPages));
                        pageButton.setItemMeta(pageMeta);
                        contents.set(4, 4, ClickableItem.empty(pageButton));

                        if (page < totalPages) {
                            ItemStack nextButton = new ItemStack(Material.ARROW);
                            ItemMeta nextMeta = nextButton.getItemMeta();
                            nextMeta.setDisplayName(CC.translate("&bSiguiente →"));
                            nextButton.setItemMeta(nextMeta);

                            contents.set(4, 6, ClickableItem.of(nextButton, e -> {
                                openItemListMenu(page + 1).open(player);
                            }));
                        }

                        ItemStack backButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&r← Atrás"));
                        backButton.setItemMeta(backMeta);

                        contents.set(4, 8, ClickableItem.of(backButton, e -> {
                            createMainMenu().open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(6, 9)
                .title(CC.translate("&c&lItems Custom - Página " + page))
                .build();
    }

    /**
     * Menú de edición de items - ACTUALIZADO CON BOTÓN DE STATS
     */
    public static SmartInventory openEditItemMenu(String itemId) {
        CustomItem item = CustomItemCommand.items.get(itemId);
        if (item == null) {
            return null;
        }

        return SmartInventory.builder()
                .id("ci_edit_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        // Información del item
                        ItemStack infoItem = new ItemStack(Material.PAPER);
                        ItemMeta infoMeta = infoItem.getItemMeta();
                        infoMeta.setDisplayName(CC.translate("&c&l" + itemId));
                        List<String> infoLore = new ArrayList<>();
                        infoLore.add(CC.translate("&7ID: &f" + item.getId()));
                        infoLore.add(CC.translate("&7Nombre: &f" + item.getDisplayName()));
                        infoLore.add(CC.translate("&7Material: &f" + item.getMaterial()));
                        infoLore.add(CC.translate("&7Estado: &f" + (item.isActive() ? "Activo" : "Inactivo")));
                        infoItem.setItemMeta(infoMeta);
                        contents.set(1, 1, ClickableItem.empty(infoItem));

                        // Botón editar nombre
                        ItemStack renameButton = new ItemStack(Material.NAME_TAG);
                        ItemMeta renameMeta = renameButton.getItemMeta();
                        renameMeta.setDisplayName(CC.translate("&e&lEditar Nombre"));
                        renameMeta.setLore(Arrays.asList(
                                CC.translate("&7Actual: &f" + item.getDisplayName()),
                                CC.translate("&a/ci rename <texto>")
                        ));
                        renameButton.setItemMeta(renameMeta);
                        contents.set(1, 2, ClickableItem.of(renameButton, e -> {
                            player.closeInventory();
                            player.sendMessage(CC.translate("&b/ci rename <nombre> &7- Para cambiar el nombre"));
                        }));

                        // Botón editar lore
                        ItemStack loreButton = new ItemStack(Material.BOOK_AND_QUILL);
                        ItemMeta loreMeta = loreButton.getItemMeta();
                        loreMeta.setDisplayName(CC.translate("&e&lEditar Lore"));
                        loreMeta.setLore(Arrays.asList(
                                CC.translate("&7Líneas: &f" + (item.getLore() != null ? item.getLore().size() : 0)),
                                CC.translate("&a/ci addline <texto>"),
                                CC.translate("&a/ci setline <línea> <texto>")
                        ));
                        loreButton.setItemMeta(loreMeta);
                        contents.set(1, 3, ClickableItem.of(loreButton, e -> {
                            player.closeInventory();
                            player.sendMessage(CC.translate("&b/ci addline <texto> &7- Para agregar lore"));
                        }));

                        // Botón agregar stats (NUEVO)
                        ItemStack statsButton = new ItemStack(Material.REDSTONE);
                        ItemMeta statsMeta = statsButton.getItemMeta();
                        statsMeta.setDisplayName(CC.translate("&a&lAñadir Stats"));
                        List<String> statsLore = new ArrayList<>();
                        statsLore.add(CC.translate("&7Stats actuales: &f" + item.getValueByStat().size()));
                        item.getValueByStat().forEach((stat, value) -> {
                            String op = item.getOperation().getOrDefault(stat, "+");
                            statsLore.add(CC.translate("&f  " + stat + " " + op + value));
                        });
                        statsLore.add("");
                        statsLore.add(CC.translate("&a[CLICK PARA AGREGAR]"));
                        statsMeta.setLore(statsLore);
                        statsButton.setItemMeta(statsMeta);
                        contents.set(1, 4, ClickableItem.of(statsButton, e -> {
                            BonusFlowManager.startBonusFlow(player, itemId);
                            CustomItemBonusMenus.createStatSelectionMenu(itemId).open(player);
                        }));

                        // Botón ver efectos
                        ItemStack effectsButton = new ItemStack(Material.REDSTONE_TORCH_ON);
                        ItemMeta effectsMeta = effectsButton.getItemMeta();
                        effectsMeta.setDisplayName(CC.translate("&6&lVer Efectos"));
                        List<String> effectsLore = new ArrayList<>();
                        effectsLore.add(CC.translate("&7Efectos activos:"));
                        item.getEffects().forEach((effect, value) -> {
                            effectsLore.add(CC.translate("&f  " + effect + ": " + (value * 100) + "%"));
                        });
                        effectsMeta.setLore(effectsLore);
                        effectsButton.setItemMeta(effectsMeta);
                        contents.set(1, 5, ClickableItem.of(effectsButton, e -> {
                            openEffectsMenu(itemId).open(player);
                        }));

                        // Botón eliminar
                        ItemStack deleteButton = new ItemStack(Material.ANVIL);
                        ItemMeta deleteMeta = deleteButton.getItemMeta();
                        deleteMeta.setDisplayName(CC.translate("&c&lEliminar Item"));
                        deleteMeta.setLore(Arrays.asList(
                                CC.translate("&7Este item será eliminado"),
                                CC.translate("&a[CLICK PARA CONFIRMAR]")
                        ));
                        deleteButton.setItemMeta(deleteMeta);
                        contents.set(1, 7, ClickableItem.of(deleteButton, e -> {
                            openDeleteConfirmMenu(itemId).open(player);
                        }));

                        // Botón atrás
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 4, ClickableItem.of(backButton, e -> {
                            openItemListMenu(1).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&c&lEditar Item: " + itemId))
                .build();
    }

    /**
     * Menú de visualización de stats
     */
    public static SmartInventory openStatsMenu(String itemId) {
        CustomItem item = CustomItemCommand.items.get(itemId);
        if (item == null) return null;

        return SmartInventory.builder()
                .id("ci_stats_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        int row = 1;
                        for (Map.Entry<String, Double> entry : item.getValueByStat().entrySet()) {
                            ItemStack statItem = new ItemStack(Material.REDSTONE);
                            ItemMeta statMeta = statItem.getItemMeta();
                            String operation = item.getOperation().getOrDefault(entry.getKey(), "+");
                            statMeta.setDisplayName(CC.translate("&a&l" + entry.getKey()));
                            statMeta.setLore(Arrays.asList(
                                    CC.translate("&7Operación: &f" + operation),
                                    CC.translate("&7Valor: &f" + entry.getValue())
                            ));
                            statItem.setItemMeta(statMeta);
                            contents.set(row, 4, ClickableItem.empty(statItem));
                            row++;
                        }

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(5, 4, ClickableItem.of(backButton, e -> {
                            openEditItemMenu(itemId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(6, 9)
                .title(CC.translate("&c&lStats: " + itemId))
                .build();
    }

    /**
     * Menú de visualización de efectos
     */
    public static SmartInventory openEffectsMenu(String itemId) {
        CustomItem item = CustomItemCommand.items.get(itemId);
        if (item == null) return null;

        return SmartInventory.builder()
                .id("ci_effects_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        int row = 1;
                        for (Map.Entry<String, Double> entry : item.getEffects().entrySet()) {
                            ItemStack effectItem = new ItemStack(Material.REDSTONE_TORCH_ON);
                            ItemMeta effectMeta = effectItem.getItemMeta();
                            effectMeta.setDisplayName(CC.translate("&6&l" + entry.getKey()));
                            effectMeta.setLore(Arrays.asList(
                                    CC.translate("&7Valor: &f" + (entry.getValue() * 100) + "%")
                            ));
                            effectItem.setItemMeta(effectMeta);
                            contents.set(row, 4, ClickableItem.empty(effectItem));
                            row++;
                        }

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(5, 4, ClickableItem.of(backButton, e -> {
                            openEditItemMenu(itemId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(6, 9)
                .title(CC.translate("&c&lEfectos: " + itemId))
                .build();
    }

    /**
     * Menú de confirmación de eliminación
     */
    public static SmartInventory openDeleteConfirmMenu(String itemId) {
        return SmartInventory.builder()
                .id("ci_delete_confirm_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        // Mensaje de confirmación
                        ItemStack confirmItem = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta confirmMeta = confirmItem.getItemMeta();
                        confirmMeta.setDisplayName(CC.translate("&c&l¿Estás seguro?"));
                        confirmMeta.setLore(Arrays.asList(
                                CC.translate("&7Esta acción no puede deshacerse"),
                                CC.translate("&7El item &f" + itemId + "&7 será eliminado permanentemente")
                        ));
                        confirmItem.setItemMeta(confirmMeta);
                        contents.set(1, 4, ClickableItem.empty(confirmItem));

                        // Botón confirmar eliminación
                        ItemStack yesButton = new ItemStack(Material.EMERALD_BLOCK);
                        ItemMeta yesMeta = yesButton.getItemMeta();
                        yesMeta.setDisplayName(CC.translate("&a&lSÍ, ELIMINAR"));
                        yesButton.setItemMeta(yesMeta);
                        contents.set(2, 3, ClickableItem.of(yesButton, e -> {
                            CustomItemCommand.items.remove(itemId);
                            player.sendMessage(CC.translate("&aItem eliminado correctamente"));
                            openItemListMenu(1).open(player);
                        }));

                        // Botón cancelar
                        ItemStack noButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta noMeta = noButton.getItemMeta();
                        noMeta.setDisplayName(CC.translate("&c&lCANCELAR"));
                        noButton.setItemMeta(noMeta);
                        contents.set(2, 5, ClickableItem.of(noButton, e -> {
                            openEditItemMenu(itemId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&c&lConfirmar Eliminación"))
                .build();
    }

    /**
     * Menú de ayuda
     */
    public static SmartInventory openHelpMenu() {
        return SmartInventory.builder()
                .id("ci_help_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        List<String> commands = Arrays.asList(
                                "/ci create <id> - Crear un item",
                                "/ci delete <id> - Eliminar un item",
                                "/ci rename <texto> - Cambiar nombre",
                                "/ci addline <texto> - Agregar lore",
                                "/ci setline <línea> <texto> - Editar lore",
                                "/ci plus <id> <valor> <stat> - Bonus aditivo (+)",
                                "/ci less <id> <valor> <stat> - Bonus sustractivo (-)",
                                "/ci percentage <id> <valor> <stat> - Bonus multiplicativo (*)",
                                "/ci effect <tipo> <valor> - Agregar efecto"
                        );

                        int row = 1;
                        for (String cmd : commands) {
                            ItemStack cmdItem = new ItemStack(Material.PAPER);
                            ItemMeta cmdMeta = cmdItem.getItemMeta();
                            cmdMeta.setDisplayName(CC.translate("&c" + cmd.split(" ")[0]));
                            cmdMeta.setLore(Collections.singletonList(CC.translate("&7" + cmd)));
                            cmdItem.setItemMeta(cmdMeta);
                            contents.set(row, 1, ClickableItem.empty(cmdItem));
                            row++;
                        }

                        ItemStack statsItem = new ItemStack(Material.REDSTONE);
                        ItemMeta statsMeta = statsItem.getItemMeta();
                        statsMeta.setDisplayName(CC.translate("&a&lStats Disponibles"));
                        statsMeta.setLore(Arrays.asList(
                                CC.translate("&fstr &7- Fuerza"),
                                CC.translate("&fcon &7- Constitución"),
                                CC.translate("&fdex &7- Destreza"),
                                CC.translate("&fwill &7- Ataque de Ki"),
                                CC.translate("&fmnd &7- Mente")
                        ));
                        statsItem.setItemMeta(statsMeta);
                        contents.set(1, 7, ClickableItem.empty(statsItem));

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(5, 4, ClickableItem.of(backButton, e -> {
                            createMainMenu().open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(6, 9)
                .title(CC.translate("&c&lAyuda - Comandos /ci"))
                .build();
    }

    /**
     * Crea un panel de cristal decorativo
     */
    private static ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14); // Color rojo
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(CC.translate("&8"));
        glass.setItemMeta(glassMeta);
        return glass;
    }
}