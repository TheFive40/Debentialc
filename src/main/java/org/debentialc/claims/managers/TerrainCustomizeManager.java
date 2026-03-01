package org.debentialc.claims.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.debentialc.Main;
import org.debentialc.claims.models.Terrain;
import org.debentialc.claims.storage.TerrainCustomizeStorage;
import org.debentialc.service.CC;

import java.util.HashMap;
import java.util.Map;

public class TerrainCustomizeManager {

    private static TerrainCustomizeStorage storage;

    public static void initialize() {
        storage = new TerrainCustomizeStorage();
    }

    private static final Map<String, Map<String, Boolean>> terrainRules   = new HashMap<String, Map<String, Boolean>>();
    private static final Map<String, String>               terrainEffects = new HashMap<String, String>();
    private static final Map<String, Long>                 terrainTime    = new HashMap<String, Long>();
    private static final Map<String, String>               terrainWeather = new HashMap<String, String>();

    private static final Map<String, Boolean> RULE_DEFAULTS = new HashMap<String, Boolean>();
    static {
        RULE_DEFAULTS.put("pvp",            true);
        RULE_DEFAULTS.put("mobGriefing",    true);
        RULE_DEFAULTS.put("keepInventory",  false);
        RULE_DEFAULTS.put("doFireTick",     true);
        RULE_DEFAULTS.put("tntExplodes",    true);
        RULE_DEFAULTS.put("doMobSpawning",  true);
        RULE_DEFAULTS.put("naturalRegen",   true);
        RULE_DEFAULTS.put("doHunger",       true);
    }

    private static Map<String, Boolean> getRulesMap(String terrainId) {
        if (!terrainRules.containsKey(terrainId)) {
            if (storage != null) {
                Map<String, Boolean> loaded = storage.loadRules(terrainId);
                Map<String, Boolean> merged = new HashMap<String, Boolean>(RULE_DEFAULTS);
                merged.putAll(loaded);
                terrainRules.put(terrainId, merged);
            } else {
                terrainRules.put(terrainId, new HashMap<String, Boolean>(RULE_DEFAULTS));
            }
        }
        return terrainRules.get(terrainId);
    }

    // Reemplaza isCommitted() - solo verifica que el origen exista
    private static boolean hasOrigin(Terrain terrain, Player player) {
        if (terrain.getOrigin() == null) {
            player.sendMessage(CC.translate("&cEste terreno no tiene posicion de origen definida."));
            return false;
        }
        return true;
    }

