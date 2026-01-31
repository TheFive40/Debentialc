package org.debentialc.customitems.tools.inventory;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.customitems.commands.CustomItemCommand;
import org.debentialc.customitems.tools.CC;
import org.debentialc.customitems.tools.ci.CustomItem;
import org.debentialc.customitems.tools.scripts.ScriptInputManager;
import org.debentialc.customitems.tools.scripts.ScriptManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Menú de gestión de scripts para items custom
 */
public class ScriptManagementMenu {

    /**
     * Menú principal de scripts para un item
     */
    public static SmartInventory createScriptMenu(String itemId) {
        CustomItem item = CustomItemCommand.items.get(itemId);
        if (item == null) return null;

        return SmartInventory.builder()
                .id("script_menu_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        boolean hasScript = ScriptManager.getInstance().hasScript(itemId);

                        // TÍTULO
                        ItemStack titleItem = new ItemStack(Material.WRITTEN_BOOK);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&5&lGestión de Scripts"));
                        titleMeta.setLore(Arrays.asList(
                                CC.translate("&7Item: &f" + itemId),
                                CC.translate("&7Estado: " + (hasScript ? "&a✓ Con script" : "&c✗ Sin script"))
                        ));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        if (hasScript) {
                            // INFORMACIÓN DEL SCRIPT
                            ScriptManager.ScriptMetadata metadata = ScriptManager.getInstance().getMetadata(itemId);

                            ItemStack infoItem = new ItemStack(Material.PAPER);
                            ItemMeta infoMeta = infoItem.getItemMeta();
                            infoMeta.setDisplayName(CC.translate("&b&lInformación del Script"));
                            List<String> infoLore = new ArrayList<>();
                            infoLore.add(CC.translate("&7Archivo: &f" + itemId + ".js"));

                            if (metadata != null) {
                                infoLore.add(CC.translate("&7URL: &f" + truncateUrl(metadata.getSourceUrl())));

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                String date = sdf.format(new Date(metadata.getDownloadDate()));
                                infoLore.add(CC.translate("&7Descargado: &f" + date));
                            }

                            infoLore.add("");
                            infoLore.add(CC.translate("&7El script se ejecuta al usar el item"));
                            infoMeta.setLore(infoLore);
                            infoItem.setItemMeta(infoMeta);
                            contents.set(2, 1, ClickableItem.empty(infoItem));

                            // ACTUALIZAR SCRIPT
                            ItemStack updateButton = new ItemStack(Material.ANVIL);
                            ItemMeta updateMeta = updateButton.getItemMeta();
                            updateMeta.setDisplayName(CC.translate("&e&lActualizar Script"));
                            updateMeta.setLore(Arrays.asList(
                                    CC.translate("&7Descarga una nueva versión"),
                                    CC.translate("&7desde la misma URL o una nueva"),
                                    "",
                                    CC.translate("&a[CLICK PARA ACTUALIZAR]")
                            ));
                            updateButton.setItemMeta(updateMeta);
                            contents.set(2, 3, ClickableItem.of(updateButton, e -> {
                                player.closeInventory();
                                ScriptInputManager.startScriptInput(player, itemId, "update");
                            }));

                            // RECARGAR SCRIPT
                            ItemStack reloadButton = new ItemStack(Material.EMERALD);
                            ItemMeta reloadMeta = reloadButton.getItemMeta();
                            reloadMeta.setDisplayName(CC.translate("&a&lRecargar Script"));
                            reloadMeta.setLore(Arrays.asList(
                                    CC.translate("&7Recarga el script desde el archivo"),
                                    CC.translate("&7Útil si editaste el .js manualmente"),
                                    "",
                                    CC.translate("&a[CLICK PARA RECARGAR]")
                            ));
                            reloadButton.setItemMeta(reloadMeta);
                            contents.set(2, 5, ClickableItem.of(reloadButton, e -> {
                                boolean success = ScriptManager.getInstance().reloadScript(itemId);

                                if (success) {
                                    player.sendMessage("");
                                    player.sendMessage(CC.translate("&a✓ Script recargado"));
                                    player.sendMessage("");
                                } else {
                                    player.sendMessage("");
                                    player.sendMessage(CC.translate("&c✗ Error al recargar"));
                                    player.sendMessage("");
                                }

                                createScriptMenu(itemId).open(player);
                            }));

                            // ELIMINAR SCRIPT
                            ItemStack deleteButton = new ItemStack(Material.REDSTONE_BLOCK);
                            ItemMeta deleteMeta = deleteButton.getItemMeta();
                            deleteMeta.setDisplayName(CC.translate("&c&lEliminar Script"));
                            deleteMeta.setLore(Arrays.asList(
                                    CC.translate("&7Elimina el script asociado"),
                                    CC.translate("&7El archivo .js será borrado"),
                                    "",
                                    CC.translate("&c[CLICK PARA ELIMINAR]")
                            ));
                            deleteButton.setItemMeta(deleteMeta);
                            contents.set(2, 7, ClickableItem.of(deleteButton, e -> {
                                createDeleteConfirmMenu(itemId).open(player);
                            }));

                        } else {
                            // NO TIENE SCRIPT - OPCIÓN PARA AGREGAR
                            ItemStack addButton = new ItemStack(Material.EMERALD_BLOCK);
                            ItemMeta addMeta = addButton.getItemMeta();
                            addMeta.setDisplayName(CC.translate("&a&lAgregar Script"));
                            addMeta.setLore(Arrays.asList(
                                    CC.translate("&7Descarga un script desde una URL"),
                                    "",
                                    CC.translate("&7Soportado:"),
                                    CC.translate("&f  • GitHub (raw)"),
                                    CC.translate("&f  • Pastebin (raw)"),
                                    "",
                                    CC.translate("&a[CLICK PARA AGREGAR]")
                            ));
                            addButton.setItemMeta(addMeta);
                            contents.set(2, 4, ClickableItem.of(addButton, e -> {
                                player.closeInventory();
                                ScriptInputManager.startScriptInput(player, itemId, "add");
                            }));
                        }

                        // BOTÓN ATRÁS
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(4, 4, ClickableItem.of(backButton, e -> {
                            CustomItemAdvancedOptionsMenu.createAdvancedOptionsMenu(itemId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(5, 9)
                .title(CC.translate("&5&lScripts - " + itemId))
                .build();
    }

    /**
     * Menú de confirmación para eliminar script
     */
    public static SmartInventory createDeleteConfirmMenu(String itemId) {
        return SmartInventory.builder()
                .id("script_delete_confirm_" + itemId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(createGlassPane()));

                        ItemStack confirmItem = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta confirmMeta = confirmItem.getItemMeta();
                        confirmMeta.setDisplayName(CC.translate("&c&l¿Eliminar Script?"));
                        confirmMeta.setLore(Arrays.asList(
                                CC.translate("&7Esta acción no puede deshacerse"),
                                CC.translate("&7El archivo &f" + itemId + ".js &7será eliminado"),
                                CC.translate("&7permanentemente")
                        ));
                        confirmItem.setItemMeta(confirmMeta);
                        contents.set(1, 4, ClickableItem.empty(confirmItem));

                        ItemStack yesButton = new ItemStack(Material.EMERALD_BLOCK);
                        ItemMeta yesMeta = yesButton.getItemMeta();
                        yesMeta.setDisplayName(CC.translate("&a&lSÍ, ELIMINAR"));
                        yesButton.setItemMeta(yesMeta);
                        contents.set(2, 3, ClickableItem.of(yesButton, e -> {
                            boolean success = ScriptManager.getInstance().deleteScript(itemId);

                            if (success) {
                                player.sendMessage("");
                                player.sendMessage(CC.translate("&a✓ Script eliminado"));
                                player.sendMessage("");
                            } else {
                                player.sendMessage("");
                                player.sendMessage(CC.translate("&c✗ Error al eliminar"));
                                player.sendMessage("");
                            }

                            createScriptMenu(itemId).open(player);
                        }));

                        ItemStack noButton = new ItemStack(Material.REDSTONE_BLOCK);
                        ItemMeta noMeta = noButton.getItemMeta();
                        noMeta.setDisplayName(CC.translate("&c&lCANCELAR"));
                        noButton.setItemMeta(noMeta);
                        contents.set(2, 5, ClickableItem.of(noButton, e -> {
                            createScriptMenu(itemId).open(player);
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

    private static ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(CC.translate("&8"));
        glass.setItemMeta(glassMeta);
        return glass;
    }

    private static String truncateUrl(String url) {
        if (url.length() > 40) {
            return url.substring(0, 37) + "...";
        }
        return url;
    }
}