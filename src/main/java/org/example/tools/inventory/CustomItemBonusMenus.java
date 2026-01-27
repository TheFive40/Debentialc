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

import java.util.Arrays;
import java.util.List;

/**
 * Menús para agregar bonificaciones a items custom
 */
public class CustomItemBonusMenus {

    /**
     * Menú para seleccionar el atributo (stat)
     */
    public static SmartInventory createStatSelectionMenu(String itemId) {
        CustomItem item = CustomItemCommand.items.get(itemId);
        if (item == null) return null;

        return SmartInventory.builder()
                .id("ci_bonus_stat_select_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        // Título
                        ItemStack titleItem = new ItemStack(Material.PAPER);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&c&lSelecciona un Atributo"));
                        titleMeta.setLore(Arrays.asList(
                                CC.translate("&7Item: &f" + itemId),
                                CC.translate("&7Elige sobre cuál stat aplicar el bonus")
                        ));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        // Stats disponibles
                        String[] stats = {"str", "con", "dex", "will", "mnd"};
                        String[] statNames = {"Fuerza", "Constitución", "Destreza", "Ataque de ki", "Mente"};
                        Material[] statMaterials = {
                                Material.DIAMOND_SWORD,
                                Material.IRON_CHESTPLATE,
                                Material.LEATHER_BOOTS,
                                Material.WOOD_PICKAXE,
                                Material.BOOK
                        };

                        int row = 1;
                        int col = 2;

                        for (int i = 0; i < stats.length; i++) {
                            final String stat = stats[i];
                            ItemStack statButton = new ItemStack(statMaterials[i]);
                            ItemMeta statMeta = statButton.getItemMeta();
                            statMeta.setDisplayName(CC.translate("&a&l" + statNames[i]));
                            statMeta.setLore(Arrays.asList(
                                    CC.translate("&7Stat: &f" + stat.toUpperCase()),
                                    CC.translate("&a[CLICK PARA SELECCIONAR]")
                            ));
                            statButton.setItemMeta(statMeta);

                            contents.set(row, col, ClickableItem.of(statButton, e -> {
                                BonusFlowManager.setSelectedStat(player, stat);
                                BonusFlowManager.nextStep(player);
                                createOperationSelectionMenu(itemId).open(player);
                            }));

                            col += 2;
                            if (col > 7) {
                                col = 2;
                                row++;
                            }
                        }

                        // Botón atrás
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 0, ClickableItem.of(backButton, e -> {
                            BonusFlowManager.clearFlowState(player);
                            CustomItemMenus.openEditItemMenu(itemId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&c&lSeleccionar Stat - " + itemId))
                .build();
    }

    /**
     * Menú para seleccionar la operación (+ - * /)
     */
    public static SmartInventory createOperationSelectionMenu(String itemId) {
        CustomItem item = CustomItemCommand.items.get(itemId);
        if (item == null) return null;

        BonusFlowManager.BonusFlowState state = BonusFlowManager.getFlowState(null); // Será obtenido del player
        return SmartInventory.builder()
                .id("ci_bonus_op_select_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        BonusFlowManager.BonusFlowState playerState = BonusFlowManager.getFlowState(player);

                        // Título
                        ItemStack titleItem = new ItemStack(Material.PAPER);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&c&lSelecciona una Operación"));
                        titleMeta.setLore(Arrays.asList(
                                CC.translate("&7Item: &f" + itemId),
                                CC.translate("&7Stat: &f" + playerState.selectedStat.toUpperCase()),
                                CC.translate("&7Elige cómo aplicar el bonus")
                        ));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        // Operaciones disponibles
                        String[] operations = {"+", "-", "*"};
                        String[] operationNames = {"Aditivo", "Sustractivo", "Multiplicativo"};
                        String[] descriptions = {
                                "Suma directa al stat",
                                "Resta directa al stat",
                                "Porcentaje multiplicador"
                        };
                        Material[] operationMaterials = {
                                Material.EMERALD_BLOCK,
                                Material.REDSTONE_BLOCK,
                                Material.GOLD_BLOCK
                        };

                        int row = 2;
                        int col = 1;

                        for (int i = 0; i < operations.length; i++) {
                            final String operation = operations[i];
                            ItemStack opButton = new ItemStack(operationMaterials[i]);
                            ItemMeta opMeta = opButton.getItemMeta();
                            opMeta.setDisplayName(CC.translate("&a&l" + operationNames[i] + " (" + operation + ")"));
                            opMeta.setLore(Arrays.asList(
                                    CC.translate("&7" + descriptions[i]),
                                    CC.translate("&a[CLICK PARA SELECCIONAR]")
                            ));
                            opButton.setItemMeta(opMeta);

                            contents.set(row, col, ClickableItem.of(opButton, e -> {
                                BonusFlowManager.setSelectedOperation(player, operation);
                                BonusFlowManager.nextStep(player);
                                createValueSelectionMenu(itemId).open(player);
                            }));

                            col += 3;
                        }

                        // Botón atrás
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 0, ClickableItem.of(backButton, e -> {
                            BonusFlowManager.setSelectedStat(player, null);
                            BonusFlowManager.getFlowState(player).step = "selecting_stat";
                            createStatSelectionMenu(itemId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&c&lSeleccionar Operación - " + itemId))
                .build();
    }

    /**
     * Menú para seleccionar el valor del bonus
     */
    public static SmartInventory createValueSelectionMenu(String itemId) {
        CustomItem item = CustomItemCommand.items.get(itemId);
        if (item == null) return null;

        return SmartInventory.builder()
                .id("ci_bonus_value_select_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        BonusFlowManager.BonusFlowState playerState = BonusFlowManager.getFlowState(player);

                        // Título
                        ItemStack titleItem = new ItemStack(Material.PAPER);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&c&lSelecciona un Valor"));
                        titleMeta.setLore(Arrays.asList(
                                CC.translate("&7Item: &f" + itemId),
                                CC.translate("&7Stat: &f" + playerState.selectedStat.toUpperCase()),
                                CC.translate("&7Operación: &f" + playerState.selectedOperation)
                        ));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        // Valores predeterminados
                        double[] values = {1, 2, 5, 10, 15, 20, 25, 50, 100};
                        int row = 1;
                        int col = 1;

                        for (double value : values) {
                            final double finalValue = value;
                            ItemStack valueButton = new ItemStack(Material.REDSTONE);
                            ItemMeta valueMeta = valueButton.getItemMeta();
                            valueMeta.setDisplayName(CC.translate("&a&l" + value));
                            valueMeta.setLore(Arrays.asList(
                                    CC.translate("&7Bonificación: &f" + playerState.selectedOperation + value),
                                    CC.translate("&a[CLICK PARA APLICAR]")
                            ));
                            valueButton.setItemMeta(valueMeta);

                            contents.set(row, col, ClickableItem.of(valueButton, e -> {
                                applyBonus(player, itemId, playerState.selectedStat,
                                        playerState.selectedOperation, finalValue);
                                BonusFlowManager.clearFlowState(player);
                                CustomItemMenus.openEditItemMenu(itemId).open(player);
                            }));

                            col++;
                            if (col > 7) {
                                col = 1;
                                row++;
                            }
                        }

                        // Instrucción para valor personalizado
                        ItemStack customValueItem = new ItemStack(Material.NAME_TAG);
                        ItemMeta customValueMeta = customValueItem.getItemMeta();
                        customValueMeta.setDisplayName(CC.translate("&e&lValor Personalizado"));
                        customValueMeta.setLore(Arrays.asList(
                                CC.translate("&7Escribe en el chat el valor deseado"),
                                CC.translate("&a/ci value <número>")
                        ));
                        customValueItem.setItemMeta(customValueMeta);
                        contents.set(3, 4, ClickableItem.of(customValueItem, e -> {
                            player.closeInventory();
                            player.sendMessage(CC.translate("&bEscribe el valor: &a/ci value <número>"));
                        }));

                        // Botón atrás
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 0, ClickableItem.of(backButton, e -> {
                            BonusFlowManager.setSelectedOperation(player, null);
                            BonusFlowManager.getFlowState(player).step = "selecting_operation";
                            createOperationSelectionMenu(itemId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&c&lSeleccionar Valor - " + itemId))
                .build();
    }

    /**
     * Aplica el bonus al item
     */
    private static void applyBonus(Player player, String itemId, String stat,
                                   String operation, double value) {
        if (!CustomItemCommand.items.containsKey(itemId)) {
            player.sendMessage(CC.translate("&cItem no encontrado"));
            return;
        }

        CustomItem item = CustomItemCommand.items.get(itemId);
        item.setOperation(operation, stat).setBonusStat(stat, value);

        // Guardar el item actualizado
        org.example.tools.storage.CustomItemStorage storage =
                new org.example.tools.storage.CustomItemStorage();
        storage.saveItem(item);

        player.sendMessage(CC.translate("&a✓ Bonificación aplicada correctamente"));
        player.sendMessage(CC.translate("&7Stat: &f" + stat + " " + operation + value));
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