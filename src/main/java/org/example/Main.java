package org.example;
import lombok.Getter;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IDBCPlayer;
import noppes.npcs.scripted.NpcAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.tools.ClassesRegistration;
import org.example.tools.ci.CustomManager;
import org.example.tools.commands.CommandFramework;
import java.io.*;

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
        CustomManager.armorTask();
    }

    @Override
    public void onDisable () {
    }


}