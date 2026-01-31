package org.debentialc.service;
import JinRyuu.JRMCore.JRMCoreH;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.api.entity.IDBCPlayer;
import noppes.npcs.scripted.NpcAPI;
import org.bukkit.entity.Player;

import java.util.*;


public class General {


    public static final String[] ranks = {"founder", "developer", "manager", "admin", "moderador", "quester", "helper", "constructor", "programador", "dev"
            , "staff", "moderador-manager", "dueño", "owner", "admin-plus", "ayudante", "helper", "builder", "builder-manager"};
    /*
    public static String DEX = "jrmcDexI";
    public static String SPI = "jrmcCncI";
    public static String CON = "jrmcCnsI";
    public static String STR = "jrmcStrI";
    public static String WIL = "jrmcWilI";
    public static String MND = "jrmcIntI";
    */
    public static String DEX2 = "jrmcDexI";
    public static String SPI2 = "jrmcCncI";
    public static String CON2 = "jrmcCnsI";
    public static String STR2 = "jrmcStrI";
    public static String WIL2 = "jrmcWilI";
    public static String MND2 = "jrmcIntI";


    public static String DEX = "dexterity";
    public static String SPI = "spirit";
    public static String CON = "constitution";
    public static String STR = "strength";
    public static String WIL = "willpower";
    public static String MND = "mind";

    public static HashMap<String, String> BONUS_STATS = new HashMap<>();
    public static HashMap<String, String> STATS_MAP = new HashMap<>();
    static {
        STATS_MAP.put ( "STR", STR2 );
        STATS_MAP.put ( "DEX", DEX2 );
        STATS_MAP.put ( "CON", CON2 );
        STATS_MAP.put ( "WIL", WIL2 );
        STATS_MAP.put ( "MND", MND2 );
        STATS_MAP.put ( "SPI", SPI2 );
    }
    public static IDBCPlayer getDBCPlayer(String name) {
        return NpcAPI.Instance().getPlayer(name).getDBCPlayer();
    }
    public static int getSTAT ( String stat, Player entity ) {
        return JRMCoreH.getInt ( toPlayerMP ( entity ), STATS_MAP.get ( stat.toUpperCase ( ) ) );
    }

    public static EntityPlayerMP toPlayerMP(Player player) {
        return (EntityPlayerMP)NpcAPI.Instance().getPlayer(player.getName()).getDBCPlayer().getMCEntity();
    }
    static {
        BONUS_STATS.put("STR", STR);
        BONUS_STATS.put("DEX", DEX);
        BONUS_STATS.put("CON", CON);
        BONUS_STATS.put("WIL", WIL);
        BONUS_STATS.put("MND", MND);
        BONUS_STATS.put("SPI", SPI);
    }
    public static int getLVL(Player player) {
        int str = JRMCoreH.getInt(toPlayerMP(player), STR);
        int dex = JRMCoreH.getInt(toPlayerMP(player), DEX);
        int con = JRMCoreH.getInt(toPlayerMP(player), CON);
        int wil = JRMCoreH.getInt(toPlayerMP(player), WIL);
        int mnd = JRMCoreH.getInt(toPlayerMP(player), MND);
        int spi = JRMCoreH.getInt(toPlayerMP(player), SPI);
        int lvl = (str + dex + con + wil + mnd + spi) / 5 - 11;
        return lvl;
    }

}
