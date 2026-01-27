package org.example.tools.inventory;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.commands.items.RegisterItem;
import org.example.tools.CC;
import org.example.tools.ci.CustomArmor;

import java.util.*;

/**
 * Menús completos para gestionar armaduras custom - CON EDICIÓN POR CHAT
 */
public class CustomArmorMenus {

    /**
     * Menú principal de armaduras custom
     */
    public static SmartInventory createMainMenu() {
        return SmartInventory.builder()
                .id("ca_menu_main")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        // Botón crear armadura
                        ItemStack createButton = new ItemStack(Material.IRON_CHESTPLATE);
                        ItemMeta createMeta = createButton.getItemMeta();
                        createMeta.setDisplayName(CC.translate("&a&lCrear Armadura"));
                        createMeta.setLore(Arrays.asList(
                                CC.translate("&7Crea una nueva armadura custom"),
                                CC.translate("&7Debes sostener la armadura en mano")
                        ));
                        createButton.setItemMeta(createMeta);

                        contents.set(1, 1, ClickableItem.of(createButton, e -> {
                            player.closeInventory();
                            ArmorCreationManager.startArmorCreation(player);
                        }));

                        // Botón listar armaduras
                        ItemStack listButton = new ItemStack(Material.BOOK);
                        ItemMeta listMeta = listButton.getItemMeta();
                        listMeta.setDisplayName(CC.translate("&b&lListar Armaduras"));
                        listMeta.setLore(Arrays.asList(
                                CC.translate("&7Ve todas las armaduras registradas"),
                                CC.translate("&7Y edítalas desde el menú")
                        ));
                        listButton.setItemMeta(listMeta);

                        contents.set(1, 4, ClickableItem.of(listButton, e -> {
                            openArmorListMenu(1).open(player);
                        }));

                        // Botón ayuda
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

                        // Botón cerrar
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
                .title(CC.translate("&b&lGestión de Armaduras Custom"))
                .build();
    }

