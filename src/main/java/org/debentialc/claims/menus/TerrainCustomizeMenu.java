package org.debentialc.claims.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.TerrainCustomizeManager;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class TerrainCustomizeMenu {

    public static SmartInventory createMainMenu(final Terrain terrain, final Player viewer) {
        return SmartInventory.builder()
                .id("terrain_customize_" + terrain.getId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 7)));

                        ItemStack title = makeItem(Material.NETHER_STAR,
                                "&6&l✦ Personalización de Terreno",
                                Arrays.asList(
                                        CC.translate("&7Terreno: &f" + terrain.getId()),
                                        CC.translate("&7Tamaño: &f" + terrain.getChunks() + " chunk(s)"),
                                        CC.translate("&8&m                              "),
                                        CC.translate("&7Elige una categoría para personalizar")
                                ));
                        contents.set(0, 4, ClickableItem.empty(title));

                        boolean hasBiome = player.hasPermission(ClaimsPermissions.TERRAIN_CUSTOMIZE_BIOME);
                        ItemStack biomeBtn = makeItem(
                                hasBiome ? Material.SAPLING : Material.REDSTONE_TORCH_ON,
                                hasBiome ? "&a&lCambiar Bioma" : "&c&lCambiar Bioma &8(VIP)",
                                Arrays.asList(
                                        CC.translate("&7Cambia el bioma visual de tu terreno"),
                                        CC.translate("&7(ambiente, sonidos, vegetación)"),
                                        CC.translate(""),
                                        CC.translate(hasBiome ? "&a▶ Click para elegir" : "&c✗ Requiere permiso VIP")
                                ));
                        if (hasBiome) {
                            contents.set(1, 2, ClickableItem.of(biomeBtn, e ->
                                    createBiomeMenu(terrain, player).open(player)));
                        } else {
                            contents.set(1, 2, ClickableItem.empty(biomeBtn));
                        }

                        boolean hasFloor = player.hasPermission(ClaimsPermissions.TERRAIN_CUSTOMIZE_FLOOR);
                        ItemStack floorBtn = makeItem(
                                hasFloor ? Material.GRASS : Material.REDSTONE_TORCH_ON,
                                hasFloor ? "&a&lPersonalizar Suelo" : "&c&lPersonalizar Suelo &8(VIP)",
                                Arrays.asList(
                                        CC.translate("&7Cambia el bloque de suelo de tu terreno"),
                                        CC.translate("&7a distintos materiales"),
                                        CC.translate(""),
                                        CC.translate(hasFloor ? "&a▶ Click para elegir" : "&c✗ Requiere permiso VIP")
                                ));
                        if (hasFloor) {
                            contents.set(1, 4, ClickableItem.of(floorBtn, e ->
                                    createFloorMenu(terrain, player).open(player)));
                        } else {
                            contents.set(1, 4, ClickableItem.empty(floorBtn));
                        }

                        boolean hasWeather = player.hasPermission(ClaimsPermissions.TERRAIN_CUSTOMIZE_WEATHER);
                        ItemStack weatherBtn = makeItem(
                                hasWeather ? Material.WATER_BUCKET : Material.REDSTONE_TORCH_ON,
                                hasWeather ? "&b&lClima del Terreno" : "&c&lClima &8(VIP)",
                                Arrays.asList(
                                        CC.translate("&7Controla lluvia y tormenta"),
                                        CC.translate("&7dentro de tu terreno"),
                                        CC.translate(""),
                                        CC.translate(hasWeather ? "&b▶ Click para configurar" : "&c✗ Requiere permiso VIP")
                                ));
                        if (hasWeather) {
                            contents.set(1, 6, ClickableItem.of(weatherBtn, e ->
                                    createWeatherMenu(terrain, player).open(player)));
                        } else {
                            contents.set(1, 6, ClickableItem.empty(weatherBtn));
                        }

                        boolean hasRules = player.hasPermission(ClaimsPermissions.TERRAIN_CUSTOMIZE_RULES);
                        ItemStack rulesBtn = makeItem(
                                hasRules ? Material.BOOK_AND_QUILL : Material.REDSTONE_TORCH_ON,
                                hasRules ? "&e&lReglas del Terreno" : "&c&lReglas &8(VIP)",
                                Arrays.asList(
                                        CC.translate("&7Configura PvP, daño por hambre,"),
                                        CC.translate("&7drops de mobs, explosiones, etc."),
                                        CC.translate(""),
                                        CC.translate(hasRules ? "&e▶ Click para configurar" : "&c✗ Requiere permiso VIP")
                                ));
                        if (hasRules) {
                            contents.set(2, 2, ClickableItem.of(rulesBtn, e ->
                                    createRulesMenu(terrain, player).open(player)));
                        } else {
                            contents.set(2, 2, ClickableItem.empty(rulesBtn));
                        }

                        boolean hasTime = player.hasPermission(ClaimsPermissions.TERRAIN_CUSTOMIZE_TIME);
                        ItemStack timeBtn = makeItem(
                                hasTime ? Material.WATCH : Material.REDSTONE_TORCH_ON,
                                hasTime ? "&d&lCiclo de Tiempo" : "&c&lTiempo &8(VIP)",
                                Arrays.asList(
                                        CC.translate("&7Fija o cambia el ciclo día/noche"),
                                        CC.translate("&7en tu terreno"),
                                        CC.translate(""),
                                        CC.translate(hasTime ? "&d▶ Click para configurar" : "&c✗ Requiere permiso VIP")
                                ));
                        if (hasTime) {
                            contents.set(2, 4, ClickableItem.of(timeBtn, e ->
                                    createTimeMenu(terrain, player).open(player)));
                        } else {
                            contents.set(2, 4, ClickableItem.empty(timeBtn));
                        }

                        boolean hasEffects = player.hasPermission(ClaimsPermissions.TERRAIN_CUSTOMIZE_EFFECTS);
                        ItemStack effectsBtn = makeItem(
                                hasEffects ? Material.BLAZE_POWDER : Material.REDSTONE_TORCH_ON,
                                hasEffects ? "&5&lEfectos Ambientales" : "&c&lEfectos &8(VIP+)",
                                Arrays.asList(
                                        CC.translate("&7Aplica efectos de poción permanentes"),
                                        CC.translate("&7a quienes entren a tu terreno"),
                                        CC.translate(""),
                                        CC.translate(hasEffects ? "&5▶ Click para configurar" : "&c✗ Requiere permiso VIP+")
                                ));
                        if (hasEffects) {
                            contents.set(2, 6, ClickableItem.of(effectsBtn, e ->
                                    createEffectsMenu(terrain, player).open(player)));
                        } else {
                            contents.set(2, 6, ClickableItem.empty(effectsBtn));
                        }

                        ItemStack back = makeItem(Material.ARROW, "&7← Volver al terreno", null);
                        contents.set(3, 4, ClickableItem.of(back, e ->
                                TerritoryInfoMenu.createInfoMenu(terrain, player).open(player)));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(4, 9)
                .title(CC.translate("&6✦ Personalizar: " + terrain.getId()))
                .build();
    }

    public static SmartInventory createBiomeMenu(final Terrain terrain, final Player viewer) {
        return SmartInventory.builder()
                .id("terrain_biome_" + terrain.getId())
                .provider(new InventoryProvider() {

                    private final Object[][] BIOMES = {
                            {"&2Bosque",       "FOREST",        Material.LOG},
                            {"&aPlanicias",     "PLAINS",        Material.GRASS},
                            {"&eDesierto",      "DESERT",        Material.SANDSTONE},
                            {"&bTundra",        "ICE_FLATS",     Material.PACKED_ICE},
                            {"&9Océano",        "OCEAN",         Material.WATER_BUCKET},
                            {"&dJungla",        "JUNGLE",        Material.VINE},
                            {"&5Mesa",          "MESA",          Material.STAINED_CLAY},
                            {"&6Sabana",        "SAVANNA",       Material.LOG},
                            {"&cNether",        "HELL",          Material.NETHERRACK},
                            {"&8End",           "SKY",           Material.ENDER_STONE},
                            {"&fMontañas",      "EXTREME_HILLS", Material.COBBLESTONE},
                    };

                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 11)));

                        ItemStack title = makeItem(Material.SAPLING,
                                "&a&lElegir Bioma",
                                Arrays.asList(
                                        CC.translate("&7Terreno: &f" + terrain.getId()),
                                        CC.translate("&7El bioma afecta la temperatura, lluvia y vegetación")
                                ));
                        contents.set(0, 4, ClickableItem.empty(title));

                        int row = 1, col = 1;
                        for (Object[] bio : BIOMES) {
                            final String biomeName   = (String) bio[1];
                            final String displayName = (String) bio[0];
                            ItemStack btn = makeItem((Material) bio[2], displayName,
                                    Arrays.asList(
                                            CC.translate("&7Bioma: &f" + biomeName),
                                            CC.translate(""),
                                            CC.translate("&a▶ Click para aplicar")
                                    ));
                            contents.set(row, col, ClickableItem.of(btn, e -> {
                                boolean ok = TerrainCustomizeManager.setBiome(terrain, player, biomeName);
                                player.sendMessage(ok
                                        ? CC.translate("&7Bioma del terreno &f" + terrain.getId() + " &7cambiado a &f" + displayName + "&7.")
                                        : CC.translate("&cNo se pudo cambiar el bioma."));
                                if (ok) player.closeInventory();
                            }));
                            col++;
                            if (col >= 8) { col = 1; row++; }
                            if (row >= 4) break;
                        }

                        addBackButton(contents, 3, 4, terrain, player);
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(4, 9)
                .title(CC.translate("&a&lBiomas"))
                .build();
    }

    public static SmartInventory createFloorMenu(final Terrain terrain, final Player viewer) {
        return SmartInventory.builder()
                .id("terrain_floor_" + terrain.getId())
                .provider(new InventoryProvider() {

                    private final Object[][] FLOORS = {
                            {"&2Pasto",          Material.GRASS,       (byte) 0},
                            {"&eArena",          Material.SAND,        (byte) 0},
                            {"&8Piedra",         Material.STONE,       (byte) 0},
                            {"&6Madera",         Material.WOOD,        (byte) 0},
                            {"&bHielo",          Material.ICE,         (byte) 0},
                            {"&fNieve",          Material.SNOW_BLOCK,  (byte) 0},
                            {"&cNetherrack",     Material.NETHERRACK,  (byte) 0},
                            {"&aEsmeralda",      Material.EMERALD_BLOCK,(byte) 0},
                            {"&6Oro",            Material.GOLD_BLOCK,  (byte) 0},
                            {"&7Hierro",         Material.IRON_BLOCK,  (byte) 0},
                            {"&dPiedra de End",  Material.ENDER_STONE, (byte) 0},
                            {"&9Obsidiana",      Material.OBSIDIAN,    (byte) 0},
                    };

                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 3)));

                        ItemStack title = makeItem(Material.GRASS,
                                "&a&lPersonalizar Suelo",
                                Arrays.asList(
                                        CC.translate("&7Terreno: &f" + terrain.getId()),
                                        CC.translate("&7Reemplaza el suelo en toda el área del terreno"),
                                        CC.translate("&c&l⚠ &cOperación puede tardar algunos segundos")
                                ));
                        contents.set(0, 4, ClickableItem.empty(title));

                        int row = 1, col = 1;
                        for (Object[] floor : FLOORS) {
                            final String displayName = (String)   floor[0];
                            final Material mat       = (Material) floor[1];
                            final byte     data      = (byte)     floor[2];
                            ItemStack btn = makeItem(mat, displayName,
                                    Arrays.asList(
                                            CC.translate("&7Material: &f" + mat.name()),
                                            CC.translate(""),
                                            CC.translate("&a▶ Click para aplicar al suelo")
                                    ));
                            contents.set(row, col, ClickableItem.of(btn, e -> {
                                player.sendMessage(CC.translate("&7Aplicando suelo &f" + displayName + " &7al terreno, espera..."));
                                player.closeInventory();
                                TerrainCustomizeManager.setFloor(terrain, player, mat, data);
                            }));
                            col++;
                            if (col >= 8) { col = 1; row++; }
                            if (row >= 4) break;
                        }

                        addBackButton(contents, 3, 4, terrain, player);
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(4, 9)
                .title(CC.translate("&a&lSuelo del Terreno"))
                .build();
    }

    public static SmartInventory createWeatherMenu(final Terrain terrain, final Player viewer) {
        return SmartInventory.builder()
                .id("terrain_weather_" + terrain.getId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 9)));

                        ItemStack title = makeItem(Material.WATER_BUCKET,
                                "&b&lClima del Terreno",
                                Arrays.asList(CC.translate("&7Terreno: &f" + terrain.getId())));
                        contents.set(0, 4, ClickableItem.empty(title));

                        ItemStack sunny = makeItem(Material.GOLD_NUGGET, "&e&lSoleado",
                                Arrays.asList(
                                        CC.translate("&7Activa un día despejado y soleado"),
                                        CC.translate(""),
                                        CC.translate("&e▶ Click para aplicar")
                                ));
                        contents.set(1, 2, ClickableItem.of(sunny, e -> {
                            TerrainCustomizeManager.setWeather(terrain, player, "CLEAR");
                            player.sendMessage(CC.translate("&7Clima del terreno cambiado a &eSoleado&7."));
                            player.closeInventory();
                        }));

                        ItemStack rain = makeItem(Material.WATER_BUCKET, "&9&lLluvia",
                                Arrays.asList(
                                        CC.translate("&7Activa lluvia en el terreno"),
                                        CC.translate(""),
                                        CC.translate("&9▶ Click para aplicar")
                                ));
                        contents.set(1, 4, ClickableItem.of(rain, e -> {
                            TerrainCustomizeManager.setWeather(terrain, player, "RAIN");
                            player.sendMessage(CC.translate("&7Clima del terreno cambiado a &9Lluvia&7."));
                            player.closeInventory();
                        }));

                        ItemStack storm = makeItem(Material.FLINT_AND_STEEL, "&8&lTormenta",
                                Arrays.asList(
                                        CC.translate("&7Activa tormenta con rayos en el terreno"),
                                        CC.translate(""),
                                        CC.translate("&8▶ Click para aplicar")
                                ));
                        contents.set(1, 6, ClickableItem.of(storm, e -> {
                            TerrainCustomizeManager.setWeather(terrain, player, "STORM");
                            player.sendMessage(CC.translate("&7Clima del terreno cambiado a &8Tormenta&7."));
                            player.closeInventory();
                        }));

                        addBackButton(contents, 2, 4, terrain, player);
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(3, 9)
                .title(CC.translate("&b&lClima"))
                .build();
    }

    public static SmartInventory createRulesMenu(final Terrain terrain, final Player viewer) {
        return SmartInventory.builder()
                .id("terrain_rules_" + terrain.getId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 14)));

                        ItemStack title = makeItem(Material.BOOK_AND_QUILL,
                                "&e&lReglas del Terreno",
                                Arrays.asList(
                                        CC.translate("&7Terreno: &f" + terrain.getId()),
                                        CC.translate("&7Click en una regla para alternar ON/OFF")
                                ));
                        contents.set(0, 4, ClickableItem.empty(title));

                        String[][] rules = {
                                {"pvp",           "PvP entre jugadores",    "Permite el combate entre jugadores"},
                                {"mobGriefing",   "Griefing de Mobs",       "Los mobs pueden modificar bloques y robar"},
                                {"keepInventory", "Conservar Inventario",   "Conservar ítems al morir dentro del terreno"},
                                {"doFireTick",    "Propagación de Fuego",   "El fuego puede extenderse por el terreno"},
                                {"tntExplodes",   "Explosión de TNT",       "El TNT causa daño de bloques en el terreno"},
                                {"doMobSpawning", "Spawn de Mobs",          "Los mobs pueden aparecer en el terreno"},
                                {"naturalRegen",  "Regeneración Natural",   "Los jugadores se regeneran vida naturalmente"},
                                {"doHunger",      "Hambre",                 "La barra de hambre baja con el tiempo"},
                        };

                        int row = 1, col = 1;
                        for (String[] rule : rules) {
                            final String ruleKey   = rule[0];
                            final String ruleLabel = rule[1];
                            boolean enabled = TerrainCustomizeManager.getRuleValue(terrain, ruleKey);
                            Material mat = enabled ? Material.EMERALD : Material.REDSTONE;
                            String stateStr = enabled ? "&aACTIVO" : "&cINACTIVO";
                            ItemStack btn = makeItem(mat, "&f" + ruleLabel,
                                    Arrays.asList(
                                            CC.translate("&7" + rule[2]),
                                            CC.translate("&7Estado: " + stateStr),
                                            CC.translate(""),
                                            CC.translate("&e▶ Click para alternar")
                                    ));
                            contents.set(row, col, ClickableItem.of(btn, e -> {
                                boolean newVal = TerrainCustomizeManager.toggleRule(terrain, player, ruleKey);
                                player.sendMessage(CC.translate("&7Regla &f" + ruleLabel + " &7→ " + (newVal ? "&aACTIVO" : "&cINACTIVO")));
                                createRulesMenu(terrain, player).open(player);
                            }));
                            col++;
                            if (col >= 8) { col = 1; row++; }
                            if (row >= 4) break;
                        }

                        addBackButton(contents, 3, 4, terrain, player);
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(4, 9)
                .title(CC.translate("&e&lReglas del Terreno"))
                .build();
    }

    public static SmartInventory createTimeMenu(final Terrain terrain, final Player viewer) {
        return SmartInventory.builder()
                .id("terrain_time_" + terrain.getId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 4)));

                        ItemStack title = makeItem(Material.WATCH,
                                "&d&lCiclo de Tiempo",
                                Arrays.asList(CC.translate("&7Configura el tiempo del mundo en tu terreno")));
                        contents.set(0, 4, ClickableItem.empty(title));

                        Object[][] times = {
                                {"&e&lAmanecer",   0L,     Material.SPONGE},
                                {"&f&lMediodía",   6000L,  Material.GLOWSTONE},
                                {"&6&lTarde",      12000L, Material.PUMPKIN},
                                {"&8&lMedianoche", 18000L, Material.COAL_BLOCK},
                        };

                        int col = 1;
                        for (Object[] t : times) {
                            final String label = (String) t[0];
                            final long   ticks = (long)   t[1];
                            ItemStack btn = makeItem((Material) t[2], label,
                                    Arrays.asList(
                                            CC.translate("&7Ticks: &f" + ticks),
                                            CC.translate(""),
                                            CC.translate("&d▶ Click para aplicar")
                                    ));
                            contents.set(1, col, ClickableItem.of(btn, e -> {
                                TerrainCustomizeManager.setTime(terrain, player, ticks);
                                player.sendMessage(CC.translate("&7Tiempo del terreno configurado a " + label + "&7."));
                                player.closeInventory();
                            }));
                            col += 2;
                        }

                        addBackButton(contents, 2, 4, terrain, player);
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(3, 9)
                .title(CC.translate("&d&lCiclo de Tiempo"))
                .build();
    }

    public static SmartInventory createEffectsMenu(final Terrain terrain, final Player viewer) {
        return SmartInventory.builder()
                .id("terrain_effects_" + terrain.getId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 5)));

                        ItemStack title = makeItem(Material.BLAZE_POWDER,
                                "&5&lEfectos Ambientales",
                                Arrays.asList(
                                        CC.translate("&7Terreno: &f" + terrain.getId()),
                                        CC.translate("&7Aplica un efecto de poción permanente"),
                                        CC.translate("&7a todos los que entren al terreno")
                                ));
                        contents.set(0, 4, ClickableItem.empty(title));

                        Object[][] effects = {
                                {"&a&lRegeneración",   "REGENERATION",    Material.SPECKLED_MELON},
                                {"&e&lVelocidad",      "SPEED",           Material.SUGAR},
                                {"&6&lFuerza",         "INCREASE_DAMAGE", Material.BLAZE_ROD},
                                {"&f&lVisión Nocturna","NIGHT_VISION",    Material.GOLDEN_CARROT},
                                {"&cInvisibilidad",    "INVISIBILITY",    Material.FERMENTED_SPIDER_EYE},
                                {"&dLentitud",         "SLOW",            Material.SOUL_SAND},
                                {"&7Ninguno",          "NONE",            Material.REDSTONE_TORCH_ON},
                        };

                        int row = 1, col = 1;
                        for (Object[] eff : effects) {
                            final String label  = (String) eff[0];
                            final String effect = (String) eff[1];
                            boolean active = TerrainCustomizeManager.getEffect(terrain).equals(effect);
                            List<String> lore = new ArrayList<String>();
                            lore.add(CC.translate("&7Efecto: &f" + effect));
                            if (active) lore.add(CC.translate("&a✔ ACTIVO ACTUALMENTE"));
                            lore.add(CC.translate(""));
                            lore.add(CC.translate("&5▶ Click para activar"));
                            ItemStack btn = makeItem((Material) eff[2], label, lore);
                            contents.set(row, col, ClickableItem.of(btn, e -> {
                                TerrainCustomizeManager.setEffect(terrain, player, effect);
                                player.sendMessage(CC.translate("&7Efecto ambiental del terreno: " + label));
                                createEffectsMenu(terrain, player).open(player);
                            }));
                            col++;
                            if (col >= 8) { col = 1; row++; }
                            if (row >= 3) break;
                        }

                        addBackButton(contents, 3, 4, terrain, player);
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(4, 9)
                .title(CC.translate("&5&lEfectos Ambientales"))
                .build();
    }

    private static ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CC.translate(name));
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static void addBackButton(InventoryContents contents, int row, int col,
                                      Terrain terrain, Player player) {
        ItemStack back = makeItem(Material.ARROW, "&7← Volver", null);
        contents.set(row, col, ClickableItem.of(back, e ->
                createMainMenu(terrain, player).open(player)));
    }
}