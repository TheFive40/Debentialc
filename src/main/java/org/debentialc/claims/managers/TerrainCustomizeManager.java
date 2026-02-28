package org.debentialc.claims.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.debentialc.Main;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager que centraliza todas las personalizaciones de un terreno:
 * bioma, suelo, clima, reglas de juego, tiempo y efectos ambientales.
 *
 * Los ajustes se almacenan en memoria (Map<terrainId, ...>) y se persisten
 * en disco a través de una extensión del TerrainStorage o un fichero propio.
 * Por ahora usa un simple Properties-based storage independiente.
 */
public class TerrainCustomizeManager {


    /** Reglas por terreno: terrainId → (ruleKey → enabled) */
    private static final Map<String, Map<String, Boolean>> terrainRules   = new HashMap<>();
    /** Efecto ambiental activo por terreno: terrainId → effectName */
    private static final Map<String, String>               terrainEffects = new HashMap<>();
    /** Ajuste de tiempo bloqueado por terreno: terrainId → ticks (-1 = libre) */
    private static final Map<String, Long>                 terrainTime    = new HashMap<>();
    /** Clima bloqueado por terreno: terrainId → "CLEAR"/"RAIN"/"STORM"/"" */
    private static final Map<String, String>               terrainWeather = new HashMap<>();

    private static final Map<String, Boolean> RULE_DEFAULTS = new HashMap<>();
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

