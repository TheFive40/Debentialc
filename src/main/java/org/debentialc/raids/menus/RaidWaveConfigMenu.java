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
import org.debentialc.raids.models.Wave;
import org.debentialc.raids.models.SpawnPoint;
import org.debentialc.raids.models.WaveReward;
import org.debentialc.service.CC;

import java.util.*;

/**
 * Menú visual de configuración de una oleada individual
 */
public class RaidWaveConfigMenu {

    public static SmartInventory createWaveConfigMenu(String raidId, int waveIndex) {
        Raid raid = RaidManager.getRaidById(raidId);
        if (raid == null) return null;
        Wave wave = raid.getWaveByIndex(waveIndex);
        if (wave == null) return null;

        return SmartInventory.builder()
                .id("raid_wave_cfg_" + raidId + "_" + waveIndex)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(RaidMainMenu.createGlassPane((short) 4)));

                        // TÍTULO
                        ItemStack titleItem = new ItemStack(Material.MAGMA_CREAM);
                        ItemMeta titleMeta = titleItem.getItemMeta();
                        titleMeta.setDisplayName(CC.translate("&6&lOleada " + wave.getWaveNumber()));
                        titleMeta.setLore(Arrays.asList(
                                CC.translate("&7Raid: &f" + raid.getRaidName()),
                                CC.translate("&7Enemigos: &f" + wave.getTotalEnemies()),
                                CC.translate("&7Spawns: &f" + wave.getSpawnPoints().size()),
                                CC.translate("&7Recompensas: &f" + wave.getRewards().size())
                        ));
                        titleItem.setItemMeta(titleMeta);
                        contents.set(0, 4, ClickableItem.empty(titleItem));

                        // === SECCIÓN SPAWN POINTS ===
                        ItemStack spawnTitle = new ItemStack(Material.MOB_SPAWNER);
                        ItemMeta spawnTitleMeta = spawnTitle.getItemMeta();
                        spawnTitleMeta.setDisplayName(CC.translate("&c&lPuntos de Spawn"));
                        spawnTitleMeta.setLore(Arrays.asList(
                                CC.translate("&7Total: &f" + wave.getSpawnPoints().size())
                        ));
                        spawnTitle.setItemMeta(spawnTitleMeta);
                        contents.set(1, 1, ClickableItem.empty(spawnTitle));

                        // Spawn points existentes
                        int col = 2;
                        for (int i = 0; i < Math.min(wave.getSpawnPoints().size(), 5); i++) {
                            SpawnPoint sp = wave.getSpawnPoints().get(i);
                            final int spIndex = i;

                            ItemStack spItem = new ItemStack(Material.SKULL_ITEM);
                            ItemMeta spMeta = spItem.getItemMeta();
                            spMeta.setDisplayName(CC.translate("&e" + sp.getNpcName()));

                            List<String> spLore = new ArrayList<>();
                            spLore.add(CC.translate("&7Tab: &f" + sp.getNpcTab()));
                            spLore.add(CC.translate("&7Cantidad: &f" + sp.getQuantity()));
                            if (sp.getLocation() != null) {
                                spLore.add(CC.translate("&7Pos: &f" + sp.getLocation().getBlockX() +
                                        ", " + sp.getLocation().getBlockY() +
                                        ", " + sp.getLocation().getBlockZ()));
                            }
                            spLore.add("");
                            spLore.add(CC.translate("&c[CLICK DERECHO] &7Eliminar"));
                            spMeta.setLore(spLore);
                            spItem.setItemMeta(spMeta);

                            contents.set(1, col, ClickableItem.of(spItem, e -> {
                                if (e.isRightClick()) {
                                    wave.getSpawnPoints().remove(spIndex);
                                    RaidManager.updateRaid(raid);
                                    RaidStorageManager.saveRaid(raid);
                                    player.sendMessage(CC.translate("&c✗ Spawn point eliminado"));
                                    createWaveConfigMenu(raidId, waveIndex).open(player);
                                }
                            }));

                            col++;
                            if (col > 6) break;
                        }

