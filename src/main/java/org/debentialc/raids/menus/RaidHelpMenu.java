package org.debentialc.raids.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.service.CC;

import java.util.Arrays;
import java.util.Collections;

/**
 * Menú visual de ayuda del sistema de raids
 */
public class RaidHelpMenu {

    public static SmartInventory createHelpMenu() {
        return SmartInventory.builder()
                .id("raid_help")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(RaidMainMenu.createGlassPane((short) 3)));

                        // TÍTULO
                        ItemStack titleItem = new ItemStack(Material.PAPER);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&b&lAyuda - Sistema de Raids"));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        // COMANDOS ADMIN
                        ItemStack adminCmds = new ItemStack(Material.COMMAND);
                        ItemMeta adminMeta = adminCmds.getItemMeta();
                        adminMeta.setDisplayName(CC.translate("&6&lComandos Admin"));
                        adminMeta.setLore(Arrays.asList(
                                CC.translate("&f/raid &7- Menú principal"),
                                CC.translate("&f/raid create &7- Crear raid"),
                                CC.translate("&f/raid list &7- Ver raids"),
                                CC.translate("&f/raid info &7- Información"),
                                CC.translate("&f/raid reload &7- Recargar"),
                                CC.translate("&f/raid clear &7- Cerrar menú")
                        ));
                        adminCmds.setItemMeta(adminMeta);
                        contents.set(1, 2, ClickableItem.empty(adminCmds));

                        // COMANDOS PARTY
                        ItemStack partyCmds = new ItemStack(Material.SKULL_ITEM);
                        ItemMeta partyMeta = partyCmds.getItemMeta();
                        partyMeta.setDisplayName(CC.translate("&e&lComandos Party"));
                        partyMeta.setLore(Arrays.asList(
                                CC.translate("&f/party create &7- Crear party"),
                                CC.translate("&f/party invite <jugador> &7- Invitar"),
                                CC.translate("&f/party leave &7- Salir"),
                                CC.translate("&f/party start <raid> &7- Iniciar raid"),
                                CC.translate("&f/party info &7- Ver info"),
                                CC.translate("&f/party members &7- Ver miembros"),
                                CC.translate("&f/party disband &7- Disolver")
                        ));
                        partyCmds.setItemMeta(partyMeta);
                        contents.set(1, 4, ClickableItem.empty(partyCmds));

                        // FLUJO DE JUEGO
                        ItemStack flowInfo = new ItemStack(Material.DIAMOND_SWORD);
                        ItemMeta flowMeta = flowInfo.getItemMeta();
                        flowMeta.setDisplayName(CC.translate("&c&lFlujo de Juego"));
                        flowMeta.setLore(Arrays.asList(
                                CC.translate("&f1. &7Crear party (/party create)"),
                                CC.translate("&f2. &7Invitar jugadores"),
                                CC.translate("&f3. &7Iniciar raid (/party start)"),
                                CC.translate("&f4. &7Completar oleadas"),
                                CC.translate("&f5. &7Recibir recompensas"),
                                "",
                                CC.translate("&7Mínimo 2 jugadores para iniciar")
                        ));
                        flowInfo.setItemMeta(flowMeta);
                        contents.set(1, 6, ClickableItem.empty(flowInfo));

                        // VARIABLES
                        ItemStack varsInfo = new ItemStack(Material.BOOK);
                        ItemMeta varsMeta = varsInfo.getItemMeta();
                        varsMeta.setDisplayName(CC.translate("&d&lVariables"));
                        varsMeta.setLore(Arrays.asList(
                                CC.translate("&7En comandos de recompensa:"),
                                CC.translate("&f@p &7- Jugadores de la raid"),
                                CC.translate("&f@a &7- Todos los jugadores"),
                                CC.translate("&f{player} &7- Nombre del jugador")
                        ));
                        varsInfo.setItemMeta(varsMeta);
                        contents.set(2, 4, ClickableItem.empty(varsInfo));

                        // PERMISOS
                        ItemStack permsInfo = new ItemStack(Material.IRON_DOOR);
                        ItemMeta permsMeta = permsInfo.getItemMeta();
                        permsMeta.setDisplayName(CC.translate("&3&lPermisos"));
                        permsMeta.setLore(Arrays.asList(
                                CC.translate("&fdbcplugin.raids.admin"),
                                CC.translate("&7Para acceder a /raid")
                        ));
                        permsInfo.setItemMeta(permsMeta);
                        contents.set(2, 6, ClickableItem.empty(permsInfo));

                        // ATRÁS
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 4, ClickableItem.of(backButton, e -> {
                            RaidMainMenu.createMainMenu().open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&b&lAyuda - Raids"))
                .build();
    }
}