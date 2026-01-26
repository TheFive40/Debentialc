package org.example.commands.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.tools.CC;
import org.example.tools.ci.CustomItem;
import org.example.tools.commands.BaseCommand;
import org.example.tools.commands.Command;
import org.example.tools.commands.CommandArgs;
import org.example.tools.permissions.Permissions;
import org.example.tools.storage.CustomItemStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CustomItemCommand extends BaseCommand {
    public static ConcurrentHashMap<String, CustomItem> items = new ConcurrentHashMap<>();
    private static CustomItemStorage itemStorage;
    private Player player = null;

    public CustomItemCommand() {
        super();
        if (itemStorage == null) {
            itemStorage = new CustomItemStorage();
            // Cargar items guardados
            items.putAll(itemStorage.loadAllItems());
        }
    }

    @Command(name = "customitem", aliases = {"customitem", "ci"}, permission = Permissions.COMMAND + "ci")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        player = command.getPlayer();
        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        try {
            String arg0 = command.getArgs(0);
            String arg1 = command.length() > 1 ? command.getArgs(1) : "";

            switch (arg0.toLowerCase()) {
                case "create":
                    if (command.length() < 2) {
                        player.sendMessage(CC.translate("&cUso: /ci create <id>"));
                        return;
                    }
                    create(arg1);
                    break;

                case "remove":
                case "delete":
                    if (command.length() < 2) {
                        player.sendMessage(CC.translate("&cUso: /ci delete <id>"));
                        return;
                    }
                    delete(arg1);
                    break;

                case "percentage":
                    if (command.length() < 4) {
                        player.sendMessage(CC.translate("&cUso: /ci percentage <id> <valor> <stat>"));
                        return;
                    }
                    addPercentage(arg1, command.getArgs(3), Double.parseDouble(command.getArgs(2)));
                    break;

                case "plus":
                    if (command.length() < 4) {
                        player.sendMessage(CC.translate("&cUso: /ci plus <id> <valor> <stat>"));
                        return;
                    }
                    plus(arg1, command.getArgs(3), Double.parseDouble(command.getArgs(2)));
                    break;

                case "less":
                    if (command.length() < 4) {
                        player.sendMessage(CC.translate("&cUso: /ci less <id> <valor> <stat>"));
                        return;
                    }
                    less(arg1, command.getArgs(3), Double.parseDouble(command.getArgs(2)));
                    break;

                case "addline":
                    StringBuilder text = new StringBuilder();
                    for (int i = 1; i < command.getArgs().length; i++) {
                        text.append(command.getArgs(i)).append(" ");
                    }
                    addLine(text.toString().trim());
                    break;

                case "setline":
                    if (command.length() < 3) {
                        player.sendMessage(CC.translate("&cUso: /ci setline <línea> <texto>"));
                        return;
                    }
                    StringBuilder loreline = new StringBuilder();
                    int line = Integer.parseInt(command.getArgs(1));
                    for (int i = 2; i < command.getArgs().length; i++) {
                        loreline.append(command.getArgs(i)).append(" ");
                    }
                    setLine(line, loreline.toString().trim());
                    break;

                case "l":
                case "list":
                    int page = command.length() > 1 ? Integer.parseInt(command.getArgs(1)) : 1;
                    sendList(player, page);
                    break;

                case "rename":
                    StringBuilder newName = new StringBuilder();
                    for (int i = 1; i < command.getArgs().length; i++) {
                        newName.append(command.getArgs(i)).append(" ");
                    }
                    rename(newName.toString().trim());
                    break;

                case "effect":
                    if (command.length() < 3) {
                        player.sendMessage(CC.translate("&cUso: /ci effect <tipo> <valor>"));
                        player.sendMessage(CC.translate("&7Tipos: HEALTHREGEN, KIREGEN, STAMINAREGEN"));
                        return;
                    }
                    addEffect(command.getArgs(1).toUpperCase(), Double.parseDouble(command.getArgs(2)));
                    break;

                case "info":
                    showInfo();
                    break;

                case "help":
                default:
                    sendHelp(player);
                    break;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cError: Número inválido"));
            sendHelp(player);
        } catch (ArrayIndexOutOfBoundsException exception) {
            sendHelp(player);
        }
    }

    public void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
        player.sendMessage(CC.translate("&c&lCustom Item - Help Menu"));
        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
        player.sendMessage(CC.translate("&fUsa &a/ci <subcomando> &fpara gestionar items."));
        player.sendMessage(" ");

        player.sendMessage(CC.translate("&e/ci create <id> &7- Crear un item custom"));
        player.sendMessage(CC.translate("&e/ci delete <id> &7- Eliminar un item registrado"));
        player.sendMessage(CC.translate("&e/ci addline <texto> &7- Agregar lore al item en mano"));
        player.sendMessage(CC.translate("&e/ci setline <línea> <texto> &7- Editar una línea de lore"));
        player.sendMessage(CC.translate("&e/ci rename <texto> &7- Cambiar el nombre del item"));

        player.sendMessage(" ");
        player.sendMessage(CC.translate("&e/ci plus <id> <valor> <stat> &7- Bonus aditivo (+)"));
        player.sendMessage(CC.translate("&e/ci less <id> <valor> <stat> &7- Bonus sustractivo (-)"));
        player.sendMessage(CC.translate("&e/ci percentage <id> <valor> <stat> &7- Bonus multiplicativo (*)"));
        player.sendMessage(CC.translate("&e/ci effect <tipo> <valor> &7- Agregar efecto especial (%)"));

        player.sendMessage(" ");
        player.sendMessage(CC.translate("&e/ci list [página] &7- Listar todos los items"));
        player.sendMessage(CC.translate("&e/ci info &7- Ver info del item en mano"));

        player.sendMessage(" ");
        player.sendMessage(CC.translate("&7Stats: &fstr&7, &fcon&7, &fdex&7, &fwill&7, &fmnd"));
        player.sendMessage(CC.translate("&7Efectos: &fHEALTHREGEN&7, &fKIREGEN&7, &fSTAMINAREGEN"));
        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
    }

    public void create(String id) {
        if (items.containsKey(id)) {
            player.sendMessage(CC.translate("&cYa existe un item registrado con este ID"));
            return;
        }

        ItemStack item = player.getItemInHand();
        if (item == null || item.getTypeId() == 0) {
            player.sendMessage(CC.translate("&cDebes sostener un item en la mano"));
            return;
        }

        if (item.getItemMeta() == null) {
            player.sendMessage(CC.translate("&cEste item no tiene metadatos (nombre/lore)"));
            return;
        }

        CustomItem customItem = new CustomItem()
                .setId(id)
                .setMaterial(item.getTypeId())
                .setLore(item.getItemMeta().getLore())
                .setDisplayName(item.getItemMeta().getDisplayName());

        items.put(id, customItem);
        itemStorage.saveItem(customItem);

        player.sendMessage(CC.translate("&aItem creado correctamente"));
        player.sendMessage(CC.translate("&7ID: &f" + id));
    }

    public void addPercentage(String id, String stat, double value) {
        if (!items.containsKey(id)) {
            player.sendMessage(CC.translate("&cItem no encontrado"));
            return;
        }
        CustomItem item = items.get(id);
        item.setOperation("*", stat).setBonusStat(stat, value);
        itemStorage.saveItem(item);
        player.sendMessage(CC.translate("&aBonificación correctamente aplicada"));
    }

    public void plus(String id, String stat, double value) {
        if (!items.containsKey(id)) {
            player.sendMessage(CC.translate("&cItem no encontrado"));
            return;
        }
        CustomItem item = items.get(id);
        item.setOperation("+", stat).setBonusStat(stat, value);
        itemStorage.saveItem(item);
        player.sendMessage(CC.translate("&aBonificación correctamente aplicada"));
    }

    public void less(String id, String stat, double value) {
        if (!items.containsKey(id)) {
            player.sendMessage(CC.translate("&cItem no encontrado"));
            return;
        }
        CustomItem item = items.get(id);
        item.setOperation("-", stat).setBonusStat(stat, value);
        itemStorage.saveItem(item);
        player.sendMessage(CC.translate("&aBonificación correctamente aplicada"));
    }

    private void delete(String id) {
        if (!items.containsKey(id)) {
            player.sendMessage(CC.translate("&cItem no encontrado"));
            return;
        }
        items.remove(id);
        itemStorage.deleteItem(id);
        player.sendMessage(CC.translate("&aItem eliminado correctamente"));
    }

    private void addLine(String text) {
        CustomItem ci = toItemCustom(player.getItemInHand());
        if (ci == null) {
            player.sendMessage(CC.translate("&cDebes sostener un item custom en la mano"));
            return;
        }

        ItemStack item = player.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        lore.add(CC.translate(text));
        meta.setLore(lore);
        item.setItemMeta(meta);

        ci.setLore(lore);
        items.put(ci.getId(), ci);
        itemStorage.saveItem(ci);

        player.sendMessage(CC.translate("&aLore actualizado correctamente"));
    }

    private void setLine(int line, String text) {
        CustomItem ci = toItemCustom(player.getItemInHand());
        if (ci == null) {
            player.sendMessage(CC.translate("&cDebes sostener un item custom en la mano"));
            return;
        }

        List<String> lore = ci.getLore();
        if (lore == null) {
            player.sendMessage(CC.translate("&cEste item no tiene lore"));
            return;
        }

        try {
            lore.set((line - 1), CC.translate(text));
            player.sendMessage(CC.translate("&aLore actualizado correctamente"));
        } catch (IndexOutOfBoundsException e) {
            player.sendMessage(CC.translate("&cNúmero de línea inválido"));
            return;
        }

        ci.setLore(lore);
        ItemStack item = player.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);

        items.put(ci.getId(), ci);
        itemStorage.saveItem(ci);
    }

    private void rename(String newName) {
        CustomItem ci = toItemCustom(player.getItemInHand());
        if (ci == null) {
            player.sendMessage(CC.translate("&cDebes sostener un item custom en la mano"));
            return;
        }

        ci.setDisplayName(CC.translate(newName));
        ItemStack item = player.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CC.translate(newName));
        item.setItemMeta(meta);

        items.put(ci.getId(), ci);
        itemStorage.saveItem(ci);

        player.sendMessage(CC.translate("&aNombre actualizado correctamente"));
    }

    private void addEffect(String effect, double value) {
        CustomItem ci = toItemCustom(player.getItemInHand());
        if (ci == null) {
            player.sendMessage(CC.translate("&cDebes sostener un item custom en la mano"));
            return;
        }

        if (!effect.matches("HEALTHREGEN|KIREGEN|STAMINAREGEN")) {
            player.sendMessage(CC.translate("&cEfecto inválido: HEALTHREGEN, KIREGEN, STAMINAREGEN"));
            return;
        }

        ci.getEffects().put(effect, value);
        items.put(ci.getId(), ci);
        itemStorage.saveItem(ci);

        player.sendMessage(CC.translate("&aEfecto agregado correctamente"));
    }

    private void showInfo() {
        CustomItem ci = toItemCustom(player.getItemInHand());
        if (ci == null) {
            player.sendMessage(CC.translate("&cDebes sostener un item custom en la mano"));
            return;
        }

        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
        player.sendMessage(CC.translate("&c&lInfo del Item"));
        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
        player.sendMessage(CC.translate("&fID: &e" + ci.getId()));
        player.sendMessage(CC.translate("&fNombre: &e" + ci.getDisplayName()));
        player.sendMessage(CC.translate("&fMaterial: &e" + ci.getMaterial()));
        player.sendMessage(CC.translate("&fLore: &e" + (ci.getLore() != null ? ci.getLore().size() : 0) + " líneas"));

        if (!ci.getValueByStat().isEmpty()) {
            player.sendMessage(CC.translate("&f&nStats:"));
            ci.getValueByStat().forEach((stat, value) -> {
                String op = ci.getOperation().getOrDefault(stat, "+");
                player.sendMessage(CC.translate("&7  " + stat + " " + op + value));
            });
        }

        if (!ci.getEffects().isEmpty()) {
            player.sendMessage(CC.translate("&f&nEfectos:"));
            ci.getEffects().forEach((effect, value) -> {
                player.sendMessage(CC.translate("&7  " + effect + ": " + (value * 100) + "%"));
            });
        }

        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
    }

    public boolean isCustom(ItemStack item) {
        if (item == null || item.getTypeId() == 0) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return items.containsValue(
                new CustomItem()
                        .setMaterial(item.getTypeId())
                        .setLore(meta.getLore())
                        .setDisplayName(meta.getDisplayName())
        );
    }

    public ItemStack toItemStack(CustomItem item) {
        ItemStack itemStack = new ItemStack(item.getMaterial());
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(item.getDisplayName());
        meta.setLore(item.getLore());
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public void sendList(Player player, int page) {
        int pageSize = 7;
        List<String> keys = new ArrayList<>(items.keySet());

        if (keys.isEmpty()) {
            player.sendMessage(CC.translate("&cNo hay items custom registrados"));
            return;
        }

        int totalPages = (int) Math.ceil((double) keys.size() / pageSize);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, keys.size());

        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
        player.sendMessage(CC.translate("&c&lItems Custom Registrados"));
        player.sendMessage(CC.translate("&7Página &a" + page + " &7/ &a" + totalPages));
        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));

        for (int i = start; i < end; i++) {
            String id = keys.get(i);
            CustomItem ci = items.get(id);
            player.sendMessage(CC.translate("&e" + id + " &7- &f" + ci.getDisplayName()));
        }

        player.sendMessage(CC.translate("&8&l&m--------------------------------------"));
    }

    public CustomItem toItemCustom(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return null;
        String displayName = itemStack.getItemMeta().getDisplayName();
        List<String> lore = itemStack.getItemMeta().getLore();
        int materialId = itemStack.getTypeId();

        return items.values().stream()
                .filter(e -> e.equals(new CustomItem()
                        .setLore(lore)
                        .setMaterial(materialId)
                        .setDisplayName(displayName)))
                .findFirst()
                .orElse(null);
    }
}