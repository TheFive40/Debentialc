package org.debentialc.customitems.tools.stats;

import kamkeel.npcdbc.constants.DBCClass;
import kamkeel.npcdbc.constants.DBCRace;
import noppes.npcs.api.entity.IDBCPlayer;
import org.debentialc.Main;
import org.debentialc.service.General;
import org.debentialc.customitems.tools.config.DBCConfigManager;

public class StatsCalculator {

    public static int getMaxHealth(IDBCPlayer idbcPlayer) {
        return getMaxHealth(idbcPlayer, 1.0);
    }

    public static int getMaxHealth(IDBCPlayer idbcPlayer, double level) {
        int race = idbcPlayer.getRace();
        int dbcclass = idbcPlayer.getDBCClass();
        int kiMax = 0;
        int lvlWIL = General.getSTAT("CON", Main.instance.getServer().getPlayer(idbcPlayer.getName()));
        String[] skills = idbcPlayer.getNbt().getCompound("PlayerPersisted")
                .getString("jrmcSSlts").split(",");
        int lvl = 0;
        for (String sk : skills) {
            sk = sk.trim();
            if (sk.startsWith("DF")) {
                try {
                    int num = Integer.parseInt(sk.substring(2));
                    lvl = Math.max(lvl, num + 1);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String raceName = getRaceName(race);
        String className = getClassName(dbcclass);

        // LEE del archivo via DBCConfigManager
        double multiplo = DBCConfigManager.getBodyMultiplier(raceName, className);

        int outputExtra = (int) ((lvlWIL * multiplo) * (lvl / 100.0));
        int previousMax = (int) (lvlWIL * multiplo);
        return (int) (previousMax * level);
    }

    public static int getKiMax(IDBCPlayer idbcPlayer) {
        return getKiMax(idbcPlayer, 1.0);
    }

    public static int getKiMax(IDBCPlayer idbcPlayer, double level) {
        int race = idbcPlayer.getRace();
        int dbcclass = idbcPlayer.getDBCClass();
        int kiMax = 0;
        int lvlWIL = General.getSTAT("SPI",Main.instance.getServer().getPlayer(idbcPlayer.getName()));
        String[] skills = idbcPlayer.getNbt().getCompound("PlayerPersisted")
                .getString("jrmcSSlts").split(",");
        int lvl = 0;
        for (String sk : skills) {
            sk = sk.trim();
            if (sk.startsWith("KB")) {
                try {
                    int num = Integer.parseInt(sk.substring(2));
                    lvl = Math.max(lvl, num + 1);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String raceName = getRaceName(race);
        String className = getClassName(dbcclass);

        // LEE del archivo via DBCConfigManager
        double multiplo = DBCConfigManager.getEnergyPoolMultiplier(raceName, className);

        int outputExtra = (int) ((lvlWIL * multiplo) * (lvl / 100.0));
        int previousMax = (int) (lvlWIL * multiplo) + outputExtra;

        return (int) (previousMax * level);
    }

    public static int getMaxStamina(IDBCPlayer idbcPlayer) {
        return getMaxStamina(idbcPlayer, 1.0);
    }

    public static int getMaxStamina(IDBCPlayer idbcPlayer, double level) {
        int race = idbcPlayer.getRace();
        int dbcclass = idbcPlayer.getDBCClass();

        int lvlCON = General.getSTAT("CON", Main.instance.getServer().getPlayer(idbcPlayer.getName()));

        String[] skills = idbcPlayer.getNbt().getCompound("PlayerPersisted")
                .getString("jrmcSSlts").split(",");
        int lvl = 0;
        for (String sk : skills) {
            sk = sk.trim();
            if (sk.startsWith("DF")) {
                try {
                    int num = Integer.parseInt(sk.substring(2));
                    lvl = Math.max(lvl, num + 1);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String raceName = getRaceName(race);
        String className = getClassName(dbcclass);

        // LEE del archivo via DBCConfigManager
        double multiplo = DBCConfigManager.getStaminaMultiplier(raceName, className);

        int outputExtra = (int) ((lvlCON * multiplo) * (lvl / 100.0));
        int previousMax = (int) (lvlCON * multiplo) + outputExtra;

        return (int) (previousMax * level);
    }

    private static String getRaceName(int race) {
        switch (race) {
            case DBCRace.HUMAN:
                return "human";
            case DBCRace.SAIYAN:
                return "saiyan";
            case DBCRace.HALFSAIYAN:
                return "half-saiyan";
            case DBCRace.NAMEKIAN:
                return "namekian";
            case DBCRace.ARCOSIAN:
                return "arcosian";
            case DBCRace.MAJIN:
                return "majin";
            default:
                return "human";
        }
    }

    private static String getClassName(int dbcClass) {
        if (dbcClass == DBCClass.Spiritualist) {
            return "Spiritualist";
        } else if (dbcClass == DBCClass.Warrior) {
            return "Warrior";
        } else {
            return "MartialArtist";
        }
    }
}