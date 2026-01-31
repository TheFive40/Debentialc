package org.debentialc;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.debentialc.boosters.core.BoosterModule;
import org.debentialc.boosters.core.BoosterSettings;
import org.debentialc.boosters.core.BoosterUtils;
import org.debentialc.boosters.managers.GlobalBoosterManager;
import org.debentialc.boosters.managers.PersonalBoosterManager;
import org.debentialc.boosters.models.PersonalBooster;
import org.debentialc.customitems.tools.ClassesRegistration;
import org.debentialc.customitems.tools.ci.CustomManager;
import org.debentialc.customitems.tools.commands.CommandFramework;
import org.debentialc.customitems.tools.fragments.FragmentBonusIntegration;
import org.debentialc.customitems.tools.storage.CustomArmorStorage;
import java.io.File;
import static org.debentialc.customitems.tools.ci.CustomManager.effectsTask;
import static org.debentialc.customitems.tools.config.DBCConfigManager.loadAllConfigs;

@Getter
public class Main extends JavaPlugin {

    private final CommandFramework commandFramework = new CommandFramework(this);

    private final ClassesRegistration classesRegistration = new ClassesRegistration();

    static {
        String ruta1 = System.getProperty("user.dir") + File.separator + "plugins";
        File file = new File(ruta1, "Debentialc");
        file.mkdir();
    }

    public static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        System.out.println("Plugin successfully enabled");
        System.out.println("Version: 1.1.5 ");
        System.out.println("By DelawareX");
        classesRegistration.loadCommands("org.debentialc.ci.commands.items");
        classesRegistration.loadListeners("org.debentialc.ci.events");
        classesRegistration.loadListeners("org.debentialc.boosters.commands");
        classesRegistration.loadListeners("org.debentialc.boosters.events");

        CustomManager.armorTask();
        effectsTask();
        new CustomArmorStorage();
        loadAllConfigs();
        armorTask();
        BoosterModule.initialize(this);
    }

    public static void armorTask() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player onlinePlayer : Main.instance.getServer().getOnlinePlayers()) {
                    FragmentBonusIntegration.applyFragmentBonuses(onlinePlayer);
                }
            }
        };
        runnable.runTaskTimer(Main.instance, 1L, 1L);
    }

    @Override
    public void onDisable() {
        BoosterModule.shutdown();

    }

    public static void exampleUsage() {
        Player player = Bukkit.getPlayer("PlayerName");
        if (player == null) return;

        double globalMult = GlobalBoosterManager.getCurrentMultiplier();
        double personalMult = PersonalBoosterManager.getActiveMultiplier(player.getUniqueId());
        double combined = BoosterUtils.calculateCombinedMultiplier(player.getUniqueId());

        player.sendMessage("Global: " + BoosterUtils.formatMultiplier(globalMult));
        player.sendMessage("Personal: " + BoosterUtils.formatMultiplier(personalMult));
        player.sendMessage("Combined: " + BoosterUtils.formatMultiplier(combined));

        boolean hasGlobal = GlobalBoosterManager.isBoosterActive();
        PersonalBooster active = PersonalBoosterManager.getActiveBooster(player.getUniqueId());

        if (hasGlobal) {
            player.sendMessage("Booster global activo: " +
                    BoosterUtils.formatPercentage(globalMult));
        }

        if (active != null) {
            player.sendMessage("Booster personal activo: " +
                    BoosterUtils.formatPercentage(active.getMultiplier()));
            player.sendMessage("Nivel: " + active.getLevelName());
            player.sendMessage("Tiempo restante: " +
                    BoosterUtils.formatTime(active.getActivationTimeRemaining(900)));
        }
    }

    public static void customizeExample() {
        BoosterSettings.setPersonalBoosterMultiplier(1, 0.15);
        BoosterSettings.setPersonalBoosterMultiplier(2, 0.30);
        BoosterSettings.setPersonalBoosterMultiplier(3, 0.60);
        BoosterSettings.setPersonalBoosterMultiplier(4, 1.20);
        BoosterSettings.setPersonalBoosterMultiplier(5, 2.50);

        BoosterSettings.setRankMultiplier("admin", 3.0);
        BoosterSettings.setRankMultiplier("moderator", 2.0);
        BoosterSettings.setRankMultiplier("vip", 1.5);

        BoosterSettings.setGlobalBoosterDuration(7200);
        BoosterSettings.setPersonalBoosterDuration(1800);
    }

    public static void activateBoosterExample() {
        GlobalBoosterManager.activateBooster(2.5, "AdminName");

        Bukkit.broadcastMessage("§6§lBooster Activado: 2.5x durante 1 hora");

        Bukkit.getScheduler().scheduleSyncDelayedTask(
                Bukkit.getPluginManager().getPlugin("Debentialc"),
                () -> GlobalBoosterManager.deactivateBooster(),
                3600 * 20
        );
    }

    public static void addPersonalBoosterExample() {
        Player player = Bukkit.getPlayer("PlayerName");
        if (player == null) return;

        int level = 4;
        double multiplier = BoosterSettings.getPersonalBoosterMultiplier(level);
        PersonalBooster booster = new PersonalBooster(player.getUniqueId(), level, multiplier);

        PersonalBoosterManager.addBooster(booster);
        player.sendMessage("§aHas recibido un booster personal nivel " + level);
    }

    public static void applyBoosterInCalculation(Player player, double baseValue) {
        double multiplier = BoosterUtils.calculateCombinedMultiplier(player.getUniqueId());
        double result = baseValue * multiplier;

        player.sendMessage("§aValor base: " + baseValue);
        player.sendMessage("§eMultiplicador: " + BoosterUtils.formatMultiplier(multiplier));
        player.sendMessage("§6Resultado: " + result);
    }
}