    /**
     * Cambia el bioma de todos los chunks del terreno.
     * Se ejecuta de forma asíncrona para no bloquear el hilo principal.
     *
     * @param terrain    Terreno objetivo
     * @param player     Jugador que ejecutó la acción (para feedback)
     * @param biomeName  Nombre del bioma (ej. "FOREST", "DESERT")
     * @return true si el bioma era válido y se lanzó la tarea
     */
    public static boolean setBiome(Terrain terrain, Player player, String biomeName) {
        Biome biome;
        try {
            biome = Biome.valueOf(biomeName);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (terrain.getOrigin() == null || !terrain.isCommitted()) return false;

        World world = terrain.getOrigin().getWorld();
        int ox = terrain.getOrigin().getBlockX();
        int oz = terrain.getOrigin().getBlockZ();
        int size = terrain.getSizeInBlocks();

        Bukkit.getScheduler().runTaskAsynchronously(Main.instance, () -> {
            for (int x = ox; x < ox + size; x++) {
                for (int z = oz; z < oz + size; z++) {
                    world.setBiome(x, z, biome);
                }
            }
            // Actualizar chunks para todos los jugadores cercanos
            Bukkit.getScheduler().runTask(Main.instance, () -> {
                for (int cx = ox >> 4; cx <= (ox + size) >> 4; cx++) {
                    for (int cz = oz >> 4; cz <= (oz + size) >> 4; cz++) {
                        world.refreshChunk(cx, cz);
                    }
                }
                player.sendMessage(CC.translate("&7Bioma del terreno &f" + terrain.getId() + " &7actualizado."));
            });
        });
        return true;
    }


    /**
     * Reemplaza la capa superficial del terreno con el material dado.
     * Busca el primer bloque sólido desde el techo hacia abajo y lo cambia.
     */
    public static void setFloor(Terrain terrain, Player player, Material mat, byte data) {
        if (terrain.getOrigin() == null || !terrain.isCommitted()) {
            player.sendMessage(CC.translate("&cEl terreno no está generado."));
            return;
        }

        World world = terrain.getOrigin().getWorld();
        int ox = terrain.getOrigin().getBlockX();
        int oz = terrain.getOrigin().getBlockZ();
        int size = terrain.getSizeInBlocks();

        Bukkit.getScheduler().runTaskAsynchronously(Main.instance, () -> {
            int changed = 0;
            for (int x = ox; x < ox + size; x++) {
                for (int z = oz; z < oz + size; z++) {
                    // Buscar el bloque de superficie (primer bloque sólido desde arriba)
                    Block surface = world.getHighestBlockAt(x, z);
                    if (surface != null) {
                        final Block toChange = surface;
                        Bukkit.getScheduler().runTask(Main.instance, () -> {
                            toChange.setTypeIdAndData(mat.getId(), data, false);
                        });
                        changed++;
                    }
                }
            }
            final int total = changed;
            Bukkit.getScheduler().runTask(Main.instance, () ->
                    player.sendMessage(CC.translate("&7Suelo del terreno &f" + terrain.getId()
                            + " &7cambiado. Bloques modificados: &f" + total)));
        });
    }


    /**
     * Ajusta el clima del mundo donde está el terreno.
     * (Minecraft no tiene clima por chunk, aplica al mundo completo pero
     *  se registra por terreno para revertir al salir si se desea.)
     *
     * @param type "CLEAR", "RAIN" o "STORM"
     */
    public static void setWeather(Terrain terrain, Player player, String type) {
        if (terrain.getOrigin() == null) return;
        World world = terrain.getOrigin().getWorld();
        terrainWeather.put(terrain.getId(), type);

        switch (type) {
            case "CLEAR":
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);
                break;
            case "RAIN":
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);
                break;
            case "STORM":
                world.setStorm(true);
                world.setThundering(true);
                world.setThunderDuration(Integer.MAX_VALUE);
                world.setWeatherDuration(Integer.MAX_VALUE);
                break;
        }
    }


    public static boolean getRuleValue(Terrain terrain, String ruleKey) {
        Map<String, Boolean> rules = terrainRules.get(terrain.getId());
        if (rules == null || !rules.containsKey(ruleKey)) {
            return RULE_DEFAULTS.getOrDefault(ruleKey, true);
        }
        return rules.get(ruleKey);
    }

    /**
     * Alterna una regla y devuelve el nuevo valor.
     */
    public static boolean toggleRule(Terrain terrain, Player player, String ruleKey) {
        terrainRules.computeIfAbsent(terrain.getId(), k -> new HashMap<>());
        boolean current = getRuleValue(terrain, ruleKey);
        boolean newVal  = !current;
        terrainRules.get(terrain.getId()).put(ruleKey, newVal);
        return newVal;
    }

    /**
     * Comprueba una regla específica del terreno. Usado por los listeners.
     */
    public static boolean isRuleEnabled(String terrainId, String ruleKey) {
        Map<String, Boolean> rules = terrainRules.get(terrainId);
        if (rules == null) return RULE_DEFAULTS.getOrDefault(ruleKey, true);
        return rules.getOrDefault(ruleKey, RULE_DEFAULTS.getOrDefault(ruleKey, true));
    }


    /**
     * Ajusta el tiempo del mundo. No hay tiempo por chunk en vanilla,
     * se aplica al mundo completo. Se registra para el terreno.
     */
    public static void setTime(Terrain terrain, Player player, long ticks) {
        if (terrain.getOrigin() == null) return;
        World world = terrain.getOrigin().getWorld();
        terrainTime.put(terrain.getId(), ticks);
        world.setTime(ticks);
    }


    public static String getEffect(Terrain terrain) {
        return terrainEffects.getOrDefault(terrain.getId(), "NONE");
    }

    public static void setEffect(Terrain terrain, Player player, String effectName) {
        terrainEffects.put(terrain.getId(), effectName);
        player.sendMessage(CC.translate("&7Efecto ambiental configurado. Los jugadores en el terreno lo recibirán."));
    }

    /**
     * Aplica el efecto ambiental al jugador si está dentro del terreno.
     * Llamar desde un task periódico (ej. cada 5 segundos).
     */
    public static void applyEffectToPlayer(Player player) {
        Terrain terrain = TerrainManager.getInstance().getTerrainAt(player.getLocation());
        if (terrain == null) return;
        String effectName = terrainEffects.getOrDefault(terrain.getId(), "NONE");
        if (effectName.equals("NONE")) return;

        PotionEffectType pet = getPotionEffectType(effectName);
        if (pet == null) return;

        player.addPotionEffect(new PotionEffect(pet, 200, 0, true), true);
    }

    private static PotionEffectType getPotionEffectType(String name) {
        switch (name) {
            case "REGENERATION":    return PotionEffectType.REGENERATION;
            case "SPEED":           return PotionEffectType.SPEED;
            case "INCREASE_DAMAGE": return PotionEffectType.INCREASE_DAMAGE;
            case "WATER_BREATHING": return PotionEffectType.WATER_BREATHING;
            case "NIGHT_VISION":    return PotionEffectType.NIGHT_VISION;
            case "INVISIBILITY":    return PotionEffectType.INVISIBILITY;
            case "SLOW":            return PotionEffectType.SLOW;
            default:                return null;
        }
    }


    public static boolean isPvpEnabled(String terrainId) {
        return isRuleEnabled(terrainId, "pvp");
    }

    public static boolean isMobSpawningEnabled(String terrainId) {
        return isRuleEnabled(terrainId, "doMobSpawning");
    }
}