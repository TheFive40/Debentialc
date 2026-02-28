package org.debentialc.claims.managers;

public class ClaimsPermissions {

    public static final String BASE    = "claims.";
    public static final String COMMAND = BASE + "command.";

    // ── Admin ────────────────────────────────────────────────────────
    public static final String ADMIN_BYPASS = BASE + "admin.bypass";
    public static final String ADMIN_MANAGE = BASE + "admin.manage";

    // ── Terrain base ─────────────────────────────────────────────────
    public static final String TERRAIN_CREATE   = COMMAND + "terrain.create";
    public static final String TERRAIN_PRICE    = COMMAND + "terrain.price";
    public static final String TERRAIN_COMMIT   = COMMAND + "terrain.commit";
    public static final String TERRAIN_INFO     = COMMAND + "terrain.info";
    public static final String TERRAIN_MEMBER   = COMMAND + "terrain.member";
    public static final String TERRAIN_KICK     = COMMAND + "terrain.kick";
    public static final String TERRAIN_TRANSFER = COMMAND + "terrain.transfer";
    public static final String TERRAIN_SELL     = COMMAND + "terrain.sell";
    public static final String TERRAIN_DELETE   = COMMAND + "terrain.delete";
    public static final String TERRAIN_DISSOLVE = COMMAND + "terrain.dissolve";

    /** Permiso base para abrir el menú de personalización */
    public static final String TERRAIN_CUSTOMIZE        = COMMAND + "terrain.customize";
    /** Cambiar el bioma del terreno */
    public static final String TERRAIN_CUSTOMIZE_BIOME   = TERRAIN_CUSTOMIZE + ".biome";
    /** Cambiar el suelo del terreno */
    public static final String TERRAIN_CUSTOMIZE_FLOOR   = TERRAIN_CUSTOMIZE + ".floor";
    /** Cambiar el clima del terreno */
    public static final String TERRAIN_CUSTOMIZE_WEATHER = TERRAIN_CUSTOMIZE + ".weather";
    /** Configurar reglas de juego del terreno */
    public static final String TERRAIN_CUSTOMIZE_RULES   = TERRAIN_CUSTOMIZE + ".rules";
    /** Configurar el tiempo (día/noche) del terreno */
    public static final String TERRAIN_CUSTOMIZE_TIME    = TERRAIN_CUSTOMIZE + ".time";
    /** Aplicar efectos de poción ambientales */
    public static final String TERRAIN_CUSTOMIZE_EFFECTS = TERRAIN_CUSTOMIZE + ".effects";
}