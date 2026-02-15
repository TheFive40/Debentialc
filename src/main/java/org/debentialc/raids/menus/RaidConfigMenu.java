package org.debentialc.raids.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.raids.managers.RaidManager;
import org.debentialc.raids.managers.RaidStorageManager;
import org.debentialc.raids.models.Raid;
import org.debentialc.service.CC;

import java.util.*;

/**
 * Menú visual de configuración de una raid individual
 */
public class RaidConfigMenu {

    public static SmartInventory createRaidConfigMenu(String raidId) {
        Raid raid = RaidManager.getRaidById(raidId);
        if (raid == null) return null;

        return SmartInventory.builder()
                .id("raid_config_" + raidId)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(RaidMainMenu.createGlassPane((short) 1)));

                        // INFO DE LA RAID
                        ItemStack infoItem = new ItemStack(Material.PAPER);
                        ItemMeta infoMeta = infoItem.getItemMeta();
                        infoMeta.setDisplayName(CC.translate("&6&l" + raid.getRaidName()));
                        List<String> infoLore = new ArrayList<>();
                        infoLore.add(CC.translate("&7ID: &f" + raid.getRaidId()));
                        infoLore.add(CC.translate("&7Descripción: &f" + (raid.getDescription() != null ? raid.getDescription() : "N/A")));
                        infoLore.add(CC.translate("&7Oleadas: &f" + raid.getTotalWaves()));
                        infoLore.add(CC.translate("&7Jugadores: &f" + raid.getMinPlayers() + "-" + raid.getMaxPlayers()));
                        infoLore.add(CC.translate("&7Cooldown: &f" + (raid.getCooldownSeconds() / 60) + " min"));
                        infoLore.add(CC.translate("&7Arena: " + (raid.getArenaSpawnPoint() != null ? "&a✓ Configurada" : "&c✗ Sin configurar")));
                        infoLore.add(CC.translate("&7Spawn: " + (raid.getPlayerSpawnPoint() != null ? "&a✓ Configurado" : "&c✗ Sin configurar")));
                        infoLore.add(CC.translate("&7Estado: " + (raid.isEnabled() ? "&a✓ Habilitada" : "&c✗ Deshabilitada")));
                        infoMeta.setLore(infoLore);
                        infoItem.setItemMeta(infoMeta);
                        contents.set(1, 1, ClickableItem.empty(infoItem));

                        // RENOMBRAR
                        ItemStack renameButton = new ItemStack(Material.NAME_TAG);
                        ItemMeta renameMeta = renameButton.getItemMeta();
                        renameMeta.setDisplayName(CC.translate("&e&lRenombrar"));
                        renameMeta.setLore(Arrays.asList(
                                CC.translate("&7Nombre actual:"),
                                CC.translate("&f" + raid.getRaidName()),
                                "",
                                CC.translate("&a[CLICK PARA RENOMBRAR]")
                        ));
                        renameButton.setItemMeta(renameMeta);
                        contents.set(1, 2, ClickableItem.of(renameButton, e -> {
                            player.closeInventory();
                            RaidChatInputManager.startRaidRenameInput(player, raidId);
                        }));

                        // EDITAR DESCRIPCIÓN
                        ItemStack descButton = new ItemStack(Material.BOOK_AND_QUILL);
                        ItemMeta descMeta = descButton.getItemMeta();
                        descMeta.setDisplayName(CC.translate("&b&lDescripción"));
                        descMeta.setLore(Arrays.asList(
                                CC.translate("&7Actual: &f" + (raid.getDescription() != null ? raid.getDescription() : "Sin descripción")),
                                "",
                                CC.translate("&a[CLICK PARA EDITAR]")
                        ));
                        descButton.setItemMeta(descMeta);
                        contents.set(1, 3, ClickableItem.of(descButton, e -> {
                            player.closeInventory();
                            RaidChatInputManager.startRaidDescriptionInput(player, raidId);
                        }));