                        // Botón agregar spawn point
                        ItemStack addSpawnButton = new ItemStack(Material.EMERALD);
                        ItemMeta addSpawnMeta = addSpawnButton.getItemMeta();
                        addSpawnMeta.setDisplayName(CC.translate("&a&l+ Agregar Spawn"));
                        addSpawnMeta.setLore(Arrays.asList(
                                CC.translate("&7Agrega un punto de spawn"),
                                CC.translate("&7Se usará tu posición actual"),
                                "",
                                CC.translate("&a[CLICK PARA AGREGAR]")
                        ));
                        addSpawnButton.setItemMeta(addSpawnMeta);
                        contents.set(1, 7, ClickableItem.of(addSpawnButton, e -> {
                            player.closeInventory();
                            RaidChatInputManager.startSpawnPointInput(player, raidId, waveIndex);
                        }));

                        // === SECCIÓN RECOMPENSAS ===
                        ItemStack rewardTitle = new ItemStack(Material.DIAMOND);
                        ItemMeta rewardTitleMeta = rewardTitle.getItemMeta();
                        rewardTitleMeta.setDisplayName(CC.translate("&b&lRecompensas"));
                        rewardTitleMeta.setLore(Arrays.asList(
                                CC.translate("&7Total: &f" + wave.getRewards().size())
                        ));
                        rewardTitle.setItemMeta(rewardTitleMeta);
                        contents.set(2, 1, ClickableItem.empty(rewardTitle));

                        // Recompensas existentes
                        col = 2;
                        for (int i = 0; i < Math.min(wave.getRewards().size(), 5); i++) {
                            WaveReward reward = wave.getRewards().get(i);
                            final int rwIndex = i;

                            ItemStack rwItem = new ItemStack(Material.GOLD_INGOT);
                            ItemMeta rwMeta = rwItem.getItemMeta();
                            String cmdDisplay = reward.getCommand().length() > 20 ?
                                    reward.getCommand().substring(0, 20) + "..." : reward.getCommand();
                            rwMeta.setDisplayName(CC.translate("&e/" + cmdDisplay));
                            rwMeta.setLore(Arrays.asList(
                                    CC.translate("&7Comando: &f/" + reward.getCommand()),
                                    CC.translate("&7Probabilidad: &f" + reward.getProbability() + "%"),
                                    "",
                                    CC.translate("&c[CLICK DERECHO] &7Eliminar")
                            ));
                            rwItem.setItemMeta(rwMeta);

                            contents.set(2, col, ClickableItem.of(rwItem, e -> {
                                if (e.isRightClick()) {
                                    wave.getRewards().remove(rwIndex);
                                    RaidManager.updateRaid(raid);
                                    RaidStorageManager.saveRaid(raid);
                                    player.sendMessage(CC.translate("&c✗ Recompensa eliminada"));
                                    createWaveConfigMenu(raidId, waveIndex).open(player);
                                }
                            }));

                            col++;
                            if (col > 6) break;
                        }

                        // Botón agregar recompensa
                        ItemStack addRewardButton = new ItemStack(Material.EMERALD);
                        ItemMeta addRewardMeta = addRewardButton.getItemMeta();
                        addRewardMeta.setDisplayName(CC.translate("&a&l+ Agregar Recompensa"));
                        addRewardMeta.setLore(Arrays.asList(
                                CC.translate("&7Agrega un comando como recompensa"),
                                CC.translate("&7@p = jugadores de la raid"),
                                "",
                                CC.translate("&a[CLICK PARA AGREGAR]")
                        ));
                        addRewardButton.setItemMeta(addRewardMeta);
                        contents.set(2, 7, ClickableItem.of(addRewardButton, e -> {
                            player.closeInventory();
                            RaidChatInputManager.startRewardInput(player, raidId, waveIndex);
                        }));

                        // DESCRIPCIÓN DE OLEADA
                        ItemStack descButton = new ItemStack(Material.BOOK_AND_QUILL);
                        ItemMeta descMeta = descButton.getItemMeta();
                        descMeta.setDisplayName(CC.translate("&e&lDescripción"));
                        descMeta.setLore(Arrays.asList(
                                CC.translate("&7Actual: &f" + (wave.getDescription() != null ? wave.getDescription() : "Sin descripción")),
                                "",
                                CC.translate("&e[CLICK PARA EDITAR]")
                        ));
                        descButton.setItemMeta(descMeta);
                        contents.set(3, 2, ClickableItem.of(descButton, e -> {
                            player.closeInventory();
                            RaidChatInputManager.startWaveDescriptionInput(player, raidId, waveIndex);
                        }));

                        // ATRÁS
                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 0, ClickableItem.of(backButton, e -> {
                            RaidWavesMenu.createWavesMenu(raidId).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&6&lOleada " + wave.getWaveNumber()))
                .build();
    }
}