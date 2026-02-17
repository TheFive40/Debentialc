package org.debentialc.claims.managers;

public class ClaimsPermissions {

    public static final String BASE = "claims.";
    public static final String COMMAND = BASE + "command.";

    public static final String ADMIN_BYPASS = BASE + "admin.bypass";
    public static final String ADMIN_MANAGE = BASE + "admin.manage";

    public static final String TERRAIN_CREATE = COMMAND + "terrain.create";
    public static final String TERRAIN_PRICE = COMMAND + "terrain.price";
    public static final String TERRAIN_COMMIT = COMMAND + "terrain.commit";
    public static final String TERRAIN_INFO = COMMAND + "terrain.info";
    public static final String TERRAIN_MEMBER = COMMAND + "terrain.member";
    public static final String TERRAIN_KICK = COMMAND + "terrain.kick";
    public static final String TERRAIN_TRANSFER = COMMAND + "terrain.transfer";
    public static final String TERRAIN_SELL = COMMAND + "terrain.sell";
}