                        // GESTIONAR OLEADAS
                        ItemStack wavesButton = new ItemStack(Material.BLAZE_POWDER);
                        ItemMeta wavesMeta = wavesButton.getItemMeta();
                        wavesMeta.setDisplayName(CC.translate("&6&lOleadas"));
                        List<String> wavesLore = new ArrayList<>();
                        wavesLore.add(CC.translate("&7Oleadas configuradas: &f" + raid.getTotalWaves()));
                        if (raid.getTotalWaves() > 0) {
                            int totalEnemies = 0;
                            for (int i = 0; i < raid.getTotalWaves(); i++) {
                                totalEnemies += raid.getWaveByIndex(i).getTotalEnemies();
                            }
                            wavesLore.add(CC.translate("&7Enemigos totales: &f" + totalEnemies));
                        }
                        wavesLore.add("");
                        wavesLore.add(CC.translate("&6[CLICK PARA GESTIONAR]"));
                        wavesMeta.setLore(wavesLore);
                        wavesButton.setItemMeta(wavesMeta);
                        contents.set(1, 4, ClickableItem.of(wavesButton, e -> {
                            RaidWavesMenu.createWavesMenu(raidId).open(player);
                        }));

                        // CONFIGURAR ARENA SPAWN
                        ItemStack arenaButton = new ItemStack(Material.IRON_FENCE);
                        ItemMeta arenaMeta = arenaButton.getItemMeta();
                        arenaMeta.setDisplayName(CC.translate("&d&lArena Spawn"));
                        arenaMeta.setLore(Arrays.asList(
                                CC.translate("&7Punto donde aparecen los NPCs"),
                                CC.translate("&7Estado: " + (raid.getArenaSpawnPoint() != null ? "&a✓ Configurado" : "&c✗ Sin configurar")),
                                "",
                                CC.translate("&7Se usará tu posición actual"),
                                "",
                                CC.translate("&d[CLICK PARA ESTABLECER]")
                        ));
                        arenaButton.setItemMeta(arenaMeta);
                        contents.set(1, 5, ClickableItem.of(arenaButton, e -> {
                            raid.setArenaSpawnPoint(player.getLocation().clone());
                            RaidManager.updateRaid(raid);
                            RaidStorageManager.saveRaid(raid);
                            player.sendMessage(CC.translate("&a✓ Arena spawn establecido en tu posición"));
                            createRaidConfigMenu(raidId).open(player);
                        }));

                        // CONFIGURAR PLAYER SPAWN
                        ItemStack playerSpawnButton = new ItemStack(Material.BED);
                        ItemMeta playerSpawnMeta = playerSpawnButton.getItemMeta();
                        playerSpawnMeta.setDisplayName(CC.translate("&a&lPlayer Spawn"));
                        playerSpawnMeta.setLore(Arrays.asList(
                                CC.translate("&7Punto donde aparecen los jugadores"),
                                CC.translate("&7Estado: " + (raid.getPlayerSpawnPoint() != null ? "&a✓ Configurado" : "&c✗ Sin configurar")),
                                "",
                                CC.translate("&7Se usará tu posición actual"),
                                "",
                                CC.translate("&a[CLICK PARA ESTABLECER]")
                        ));
                        playerSpawnButton.setItemMeta(playerSpawnMeta);
                        contents.set(1, 6, ClickableItem.of(playerSpawnButton, e -> {
                            raid.setPlayerSpawnPoint(player.getLocation().clone());
                            RaidManager.updateRaid(raid);
                            RaidStorageManager.saveRaid(raid);
                            player.sendMessage(CC.translate("&a✓ Player spawn establecido en tu posición"));
                            createRaidConfigMenu(raidId).open(player);
                        }));

                        // COOLDOWN
                        ItemStack cooldownButton = new ItemStack(Material.WATCH);
                        ItemMeta cooldownMeta = cooldownButton.getItemMeta();
                        cooldownMeta.setDisplayName(CC.translate("&3&lCooldown"));
                        cooldownMeta.setLore(Arrays.asList(
                                CC.translate("&7Actual: &f" + (raid.getCooldownSeconds() / 60) + " minutos"),
                                "",
                                CC.translate("&3[CLICK PARA CAMBIAR]")
                        ));
                        cooldownButton.setItemMeta(cooldownMeta);
                        contents.set(2, 2, ClickableItem.of(cooldownButton, e -> {
                            player.closeInventory();
                            RaidChatInputManager.startCooldownInput(player, raidId);
                        }));

