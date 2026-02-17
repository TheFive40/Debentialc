package org.debentialc.claims.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class TerrainCommand extends BaseCommand {

    @Command(name = "terrain", aliases = {"terrain", "t"}, permission = ClaimsPermissions.TERRAIN_INFO)
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        String sub = command.getArgs(0).toLowerCase();

        switch (sub) {
            case "create":
                handleCreate(player, command);
                break;
            case "price":
                handlePrice(player, command);
                break;
            case "commit":
                handleCommit(player, command);
                break;
            case "info":
                handleInfo(player, command);
                break;
            case "member":
                handleMember(player, command);
                break;
            case "kick":
                handleKick(player, command);
                break;
            case "transfer":
                handleTransfer(player, command);
                break;
            case "sell":
                handleSell(player, command);
                break;
            case "list":
                handleList(player);
                break;
            case "delete":
                handleDelete(player, command);
                break;
            case "dissolve":
                handleDissolve(player, command);
                break;
            default:
                sendHelp(player);
                break;
        }
    }

    private void handleCreate(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_CREATE)) {
            player.sendMessage(CC.translate("&cNo tienes permiso para crear terrenos."));
            return;
        }
        if (command.length() < 3) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain create <id> <chunks>"));
            return;
        }
        String id = command.getArgs(1);
        int chunks;
        try {
            chunks = Integer.parseInt(command.getArgs(2));
            if (chunks < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&7El valor de chunks debe ser un número mayor a 0."));
            return;
        }

        boolean created = TerrainManager.getInstance().createTerrain(id, chunks);
        if (!created) {
            player.sendMessage(CC.translate("&7Ya existe un terreno con la id &f" + id + "&7."));
            return;
        }
        player.sendMessage(CC.translate("&7Terreno &f" + id + " &7creado. Tamaño: &f" + chunks + " &7chunk(s)."));
    }

    private void handlePrice(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_PRICE)) {
            player.sendMessage(CC.translate("&cNo tienes permiso."));
            return;
        }
        if (command.length() < 3) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain price <id> <precio>"));
            return;
        }
        String id = command.getArgs(1);
        double price;
        try {
            price = Double.parseDouble(command.getArgs(2));
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&7Ingresa un precio válido."));
            return;
        }

        boolean ok = TerrainManager.getInstance().setPrice(id, price);
        if (!ok) {
            player.sendMessage(CC.translate("&7No se encontró el terreno &f" + id + "&7."));
            return;
        }

        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain.isCommitted()) TerrainManager.getInstance().updateSign(terrain);

        player.sendMessage(CC.translate("&7Precio del terreno &f" + id + " &7actualizado a &f$" + (int) price + "&7."));
    }

    private void handleCommit(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_COMMIT)) {
            player.sendMessage(CC.translate("&cNo tienes permiso."));
            return;
        }
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain commit <id>"));
            return;
        }
        String id = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No se encontró el terreno &f" + id + "&7."));
            return;
        }
        if (terrain.isCommitted()) {
            player.sendMessage(CC.translate("&7Este terreno ya fue generado."));
            return;
        }

        String collision = TerrainManager.getInstance().getCollisionId(id, player, terrain);
        if (collision != null) {
            int buffer = TerrainManager.getInstance().getBuffer();
            player.sendMessage(CC.translate("&cNo se puede generar el terreno aquí."));
            player.sendMessage(CC.translate("&7Se choca con el terreno &f" + collision + " &7o está a menos de &f" + buffer + " &7bloques de distancia."));
            return;
        }

        boolean ok = TerrainManager.getInstance().commitTerrain(id, player);
        if (!ok) {
            player.sendMessage(CC.translate("&7No se pudo generar el terreno."));
            return;
        }
        player.sendMessage(CC.translate("&7Terreno &f" + id + " &7generado en tu posición actual."));
    }

    private void handleInfo(Player player, CommandArgs command) {
        Terrain terrain;
        if (command.length() >= 2) {
            terrain = TerrainManager.getInstance().getTerrain(command.getArgs(1));
        } else {
            terrain = TerrainManager.getInstance().getTerrainAt(player.getLocation());
        }
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No hay ningún terreno aquí o el id no existe."));
            return;
        }

        player.sendMessage(CC.translate("&8&m                                    "));
        player.sendMessage(CC.translate("  &7Terreno &f" + terrain.getId()));
        player.sendMessage(CC.translate("  &7Tamaño: &f" + terrain.getChunks() + " chunk(s) &7(" + terrain.getSizeInBlocks() + "x" + terrain.getSizeInBlocks() + ")"));
        player.sendMessage(CC.translate("  &7Precio: &f$" + (int) terrain.getPrice()));
        if (terrain.hasOwner()) {
            player.sendMessage(CC.translate("  &7Propietario: &f" + terrain.getOwnerName()));
        } else {
            player.sendMessage(CC.translate("  &7Estado: &aEn venta"));
        }
        if (!terrain.getMembers().isEmpty()) {
            player.sendMessage(CC.translate("  &7Miembros: &f" + terrain.getMembers().size()));
        }
        player.sendMessage(CC.translate("&8&m                                    "));
    }

    private void handleMember(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_MEMBER)) {
            player.sendMessage(CC.translate("&cNo tienes permiso."));
            return;
        }
        if (command.length() < 4) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain member <id> <jugador> <build|break|containers|all>"));
            return;
        }
        String id = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No se encontró el terreno."));
            return;
        }
        if (!terrain.isOwner(player.getUniqueId()) && !player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
            player.sendMessage(CC.translate("&7No eres el propietario de este terreno."));
            return;
        }

        String targetName = command.getArgs(2);
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(CC.translate("&7El jugador &f" + targetName + " &7no está en línea."));
            return;
        }

        String roleStr = command.getArgs(3).toUpperCase();
        Terrain.MemberRole role;
        try {
            role = Terrain.MemberRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(CC.translate("&7Rol inválido. Usa: &fbuild, break, containers, all"));
            return;
        }

        TerrainManager.getInstance().addMember(terrain, target.getUniqueId(), target.getName(), role);
        player.sendMessage(CC.translate("&f" + target.getName() + " &7ahora tiene permiso &f" + roleStr.toLowerCase() + " &7en el terreno &f" + id + "&7."));
        target.sendMessage(CC.translate("&7Se te otorgó permiso &f" + roleStr.toLowerCase() + " &7en el terreno &f" + id + "&7."));
    }

    private void handleKick(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_KICK)) {
            player.sendMessage(CC.translate("&cNo tienes permiso."));
            return;
        }
        if (command.length() < 3) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain kick <id> <jugador>"));
            return;
        }
        String id = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No se encontró el terreno."));
            return;
        }
        if (!terrain.isOwner(player.getUniqueId()) && !player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
            player.sendMessage(CC.translate("&7No eres el propietario de este terreno."));
            return;
        }

        String targetName = command.getArgs(2);
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : null;

        if (targetUUID == null) {
            player.sendMessage(CC.translate("&7El jugador &f" + targetName + " &7no está en línea."));
            return;
        }
        if (!terrain.isMember(targetUUID)) {
            player.sendMessage(CC.translate("&f" + targetName + " &7no es miembro de este terreno."));
            return;
        }

        TerrainManager.getInstance().removeMember(terrain, targetUUID);
        player.sendMessage(CC.translate("&f" + targetName + " &7fue removido del terreno &f" + id + "&7."));
        if (target != null) {
            target.sendMessage(CC.translate("&7Se te removió del terreno &f" + id + "&7."));
        }
    }

    private void handleTransfer(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_TRANSFER)) {
            player.sendMessage(CC.translate("&cNo tienes permiso."));
            return;
        }
        if (command.length() < 3) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain transfer <id> <jugador>"));
            return;
        }
        String id = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No se encontró el terreno."));
            return;
        }
        if (!terrain.isOwner(player.getUniqueId()) && !player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
            player.sendMessage(CC.translate("&7No eres el propietario de este terreno."));
            return;
        }

        String targetName = command.getArgs(2);
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(CC.translate("&7El jugador &f" + targetName + " &7no está en línea."));
            return;
        }

        TerrainManager.getInstance().transferOwner(terrain, target.getUniqueId(), target.getName());
        player.sendMessage(CC.translate("&7Terreno &f" + id + " &7transferido a &f" + target.getName() + "&7."));
        target.sendMessage(CC.translate("&7Ahora eres propietario del terreno &f" + id + "&7."));
    }

    private void handleSell(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_SELL)) {
            player.sendMessage(CC.translate("&cNo tienes permiso."));
            return;
        }
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain sell <id>"));
            return;
        }
        String id = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No se encontró el terreno."));
            return;
        }
        if (!terrain.isOwner(player.getUniqueId()) && !player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
            player.sendMessage(CC.translate("&7No eres el propietario de este terreno."));
            return;
        }
        if (!terrain.hasOwner()) {
            player.sendMessage(CC.translate("&7Este terreno ya está a la venta."));
            return;
        }

        double refund = terrain.getPrice() / 2;
        terrain.setOwner(null);
        terrain.setOwnerName(null);
        terrain.getMembers().clear();
        TerrainManager.getInstance().save(terrain);
        TerrainManager.getInstance().updateSign(terrain);

        if (TerrainManager.getInstance().getEconomy() != null && refund > 0) {
            TerrainManager.getInstance().getEconomy().depositPlayer(player.getName(), refund);
            player.sendMessage(CC.translate("&7Terreno puesto a la venta. Reembolso: &f$" + (int) refund + "&7."));
        } else {
            player.sendMessage(CC.translate("&7Terreno &f" + id + " &7puesto a la venta."));
        }
    }

    private void handleList(Player player) {
        Map<String, Terrain> all = TerrainManager.getInstance().getAll();
        if (all.isEmpty()) {
            player.sendMessage(CC.translate("&7No hay terrenos registrados."));
            return;
        }
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7Terrenos registrados:"));
        for (Terrain terrain : all.values()) {
            String state = terrain.hasOwner() ? "&f" + terrain.getOwnerName() : "&aEn venta";
            String committed = terrain.isCommitted() ? "" : " &8(sin generar)";
            player.sendMessage(CC.translate("  &7- &f" + terrain.getId() + " &8| " + state + committed));
        }
        player.sendMessage(CC.translate("&8&m                                   "));
    }

    private void handleDelete(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_DELETE)) {
            player.sendMessage(CC.translate("&cNo tienes permiso. Solo administradores pueden eliminar terrenos."));
            return;
        }
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain delete <id>"));
            return;
        }
        String id = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No se encontró el terreno &f" + id + "&7."));
            return;
        }

        boolean deleted = TerrainManager.getInstance().deleteTerrain(id);
        if (!deleted) {
            player.sendMessage(CC.translate("&7No se pudo eliminar el terreno &f" + id + "&7."));
            return;
        }
        player.sendMessage(CC.translate("&7Terreno &f" + id + " &7eliminado. Los bordes fueron removidos del mundo."));
    }

    private void handleDissolve(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.TERRAIN_DISSOLVE)) {
            player.sendMessage(CC.translate("&cNo tienes permiso. Solo administradores pueden disolver terrenos."));
            return;
        }
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/terrain dissolve <id>"));
            return;
        }
        String id = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(id);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7No se encontró el terreno &f" + id + "&7."));
            return;
        }
        if (!terrain.hasOwner()) {
            player.sendMessage(CC.translate("&7El terreno &f" + id + " &7ya está sin propietario."));
            return;
        }

        String prevOwner = terrain.getOwnerName();
        boolean dissolved = TerrainManager.getInstance().dissolveTerrain(id);
        if (!dissolved) {
            player.sendMessage(CC.translate("&7No se pudo disolver el terreno &f" + id + "&7."));
            return;
        }
        player.sendMessage(CC.translate("&7Terreno &f" + id + " &7disuelto. El propietario &f" + prevOwner + " &7fue removido. El terreno vuelve a estar en venta."));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7Comandos de Terreno"));
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &f/terrain create <id> <chunks>"));
        player.sendMessage(CC.translate("  &f/terrain price <id> <precio>"));
        player.sendMessage(CC.translate("  &f/terrain commit <id>"));
        player.sendMessage(CC.translate("  &f/terrain info [id]"));
        player.sendMessage(CC.translate("  &f/terrain member <id> <jugador> <rol>"));
        player.sendMessage(CC.translate("  &f/terrain kick <id> <jugador>"));
        player.sendMessage(CC.translate("  &f/terrain transfer <id> <jugador>"));
        player.sendMessage(CC.translate("  &f/terrain sell <id>"));
        player.sendMessage(CC.translate("  &f/terrain list"));
        player.sendMessage(CC.translate("  &f/terrain delete <id> &8(Admin)"));
        player.sendMessage(CC.translate("  &f/terrain dissolve <id> &8(Admin)"));
        player.sendMessage(CC.translate("&8&m                                   "));
    }
}