    public static boolean setBiome(final Terrain terrain, final Player player, final String biomeName) {
        final Biome biome;
        try {
            biome = Biome.valueOf(biomeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(CC.translate("&cBioma desconocido: &f" + biomeName));
            return false;
        }

        if (!hasOrigin(terrain, player)) return false;

        final World world = terrain.getOrigin().getWorld();
        final int ox   = terrain.getOrigin().getBlockX();
        final int oz   = terrain.getOrigin().getBlockZ();
        final int size = terrain.getSizeInBlocks();
        final String tid = terrain.getId();

        Bukkit.getScheduler().runTaskAsynchronously(Main.instance, new Runnable() {
            public void run() {
                for (int x = ox; x < ox + size; x++) {
                    for (int z = oz; z < oz + size; z++) {
                        world.setBiome(x, z, biome);
                    }
                }
                Bukkit.getScheduler().runTask(Main.instance, new Runnable() {
                    public void run() {
                        for (int cx = ox >> 4; cx <= (ox + size) >> 4; cx++) {
                            for (int cz = oz >> 4; cz <= (oz + size) >> 4; cz++) {
                                world.refreshChunk(cx, cz);
                            }
                        }
                        player.sendMessage(CC.translate("&7Bioma del terreno &f" + tid + " &7cambiado a &f" + biomeName + "&7."));
                    }
                });
            }
        });
        return true;
    }

    public static void setFloor(Terrain terrain, Player player, Material mat, byte data) {
        setFloor(terrain, player, mat.getId(), data);
    }

    public static void setFloor(final Terrain terrain, final Player player, final int blockId, final byte data) {
        if (!hasOrigin(terrain, player)) return;

        final World world  = terrain.getOrigin().getWorld();
        final int ox       = terrain.getOrigin().getBlockX();
        final int oz       = terrain.getOrigin().getBlockZ();
        final int size     = terrain.getSizeInBlocks();
        final int baseY    = terrain.getOrigin().getBlockY() - 1;
        final String tid   = terrain.getId();

        player.sendMessage(CC.translate("&7Aplicando suelo al terreno &f" + tid + "&7, espera..."));

        Bukkit.getScheduler().runTask(Main.instance, new Runnable() {
            public void run() {
                int count = 0;
                for (int x = ox; x < ox + size; x++) {
                    for (int z = oz; z < oz + size; z++) {
                        world.getBlockAt(x, baseY, z).setTypeIdAndData(blockId, data, false);
                        count++;
                    }
                }
                player.sendMessage(CC.translate("&7Suelo cambiado. Bloques modificados: &f" + count));
            }
        });
    }

    public static void setWeather(Terrain terrain, Player player, String type) {
        if (!hasOrigin(terrain, player)) return;

        World world = terrain.getOrigin().getWorld();
        terrainWeather.put(terrain.getId(), type);
        if (storage != null) storage.setWeather(terrain.getId(), type);

        if ("CLEAR".equals(type)) {
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
        } else if ("RAIN".equals(type)) {
            world.setStorm(true);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
        } else if ("STORM".equals(type)) {
            world.setStorm(true);
            world.setThundering(true);
            world.setThunderDuration(Integer.MAX_VALUE);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    public static boolean getRuleValue(Terrain terrain, String ruleKey) {
        return getRulesMap(terrain.getId()).getOrDefault(ruleKey, RULE_DEFAULTS.getOrDefault(ruleKey, true));
    }

    public static boolean toggleRule(Terrain terrain, Player player, String ruleKey) {
        Map<String, Boolean> rules = getRulesMap(terrain.getId());
        boolean current = rules.getOrDefault(ruleKey, RULE_DEFAULTS.getOrDefault(ruleKey, true));
        boolean newVal  = !current;
        rules.put(ruleKey, newVal);
        if (storage != null) storage.setRule(terrain.getId(), ruleKey, newVal);
        return newVal;
    }

    public static boolean isRuleEnabled(String terrainId, String ruleKey) {
        if (!terrainRules.containsKey(terrainId)) {
            if (storage != null) {
                Map<String, Boolean> loaded = storage.loadRules(terrainId);
                Map<String, Boolean> merged = new HashMap<String, Boolean>(RULE_DEFAULTS);
                merged.putAll(loaded);
                terrainRules.put(terrainId, merged);
            } else {
                return RULE_DEFAULTS.getOrDefault(ruleKey, true);
            }
        }
        return terrainRules.get(terrainId).getOrDefault(ruleKey, RULE_DEFAULTS.getOrDefault(ruleKey, true));
    }

    public static void setTime(Terrain terrain, Player player, long ticks) {
        terrainTime.put(terrain.getId(), ticks);
        if (storage != null) storage.setTime(terrain.getId(), ticks);
        player.setPlayerTime(ticks, false);
        player.sendMessage(CC.translate("&7Tiempo del terreno configurado."));
    }

    public static void applyTimeToPlayer(Player player) {
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(player.getLocation());
        if (terrain != null) {
            Long cachedTicks = terrainTime.get(terrain.getId());
            if (cachedTicks == null && storage != null) {
                long diskTicks = storage.getTime(terrain.getId());
                if (diskTicks >= 0) {
                    terrainTime.put(terrain.getId(), diskTicks);
                    cachedTicks = diskTicks;
                }
            }
            if (cachedTicks != null && cachedTicks >= 0) {
                player.setPlayerTime(cachedTicks, false);
                return;
            }
        }
        if (!player.isPlayerTimeRelative()) {
            player.resetPlayerTime();
        }
    }

    public static String getEffect(Terrain terrain) {
        if (!terrainEffects.containsKey(terrain.getId()) && storage != null) {
            terrainEffects.put(terrain.getId(), storage.getEffect(terrain.getId()));
        }
        return terrainEffects.getOrDefault(terrain.getId(), "NONE");
    }

    public static void setEffect(Terrain terrain, Player player, String effectName) {
        terrainEffects.put(terrain.getId(), effectName);
        if (storage != null) storage.setEffect(terrain.getId(), effectName);
        if ("NONE".equals(effectName)) {
            player.sendMessage(CC.translate("&7Efecto ambiental desactivado."));
        } else {
            player.sendMessage(CC.translate("&7Efecto ambiental configurado."));
        }
    }

    public static void applyEffectToPlayer(Player player) {
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(player.getLocation());
        if (terrain == null) return;

        String effectName = getEffect(terrain);
        if ("NONE".equals(effectName) || effectName.isEmpty()) return;

        PotionEffectType pet = getPotionEffectType(effectName);
        if (pet == null) return;

        PotionEffect existing = null;
        for (PotionEffect pe : player.getActivePotionEffects()) {
            if (pe.getType().equals(pet)) { existing = pe; break; }
        }
        if (existing != null && existing.getDuration() > 100) return;

        player.addPotionEffect(new PotionEffect(pet, 300, 0, true), true);
    }

    private static PotionEffectType getPotionEffectType(String name) {
        if ("REGENERATION".equals(name))    return PotionEffectType.REGENERATION;
        if ("SPEED".equals(name))           return PotionEffectType.SPEED;
        if ("INCREASE_DAMAGE".equals(name)) return PotionEffectType.INCREASE_DAMAGE;
        if ("WATER_BREATHING".equals(name)) return PotionEffectType.WATER_BREATHING;
        if ("NIGHT_VISION".equals(name))    return PotionEffectType.NIGHT_VISION;
        if ("INVISIBILITY".equals(name))    return PotionEffectType.INVISIBILITY;
        if ("SLOW".equals(name))            return PotionEffectType.SLOW;
        return null;
    }

    public static boolean isPvpEnabled(String terrainId) {
        return isRuleEnabled(terrainId, "pvp");
    }

    public static boolean isMobSpawningEnabled(String terrainId) {
        return isRuleEnabled(terrainId, "doMobSpawning");
    }
}