                        // JUGADORES MIN/MAX
                        ItemStack playersButton = new ItemStack(Material.SKULL_ITEM);
                        ItemMeta playersMeta = playersButton.getItemMeta();
                        playersMeta.setDisplayName(CC.translate("&b&lJugadores"));
                        playersMeta.setLore(Arrays.asList(
                                CC.translate("&7Mínimo: &f" + raid.getMinPlayers()),
                                CC.translate("&7Máximo: &f" + raid.getMaxPlayers()),
                                "",
                                CC.translate("&b[CLICK PARA CAMBIAR]")
                        ));
                        playersButton.setItemMeta(playersMeta);
                        contents.set(2, 3, ClickableItem.of(playersButton, e -> {
                            player.closeInventory();
                            RaidChatInputManager.startPlayersInput(player, raidId);
                        }));

                        // HABILITAR / DESHABILITAR
                        ItemStack toggleButton;
                        if (raid.isEnabled()) {
                            toggleButton = new ItemStack(Material.EMERALD);
                            ItemMeta toggleMeta = toggleButton.getItemMeta();
                            toggleMeta.setDisplayName(CC.translate("&a&lHabilitada"));
                            toggleMeta.setLore(Arrays.asList(
                                    CC.translate("&a✓ La raid está activa"),
                                    "",
                                    CC.translate("&c[CLICK PARA DESHABILITAR]")
                            ));
                            toggleButton.setItemMeta(toggleMeta);
                        } else {
                            toggleButton = new ItemStack(Material.REDSTONE);
                            ItemMeta toggleMeta = toggleButton.getItemMeta();
                            toggleMeta.setDisplayName(CC.translate("&c&lDeshabilitada"));
                            toggleMeta.setLore(Arrays.asList(
                                    CC.translate("&c✗ La raid está inactiva"),
                                    "",
                                    CC.translate("&a[CLICK PARA HABILITAR]")
                            ));
                            toggleButton.setItemMeta(toggleMeta);
                        }
                        contents.set(2, 4, ClickableItem.of(toggleButton, e -> {
                            if (raid.isEnabled()) {
                                RaidManager.disableRaid(raidId);
                                player.sendMessage(CC.translate("&c✗ Raid deshabilitada"));
                            } else {
                                RaidManager.enableRaid(raidId);
                                player.sendMessage(CC.translate("&a✓ Raid habilitada"));
                            }
                            RaidStorageManager.saveRaid(raid);
                            createRaidConfigMenu(raidId).open(player);
                        }));

                        // GUARDAR
                        ItemStack saveButton = new ItemStack(Material.CHEST);
                        ItemMeta saveMeta = saveButton.getItemMeta();
                        saveMeta.setDisplayName(CC.translate("&d&lGuardar"));
                        saveMeta.setLore(Arrays.asList(
                                CC.translate("&7Guarda esta raid"),
                                "",
                                CC.translate("&d[CLICK PARA GUARDAR]")
                        ));
                        saveButton.setItemMeta(saveMeta);
                        contents.set(2, 5, ClickableItem.of(saveButton, e -> {
                            RaidStorageManager.saveRaid(raid);
                            RaidStorageManager.saveAllRaids();
                            player.sendMessage(CC.translate("&a✓ Raid guardada"));
                            createRaidConfigMenu(raidId).open(player);
                        }));

                        // ELIMINAR
                        ItemStack deleteButton = new ItemStack(Material.ANVIL);
                        ItemMeta deleteMeta = deleteButton.getItemMeta();
                        deleteMeta.setDisplayName(CC.translate("&c&lEliminar Raid"));
                        deleteMeta.setLore(Arrays.asList(
                                CC.translate("&7Elimina esta raid permanentemente"),
                                "",
                                CC.translate("&c[CLICK PARA ELIMINAR]")
                        ));
                        deleteButton.setItemMeta(deleteMeta);
                        contents.set(2, 7, ClickableItem.of(deleteButton, e -> {
                            RaidDeleteConfirmMenu.createDeleteConfirmMenu(raidId).open(player);
                        }));

                        // ATRÁS
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 4, ClickableItem.of(backButton, e -> {
                            RaidListMenu.createRaidListMenu(1).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&6&lConfig Raid"))
                .build();
    }
}