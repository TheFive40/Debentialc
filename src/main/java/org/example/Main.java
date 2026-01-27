package org.example;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.tools.ClassesRegistration;
import org.example.tools.ci.CustomManager;
import org.example.tools.commands.CommandFramework;
import org.example.tools.storage.CustomArmorStorage;
import org.example.tools.storage.CustomItemStorage;
import org.example.commands.items.RegisterItem;
import org.example.commands.items.CustomItemCommand;

import java.io.*;

import static org.example.tools.ci.CustomManager.effectsTask;
import static org.example.tools.config.DBCConfigManager.loadAllConfigs;

@Getter
public class Main extends JavaPlugin {

    private final CommandFramework commandFramework = new CommandFramework ( this );

    private final ClassesRegistration classesRegistration = new ClassesRegistration ( );

    static {
        String ruta1 = System.getProperty ( "user.dir" ) + File.separator + "plugins";
        File file = new File ( ruta1, "Debentialc" );
        file.mkdir ( );
    }
    public static Main instance;

    @Override
    public void onEnable () {
        instance = this;
        System.out.println ( "Plugin successfully enabled" );
        System.out.println ( "Version: 1.1.5 " );
        System.out.println ( "By DelawareX" );

        classesRegistration.loadCommands ( "org.example.commands.items" );
        classesRegistration.loadListeners ( "org.example.events" );

        CustomArmorStorage customArmorStorage = new CustomArmorStorage();
        RegisterItem.items.putAll(customArmorStorage.loadAllArmors());
        System.out.println("[Debentialc] Armaduras cargadas: " + RegisterItem.items.size());

        CustomItemStorage customItemStorage = new CustomItemStorage();
        CustomItemCommand.items.putAll(customItemStorage.loadAllItems());
        System.out.println("[Debentialc] Items cargados: " + CustomItemCommand.items.size());

        CustomManager.armorTask();
        effectsTask();

        loadAllConfigs();
    }

    @Override
    public void onDisable () {
        CustomArmorStorage customArmorStorage = new CustomArmorStorage();
        for (String armorId : RegisterItem.items.keySet()) {
            customArmorStorage.saveArmor(RegisterItem.items.get(armorId));
        }
        System.out.println("[Debentialc] Armaduras guardadas: " + RegisterItem.items.size());

        CustomItemStorage customItemStorage = new CustomItemStorage();
        for (String itemId : CustomItemCommand.items.keySet()) {
            customItemStorage.saveItem(CustomItemCommand.items.get(itemId));
        }
        System.out.println("[Debentialc] Items guardados: " + CustomItemCommand.items.size());
    }
}