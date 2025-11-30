package org.example.tools;
import noppes.npcs.api.entity.IDBCPlayer;
import noppes.npcs.scripted.NpcAPI;
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

    public static String DEX = "dexterity";
    public static String SPI = "spirit";
    public static String CON = "constitution";
    public static String STR = "strength";
    public static String WIL = "willpower";
    public static String MND = "mind";

    public static HashMap<String, String> STATS_MAP = new HashMap<> ( );
    public static IDBCPlayer getDBCPlayer(String name){
        return NpcAPI.Instance().getPlayer(name).getDBCPlayer();
    }
    static {
        STATS_MAP.put ( "STR", STR );
        STATS_MAP.put ( "DEX", DEX );
        STATS_MAP.put ( "CON", CON );
        STATS_MAP.put ( "WIL", WIL );
        STATS_MAP.put ( "MND", MND );
        STATS_MAP.put ( "SPI", SPI );
    }
}
