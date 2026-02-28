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

/**
 * Manager que centraliza todas las personalizaciones de un terreno:
 * bioma, suelo, clima, reglas de juego, tiempo y efectos ambientales.
 *
 * Los ajustes se guardan en disco mediante TerrainCustomizeStorage.
 * El tiempo es por jugador (no global); el clima sigue siendo global al mundo.
 */
public class TerrainCustomizeManager {


    private static TerrainCustomizeStorage storage;

    public static void initialize() {
        storage = new TerrainCustomizeStorage();
        Main.instance.getLogger().info("[Claims] TerrainCustomizeManager inicializado con persistencia.");
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


    public static boolean setBiome(final Terrain terrain, final Player player, final String biomeName) {
        final Biome biome;
        try {
            biome = Biome.valueOf(biomeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(CC.translate("&cBioma desconocido: &f" + biomeName));
            return false;
        }

        if (terrain.getOrigin() == null || !terrain.isCommitted()) {
            player.sendMessage(CC.translate("&cEl terreno no está generado."));
            return false;
        }

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


    /**
     * Versión con Material estándar — delega en la versión por ID.
     */
    public static void setFloor(Terrain terrain, Player player, Material mat, byte data) {
        setFloor(terrain, player, mat.getId(), data);
    }

    /**
     * Reemplaza el suelo del terreno con el block ID dado.
     * Soporta IDs de bloques de mods (> 255).
     *
     * FIX: ya no usa getHighestBlockAt (que devolvía el Y de las losas de borde).
     * Ahora usa el Y del origen del terreno — el mismo nivel donde están los bordes —
     * y solo modifica el interior (excluye los bloques de losa en los extremos).
     */
    public static void setFloor(final Terrain terrain, final Player player, final int blockId, final byte data) {
        if (terrain.getOrigin() == null || !terrain.isCommitted()) {
            player.sendMessage(CC.translate("&cEl terreno no está generado."));
            return;
        }

        final World world = terrain.getOrigin().getWorld();
        final int ox      = terrain.getOrigin().getBlockX();
        final int oz      = terrain.getOrigin().getBlockZ();
        final int size    = terrain.getSizeInBlocks();
        final int baseY   = terrain.getOrigin().getBlockY();
        final String tid  = terrain.getId();

        player.sendMessage(CC.translate("&7Aplicando suelo al terreno &f" + tid + "&7, espera..."));

        Bukkit.getScheduler().runTask(Main.instance, new Runnable() {
            public void run() {
                int count = 0;
                for (int x = ox + 1; x < ox + size - 1; x++) {
                    for (int z = oz + 1; z < oz + size - 1; z++) {
                        world.getBlockAt(x, baseY, z).setTypeIdAndData(blockId, data, false);
                        count++;
                    }
                }
                player.sendMessage(CC.translate("&7Suelo cambiado. Bloques modificados: &f" + count));
            }
        });
    }


    /**
     * Ajusta el clima global del mundo donde está el terreno y lo persiste.
     * (Minecraft no soporta clima por jugador en 1.7.10.)
     *
     * @param type "CLEAR", "RAIN" o "STORM"
     */
    public static void setWeather(Terrain terrain, Player player, String type) {
        if (terrain.getOrigin() == null) {
            player.sendMessage(CC.translate("&cEl terreno no tiene origen definido."));
            return;
        }
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

    // ─── Tiempo (por jugador) ─────────────────────────────────────────────────

    /**
     * Guarda el tiempo configurado para el terreno y lo persiste.
     * El tiempo NO es global: se aplica individualmente a cada jugador
     * que esté en el terreno mediante {@link #applyTimeToPlayer(Player)}.
     */
    public static void setTime(Terrain terrain, Player player, long ticks) {
        terrainTime.put(terrain.getId(), ticks);
        if (storage != null) storage.setTime(terrain.getId(), ticks);
        // Aplica al jugador que hizo el cambio de forma inmediata
        player.setPlayerTime(ticks, false);
        player.sendMessage(CC.translate("&7Tiempo del terreno configurado. Solo visible para ti y los jugadores en el terreno."));
    }

    /**
     * Llamar desde el task periódico (cada ~5 s) para mantener el tiempo por jugador.
     *
     * - Si el jugador está en un terreno con tiempo configurado → aplica ese tiempo solo a él.
     * - Si no está en ningún terreno con tiempo configurado → restaura el tiempo real del mundo.
     */
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

        // Fuera de terreno con tiempo → restaurar tiempo real si estaba sobreescrito
        if (!player.isPlayerTimeRelative()) {
            player.resetPlayerTime();
        }
    }

    // ─── Efectos ambientales ──────────────────────────────────────────────────

    /** Devuelve el nombre del efecto activo para el terreno (con caché + persistencia). */
    public static String getEffect(Terrain terrain) {
        if (!terrainEffects.containsKey(terrain.getId()) && storage != null) {
            terrainEffects.put(terrain.getId(), storage.getEffect(terrain.getId()));
        }
        return terrainEffects.getOrDefault(terrain.getId(), "NONE");
    }

    /**
     * Asigna o desactiva el efecto ambiental del terreno.
     * Pasar "NONE" desactiva cualquier efecto activo.
     */
    public static void setEffect(Terrain terrain, Player player, String effectName) {
        terrainEffects.put(terrain.getId(), effectName);
        if (storage != null) storage.setEffect(terrain.getId(), effectName);
        if ("NONE".equals(effectName)) {
            player.sendMessage(CC.translate("&7Efecto ambiental desactivado en el terreno."));
        } else {
            player.sendMessage(CC.translate("&7Efecto ambiental configurado. Los jugadores en el terreno lo recibirán."));
        }
    }

    /**
     * Aplica el efecto ambiental al jugador si está dentro de un terreno con efecto activo.
     * Llamar desde un task periódico (ej. cada 5 segundos).
     */
    public static void applyEffectToPlayer(Player player) {
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(player.getLocation());
        if (terrain == null) return;

        String effectName = getEffect(terrain);
        if ("NONE".equals(effectName) || effectName.isEmpty()) return;

        PotionEffectType pet = getPotionEffectType(effectName);
        if (pet == null) return;

        player.addPotionEffect(new PotionEffect(pet, 200, 0, true), true);
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