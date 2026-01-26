package org.example.tools.ci;
import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.example.Main;
import org.example.commands.items.RegisterItem;
import org.example.tools.General;
import java.util.*;
import static org.example.events.CustomArmor.playerArmorBonus;

public class CustomManager {

    public static void applyArmorBonus(Player player){
        PlayerInventory inventory = player.getInventory();
        for (ItemStack armor : inventory.getArmorContents()){
            RegisterItem item = new RegisterItem();
            if (armor == null) continue;
            if (armor.getTypeId() == Material.AIR.getId()) continue;
            CustomArmor ci  = item.toItemCustom(armor);
            if (ci != null){
                IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());
                ci.getValueByStat().forEach((k, v) -> {
                    String operation = ci.getOperation().get(k);
                    Set<String> bonuses = (!playerArmorBonus.containsKey(player.getUniqueId())) ? new HashSet<>() :
                            playerArmorBonus.get(player.getUniqueId());
                    bonuses.add(ci.getId());
                    playerArmorBonus.put(player.getUniqueId(), bonuses);
                    try {
                        idbcPlayer.addBonusAttribute(General.STATS_MAP.get(k.toUpperCase()), ci.getId(),
                                operation, v);
                    } catch (NullPointerException ignored) {
                    }
                });
            }
        }
    }
    public static void effectsTask(){
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player onlinePlayer : Main.instance.getServer().getOnlinePlayers()) {
                    for (ItemStack armorContent : onlinePlayer.getInventory().getArmorContents()) {
                        RegisterItem registerItem = new RegisterItem();
                        CustomArmor ca = registerItem.toItemCustom(armorContent);
                        ca.getEffects().forEach((k,v)->{
                            if (k.equalsIgnoreCase("HEALTHREGEN")){
                                IDBCPlayer idbcPlayer = General.getDBCPlayer(onlinePlayer.getName());
                                idbcPlayer.setHP((int) (v * idbcPlayer.getBody()));
                            }
                            if (k.equalsIgnoreCase("KIREGEN")){
                                IDBCPlayer idbcPlayer = General.getDBCPlayer(onlinePlayer.getName());
                                idbcPlayer.setKi((int) (v * idbcPlayer.getKi()));
                            }
                            if (k.equalsIgnoreCase("STAMINAREGEN")){
                                IDBCPlayer idbcPlayer = General.getDBCPlayer(onlinePlayer.getName());
                                idbcPlayer.setStamina((int) (v * idbcPlayer.getStamina()));
                            }
                        });
                    }
                }
            }
        };
        runnable.runTaskTimer(Main.instance,1,1);
    }
    
    public static void armorTask() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Main.instance.getServer().getOnlinePlayers()) {
                    List<String> currentArmorId = new ArrayList<>();
                    RegisterItem registerItem = new RegisterItem();
                    for (ItemStack armorContent : player.getInventory().getArmorContents()) {
                        if (armorContent == null) continue;
                        if (armorContent.getTypeId() == Material.AIR.getId()) continue;
                        if (registerItem.isCustom(armorContent)){
                           CustomArmor ci = registerItem.toItemCustom(armorContent);
                           currentArmorId.add(ci.getId());
                        }
                    }
                    applyArmorBonus(player);
                    if (playerArmorBonus.containsKey(player.getUniqueId())){
                       Set<String> activeBonus = playerArmorBonus.get(player.getUniqueId());
                        for (String bonus : activeBonus) {
                            if (!currentArmorId.contains(bonus)){
                                IDBCPlayer idbcPlayer = General.getDBCPlayer(player.getName());
                                idbcPlayer.removeBonusAttribute("strength", bonus);
                                idbcPlayer.removeBonusAttribute("constitution", bonus);
                                idbcPlayer.removeBonusAttribute("dexterity", bonus);
                                idbcPlayer.removeBonusAttribute("willpower", bonus);
                                idbcPlayer.removeBonusAttribute("mind", bonus);
                            }
                        }
                    }
                }

            }
        };
        runnable.runTaskTimer(Main.instance,20L,20L);
    }
}