    /**
     * Menú de listado de armaduras con paginación
     */
    public static SmartInventory openArmorListMenu(int page) {
        return SmartInventory.builder()
                .id("ca_list_menu_" + page)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        Set<String> armorIds = RegisterItem.items.keySet();
                        int pageSize = 21;
                        int totalPages = (int) Math.ceil((double) armorIds.size() / pageSize);

                        if (page < 1) return;
                        if (page > totalPages) return;

                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        List<String> ids = new ArrayList<>(armorIds);
                        int start = (page - 1) * pageSize;
                        int end = Math.min(start + pageSize, ids.size());

                        int row = 1;
                        int col = 1;

                        for (int i = start; i < end; i++) {
                            String id = ids.get(i);
                            CustomArmor armor = RegisterItem.items.get(id);

                            ItemStack displayItem = new ItemStack(armor.getMaterial());
                            ItemMeta meta = displayItem.getItemMeta();
                            meta.setDisplayName(CC.translate("&b" + id));

                            List<String> lore = new ArrayList<>();
                            lore.add(CC.translate("&7Nombre: &f" + armor.getDisplayName()));
                            lore.add(CC.translate("&7Stats: &f" + armor.getValueByStat().size()));
                            lore.add(CC.translate("&7Efectos: &f" + armor.getEffects().size()));
                            lore.add("");
                            lore.add(CC.translate("&a[CLICK PARA EDITAR]"));

                            meta.setLore(lore);
                            displayItem.setItemMeta(meta);

                            String finalId = id;
                            contents.set(row, col, ClickableItem.of(displayItem, e -> {
                                openEditArmorMenu(finalId).open(player);
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

                        // Navegación
                        if (page > 1) {
                            ItemStack prevButton = new ItemStack(Material.ARROW);
                            ItemMeta prevMeta = prevButton.getItemMeta();
                            prevMeta.setDisplayName(CC.translate("&b← Anterior"));
                            prevButton.setItemMeta(prevMeta);

                            contents.set(4, 2, ClickableItem.of(prevButton, e -> {
                                openArmorListMenu(page - 1).open(player);
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
                                openArmorListMenu(page + 1).open(player);
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
                .title(CC.translate("&b&lArmaduras Custom - Página " + page))
                .build();
    }

    /**
     * Menú de edición de armadura - COMPLETAMENTE ACTUALIZADO
     */
    public static SmartInventory openEditArmorMenu(String armorId) {
        CustomArmor armor = RegisterItem.items.get(armorId);
        if (armor == null) return null;

        return SmartInventory.builder()
                .id("ca_edit_" + armorId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        // Información de la armadura
                        ItemStack infoItem = new ItemStack(Material.PAPER);
                        ItemMeta infoMeta = infoItem.getItemMeta();
                        infoMeta.setDisplayName(CC.translate("&b&l" + armorId));
                        List<String> infoLore = new ArrayList<>();
                        infoLore.add(CC.translate("&7ID: &f" + armor.getId()));
                        infoLore.add(CC.translate("&7Nombre: &f" + armor.getDisplayName()));
                        infoLore.add(CC.translate("&7Material: &f" + armor.getMaterial()));
                        infoLore.add(CC.translate("&7Estado: &f" + (armor.isArmor() ? "Activa" : "Inactiva")));
                        infoItem.setItemMeta(infoMeta);
                        contents.set(1, 1, ClickableItem.empty(infoItem));

                        // Botón renombrar
                        ItemStack renameButton = new ItemStack(Material.NAME_TAG);
                        ItemMeta renameMeta = renameButton.getItemMeta();
                        renameMeta.setDisplayName(CC.translate("&e&lRenombrar"));
                        renameMeta.setLore(Arrays.asList(
                                CC.translate("&7Nombre actual: &f" + armor.getDisplayName()),
                                CC.translate("&a[CLICK PARA RENOMBRAR]")
                        ));
                        renameButton.setItemMeta(renameMeta);
                        contents.set(1, 2, ClickableItem.of(renameButton, e -> {
                            ArmorEditManager.startArmorEdit(player, armorId, "rename");
                        }));

                        // Botón editar lore
                        ItemStack loreButton = new ItemStack(Material.BOOK_AND_QUILL);
                        ItemMeta loreMeta = loreButton.getItemMeta();
                        loreMeta.setDisplayName(CC.translate("&e&lEditar Lore"));
                        loreMeta.setLore(Arrays.asList(
                                CC.translate("&7Líneas: &f" + (armor.getLore() != null ? armor.getLore().size() : 0)),
                                CC.translate("&a[CLICK PARA AGREGAR LÍNEA]")
                        ));
                        loreButton.setItemMeta(loreMeta);
                        contents.set(1, 3, ClickableItem.of(loreButton, e -> {
                            ArmorEditManager.startArmorEdit(player, armorId, "addline");
                        }));

                        // Botón editar stats
                        ItemStack statsButton = new ItemStack(Material.REDSTONE);
                        ItemMeta statsMeta = statsButton.getItemMeta();
                        statsMeta.setDisplayName(CC.translate("&a&lAñadir Stats"));
                        List<String> statsLore = new ArrayList<>();
                        statsLore.add(CC.translate("&7Stats actuales: &f" + armor.getValueByStat().size()));
                        armor.getValueByStat().forEach((stat, value) -> {
                            String op = armor.getOperation().getOrDefault(stat, "+");
                            statsLore.add(CC.translate("&f  " + stat + " " + op + value));
                        });
                        statsLore.add("");
                        statsLore.add(CC.translate("&a[CLICK PARA AGREGAR]"));
                        statsMeta.setLore(statsLore);
                        statsButton.setItemMeta(statsMeta);
                        contents.set(1, 4, ClickableItem.of(statsButton, e -> {
                            BonusFlowManager.startBonusFlow(player, armorId);
                            CustomArmorBonusMenus.createStatSelectionMenu(armorId).open(player);
                        }));

                        // Botón ver efectos
                        ItemStack effectsButton = new ItemStack(Material.REDSTONE_TORCH_ON);
                        ItemMeta effectsMeta = effectsButton.getItemMeta();
                        effectsMeta.setDisplayName(CC.translate("&6&lVer Efectos"));
                        List<String> effectsLore = new ArrayList<>();
                        effectsLore.add(CC.translate("&7Efectos activos:"));
                        armor.getEffects().forEach((effect, value) -> {
                            effectsLore.add(CC.translate("&f  " + effect + ": " + (value * 100) + "%"));
                        });
                        effectsMeta.setLore(effectsLore);
                        effectsButton.setItemMeta(effectsMeta);
                        contents.set(1, 5, ClickableItem.of(effectsButton, e -> {
                            openEffectsMenu(armorId).open(player);
                        }));

                        // ⭐ NUEVO: Botón dar armadura
                        ItemStack giveButton = new ItemStack(Material.APPLE);
                        ItemMeta giveMeta = giveButton.getItemMeta();
                        giveMeta.setDisplayName(CC.translate("&a&lDar Armadura"));
                        giveMeta.setLore(Arrays.asList(
                                CC.translate("&7Recibe la armadura en tu inventario"),
                                CC.translate("&a[CLICK PARA DAR]")
                        ));
                        giveButton.setItemMeta(giveMeta);
                        contents.set(1, 6, ClickableItem.of(giveButton, e -> {
                            ArmorEditManager.giveCustomArmor(player, armorId);
                        }));

                        // Botón eliminar
                        ItemStack deleteButton = new ItemStack(Material.ANVIL);
                        ItemMeta deleteMeta = deleteButton.getItemMeta();
                        deleteMeta.setDisplayName(CC.translate("&c&lEliminar Armadura"));
                        deleteMeta.setLore(Arrays.asList(
                                CC.translate("&7Esta armadura será eliminada"),
                                CC.translate("&a[CLICK PARA CONFIRMAR]")
                        ));
                        deleteButton.setItemMeta(deleteMeta);
                        contents.set(1, 7, ClickableItem.of(deleteButton, e -> {
                            openDeleteConfirmMenu(armorId).open(player);
                        }));

                        // Botón atrás
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 4, ClickableItem.of(backButton, e -> {
                            openArmorListMenu(1).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&b&lEditar Armadura: " + armorId))
                .build();
    }

    /**
     * Menú de visualización de efectos
     */
    public static SmartInventory openEffectsMenu(String armorId) {
        CustomArmor armor = RegisterItem.items.get(armorId);
        if (armor == null) return null;

        return SmartInventory.builder()
                .id("ca_effects_" + armorId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        int row = 1;
                        for (Map.Entry<String, Double> entry : armor.getEffects().entrySet()) {
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
                            openEditArmorMenu(armorId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(6, 9)
                .title(CC.translate("&b&lEfectos: " + armorId))
                .build();
    }

    /**
     * Menú de confirmación de eliminación
     */
    public static SmartInventory openDeleteConfirmMenu(String armorId) {
        return SmartInventory.builder()
                .id("ca_delete_confirm_" + armorId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        ItemStack confirmItem = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta confirmMeta = confirmItem.getItemMeta();
                        confirmMeta.setDisplayName(CC.translate("&c&l¿Estás seguro?"));
                        confirmMeta.setLore(Arrays.asList(
                                CC.translate("&7Esta acción no puede deshacerse"),
                                CC.translate("&7La armadura &f" + armorId + "&7 será eliminada permanentemente")
                        ));
                        confirmItem.setItemMeta(confirmMeta);
                        contents.set(1, 4, ClickableItem.empty(confirmItem));

                        ItemStack yesButton = new ItemStack(Material.EMERALD_BLOCK);
                        ItemMeta yesMeta = yesButton.getItemMeta();
                        yesMeta.setDisplayName(CC.translate("&a&lSÍ, ELIMINAR"));
                        yesButton.setItemMeta(yesMeta);
                        contents.set(2, 3, ClickableItem.of(yesButton, e -> {
                            RegisterItem.items.remove(armorId);
                            player.sendMessage(CC.translate("&aArmadura eliminada correctamente"));
                            openArmorListMenu(1).open(player);
                        }));

                        ItemStack noButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta noMeta = noButton.getItemMeta();
                        noMeta.setDisplayName(CC.translate("&c&lCANCELAR"));
                        noButton.setItemMeta(noMeta);
                        contents.set(2, 5, ClickableItem.of(noButton, e -> {
                            openEditArmorMenu(armorId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&b&lConfirmar Eliminación"))
                .build();
    }

    /**
     * Menú de ayuda
     */
    public static SmartInventory openHelpMenu() {
        return SmartInventory.builder()
                .id("ca_help_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        List<String> commands = Arrays.asList(
                                "/ca create <id> - Crear una armadura",
                                "/ca delete <id> - Eliminar una armadura",
                                "/ca plus <id> <valor> <stat> - Bonus aditivo (+)",
                                "/ca less <id> <valor> <stat> - Bonus sustractivo (-)",
                                "/ca percentage <id> <valor> <stat> - Bonus multiplicativo (*)",
                                "/ca effect <tipo> <valor> - Agregar efecto"
                        );

                        int row = 1;
                        for (String cmd : commands) {
                            ItemStack cmdItem = new ItemStack(Material.PAPER);
                            ItemMeta cmdMeta = cmdItem.getItemMeta();
                            cmdMeta.setDisplayName(CC.translate("&b" + cmd.split(" ")[0]));
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
                                CC.translate("&fwill &7- Voluntad"),
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
                .title(CC.translate("&b&lAyuda - Comandos /ca"))
                .build();
    }

    /**
     * Crea un panel de cristal decorativo
     */
    private static ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(CC.translate("&8"));
        glass.setItemMeta(glassMeta);
        return glass;
    }
}