package org.example.tools.stats;

import kamkeel.npcdbc.constants.DBCClass;
import kamkeel.npcdbc.constants.DBCRace;
import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.example.tools.General;
import org.example.tools.config.DBCConfigManager;

public class StatsCalculator {

    public static int getMaxHealth(IDBCPlayer idbcPlayer) {
        return getMaxHealth(idbcPlayer, 1.0);
    }

    public static int getMaxHealth(IDBCPlayer idbcPlayer, double level) {
        try {
            int race = idbcPlayer.getRace();
            int dbcclass = idbcPlayer.getDBCClass();

            Player player = Bukkit.getPlayer(idbcPlayer.getName());
            if (player == null) return 0;

            int lvlCON = General.getSTAT("CON", player);

            String[] skills = idbcPlayer.getNbt().getCompound("PlayerPersisted")
                    .getString("jrmcSSlts").split(",");
            int skillLevel = 0;
            for (String sk : skills) {
                sk = sk.trim();
                if (sk.startsWith("DF")) {
                    try {
                        int num = Integer.parseInt(sk.substring(2));
                        skillLevel = Math.max(skillLevel, num + 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            String raceName = getRaceName(race);
            String className = getClassName(dbcclass);

            double bodyMultiplier = DBCConfigManager.getBodyMultiplier(raceName, className);

            int previousMax = (int) (lvlCON * bodyMultiplier);
            int skillBonus = (int) ((lvlCON * bodyMultiplier) * (skillLevel / 100.0));
            int totalMax = previousMax + skillBonus;

            return (int) (totalMax * level);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getKiMax(IDBCPlayer idbcPlayer) {
        return getKiMax(idbcPlayer, 1.0);
    }

    public static int getKiMax(IDBCPlayer idbcPlayer, double level) {
        try {
            int race = idbcPlayer.getRace();
            int dbcclass = idbcPlayer.getDBCClass();

            Player player = Bukkit.getPlayer(idbcPlayer.getName());
            if (player == null) return 0;

            int lvlSPI = General.getSTAT("SPI", player);

            String[] skills = idbcPlayer.getNbt().getCompound("PlayerPersisted")
                    .getString("jrmcSSlts").split(",");
            int skillLevel = 0;
            for (String sk : skills) {
                sk = sk.trim();
                if (sk.startsWith("KB")) {
                    try {
                        int num = Integer.parseInt(sk.substring(2));
                        skillLevel = Math.max(skillLevel, num + 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            String raceName = getRaceName(race);
            String className = getClassName(dbcclass);

            double energyPoolMultiplier = DBCConfigManager.getEnergyPoolMultiplier(raceName, className);

            int previousMax = (int) (lvlSPI * energyPoolMultiplier);
            int skillBonus = (int) ((lvlSPI * energyPoolMultiplier) * (skillLevel / 100.0));
            int totalMax = previousMax + skillBonus;

            return (int) (totalMax * level);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getMaxStamina(IDBCPlayer idbcPlayer) {
        return getMaxStamina(idbcPlayer, 1.0);
    }

    public static int getMaxStamina(IDBCPlayer idbcPlayer, double level) {
        try {
            int race = idbcPlayer.getRace();
            int dbcclass = idbcPlayer.getDBCClass();

            Player player = Bukkit.getPlayer(idbcPlayer.getName());
            if (player == null) return 0;

            int lvlCON = General.getSTAT("CON", player);

            String[] skills = idbcPlayer.getNbt().getCompound("PlayerPersisted")
                    .getString("jrmcSSlts").split(",");
            int skillLevel = 0;
            for (String sk : skills) {
                sk = sk.trim();
                if (sk.startsWith("DF")) {
                    try {
                        int num = Integer.parseInt(sk.substring(2));
                        skillLevel = Math.max(skillLevel, num + 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            String raceName = getRaceName(race);
            String className = getClassName(dbcclass);

            double staminaMultiplier = DBCConfigManager.getStaminaMultiplier(raceName, className);
            if (staminaMultiplier <= 0) {
                staminaMultiplier = 3.5;
            }

            int previousMax = (int) (lvlCON * staminaMultiplier);
            int skillBonus = (int) ((lvlCON * staminaMultiplier) * (skillLevel / 100.0));
            int totalMax = previousMax + skillBonus;

            return (int) (totalMax * level);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
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