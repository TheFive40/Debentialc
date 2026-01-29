package org.example;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.example.tools.ClassesRegistration;
import org.example.tools.ci.CustomManager;
import org.example.tools.commands.CommandFramework;
import org.example.tools.fragments.FragmentBonusIntegration;
import org.example.tools.storage.CustomArmorStorage;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.example.events.CustomArmor.playerArmorBonus;
import static org.example.tools.ci.CustomManager.effectsTask;
import static org.example.tools.ci.CustomManager.removeBonusFromPlayer;
import static org.example.tools.config.DBCConfigManager.loadAllConfigs;
import static sun.audio.AudioPlayer.player;

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
        classesRegistration.loadCommands("org.example.commands.items");
        classesRegistration.loadListeners("org.example.events");
        CustomManager.armorTask();
        effectsTask();
        new CustomArmorStorage();
        loadAllConfigs();
        armorTask();
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
        runnable.runTaskTimer(Main.instance, 20L, 20L);
    }

    @Override
    public void onDisable() {

    }


}