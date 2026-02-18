package org.debentialc.claims.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.menus.LeaseMenu;
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class LeaseCommand extends BaseCommand {

    @Command(name = "lease", aliases = {"arrendamiento", "arriendo"}, permission = ClaimsPermissions.TERRAIN_INFO)
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() < 1) {
            LeaseMenu.createMainMenu(player).open(player);
            return;
        }

        String sub = command.getArgs(0).toLowerCase();

        switch (sub) {
            case "menu":
                LeaseMenu.createMainMenu(player).open(player);
                break;
            case "offer":
                handleOffer(player, command);
                break;
            case "request":
                handleRequest(player, command);
                break;
            case "accept":
                handleAccept(player, command);
                break;
            case "cancel":
                handleCancel(player, command);
                break;
            case "assign":
                handleAssign(player, command);
                break;
            case "evict":
                handleEvict(player, command);
                break;
            case "pay":
                handlePay(player, command);
                break;
            case "list":
                handleList(player);
                break;
            case "info":
                handleInfo(player, command);
                break;
            default:
                sendHelp(player);
                break;
        }
    }

    private void handleOffer(Player player, CommandArgs command) {
        if (command.length() < 6) {
            player.sendMessage(CC.translate("&7Uso: &f/lease offer <terreno> <jugador> <chunks> <precio> <días>"));
            return;
        }

        String terrainId = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(terrainId);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7Terreno &f" + terrainId + " &7no encontrado."));
            return;
        }
        if (!terrain.isOwner(player.getUniqueId()) && !player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
            player.sendMessage(CC.translate("&7No eres el propietario de ese terreno."));
            return;
        }
        if (terrain.getChunks() < LeaseManager.MIN_CHUNKS_TO_LEASE) {
            player.sendMessage(CC.translate("&7El terreno necesita al menos &f" + LeaseManager.MIN_CHUNKS_TO_LEASE + " chunks &7para poder arrendar."));
            return;
        }
        if (LeaseManager.getInstance().isSubTerrain(terrainId)) {
            player.sendMessage(CC.translate("&7No puedes subarrendar un sub-terreno."));
            return;
        }

        String targetName = command.getArgs(2);
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(CC.translate("&7El jugador &f" + targetName + " &7no está en línea."));
            return;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.translate("&7No puedes ofrecerte un contrato a ti mismo."));
            return;
        }

        int chunks;
        double price;
        int cycleDays;
        try {
            chunks = Integer.parseInt(command.getArgs(3));
            price = Double.parseDouble(command.getArgs(4));
            cycleDays = Integer.parseInt(command.getArgs(5));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&7Valores inválidos. Chunks, precio y días deben ser números."));
            return;
        }

        if (cycleDays < LeaseManager.MIN_CYCLE_DAYS || cycleDays > LeaseManager.MAX_CYCLE_DAYS) {
            player.sendMessage(CC.translate("&7El ciclo debe estar entre &f" + LeaseManager.MIN_CYCLE_DAYS + " &7y &f" + LeaseManager.MAX_CYCLE_DAYS + " &7días."));
            return;
        }
        if (chunks < 1 || chunks >= terrain.getChunks()) {
            player.sendMessage(CC.translate("&7Chunks inválidos. Debe ser entre 1 y " + (terrain.getChunks() - 1) + "."));
            return;
        }
        if (price <= 0) {
            player.sendMessage(CC.translate("&7El precio debe ser mayor a 0."));
            return;
        }

        LeaseContract contract = LeaseManager.getInstance().offerContract(terrain, player, target.getUniqueId(), target.getName(), chunks, price, cycleDays);
        if (contract == null) {
            player.sendMessage(CC.translate("&7No se pudo crear el contrato."));
            return;
        }

        player.sendMessage(CC.translate("&7Oferta enviada a &f" + target.getName() + " &7— &f" + chunks + " chunk(s) &7en &f" + terrainId + " &7por &f$" + (int) price + " &7c/" + cycleDays + " días."));
        target.sendMessage(CC.translate("&8&m                                   "));
        target.sendMessage(CC.translate("  &6&lOferta de Arrendamiento"));
        target.sendMessage(CC.translate("  &7Dueño: &f" + player.getName()));
        target.sendMessage(CC.translate("  &7Terreno: &f" + terrainId + " &8(" + chunks + " chunk(s))"));
        target.sendMessage(CC.translate("  &7Precio: &f$" + (int) price + " &7cada &f" + cycleDays + " días"));
        target.sendMessage(CC.translate("  &7Escribe &f/lease accept " + player.getName() + " &7para aceptar."));
        target.sendMessage(CC.translate("&8&m                                   "));
    }

    private void handleRequest(Player player, CommandArgs command) {
        if (command.length() < 6) {
            player.sendMessage(CC.translate("&7Uso: &f/lease request <terreno> <chunks> <precio> <días> <dueño>"));
            return;
        }

        String terrainId = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(terrainId);
        if (terrain == null) {
            player.sendMessage(CC.translate("&7Terreno &f" + terrainId + " &7no encontrado."));
            return;
        }
        if (!terrain.hasOwner()) {
            player.sendMessage(CC.translate("&7Ese terreno no tiene propietario."));
            return;
        }
        if (terrain.getChunks() < LeaseManager.MIN_CHUNKS_TO_LEASE) {
            player.sendMessage(CC.translate("&7El terreno es muy pequeño (mínimo &f" + LeaseManager.MIN_CHUNKS_TO_LEASE + " chunks&7)."));
            return;
        }

        int chunks;
        double price;
        int cycleDays;
        try {
            chunks = Integer.parseInt(command.getArgs(2));
            price = Double.parseDouble(command.getArgs(3));
            cycleDays = Integer.parseInt(command.getArgs(4));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&7Valores inválidos."));
            return;
        }

        if (cycleDays < LeaseManager.MIN_CYCLE_DAYS || cycleDays > LeaseManager.MAX_CYCLE_DAYS) {
            player.sendMessage(CC.translate("&7El ciclo debe estar entre &f" + LeaseManager.MIN_CYCLE_DAYS + " &7y &f" + LeaseManager.MAX_CYCLE_DAYS + " &7días."));
            return;
        }
        if (chunks < 1 || chunks >= terrain.getChunks()) {
            player.sendMessage(CC.translate("&7Chunks inválidos. Debe ser entre 1 y " + (terrain.getChunks() - 1) + "."));
            return;
        }
        if (price <= 0) {
            player.sendMessage(CC.translate("&7El precio debe ser mayor a 0."));
            return;
        }

        LeaseContract contract = LeaseManager.getInstance().requestContract(
                terrain, player, terrain.getOwner(), terrain.getOwnerName(), chunks, price, cycleDays);
        if (contract == null) {
            player.sendMessage(CC.translate("&7No se pudo crear la solicitud."));
            return;
        }

        player.sendMessage(CC.translate("&7Solicitud enviada al dueño de &f" + terrainId + "&7."));

        Player owner = Bukkit.getPlayerExact(terrain.getOwnerName());
        if (owner != null) {
            owner.sendMessage(CC.translate("&8&m                                   "));
            owner.sendMessage(CC.translate("  &6&lSolicitud de Arrendamiento"));
            owner.sendMessage(CC.translate("  &7Inquilino: &f" + player.getName()));
            owner.sendMessage(CC.translate("  &7Terreno: &f" + terrainId + " &8(" + chunks + " chunk(s))"));
            owner.sendMessage(CC.translate("  &7Precio ofrecido: &f$" + (int) price + " &7cada &f" + cycleDays + " días"));
            owner.sendMessage(CC.translate("  &7Escribe &f/lease accept " + player.getName() + " &7para aceptar."));
            owner.sendMessage(CC.translate("&8&m                                   "));
        }
    }

    private void handleAccept(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease accept <nombre_del_otro_jugador>"));
            return;
        }

        String otherName = command.getArgs(1);
        LeaseContract contract = LeaseManager.getInstance().findPendingByPlayerName(player.getUniqueId(), otherName);

        if (contract == null) {
            player.sendMessage(CC.translate("&7No hay ningún contrato pendiente con &f" + otherName + "&7."));
            return;
        }

        boolean accepted = LeaseManager.getInstance().acceptContract(contract, player);
        if (!accepted) {
            player.sendMessage(CC.translate("&7No se pudo aceptar el contrato."));
            return;
        }

        player.sendMessage(CC.translate("&7Contrato aceptado."));

        boolean playerIsOwner = contract.getOwnerId().equals(player.getUniqueId());

        if (playerIsOwner) {
            player.sendMessage(CC.translate("&7Ahora debes asignar el sub-terreno. Párate en el chunk deseado y escribe:"));
            player.sendMessage(CC.translate("&f/lease assign " + contract.getTenantName()));
            Player tenant = Bukkit.getPlayerExact(contract.getTenantName());
            if (tenant != null) {
                tenant.sendMessage(CC.translate("&7El dueño &f" + player.getName() + " &7aceptó tu solicitud. Espera a que asigne el sub-terreno."));
            }
        } else {
            player.sendMessage(CC.translate("&7Ahora el dueño debe asignar el sub-terreno con &f/lease assign " + player.getName() + "&7."));
            Player owner = Bukkit.getPlayerExact(contract.getOwnerName());
            if (owner != null) {
                owner.sendMessage(CC.translate("&7&f" + player.getName() + " &7aceptó el contrato. Párate en el chunk del sub-terreno y escribe:"));
                owner.sendMessage(CC.translate("&f/lease assign " + player.getName()));
            }
        }
    }

    private void handleAssign(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease assign <nombre_del_inquilino>"));
            player.sendMessage(CC.translate("&7Párate en el chunk donde quieres colocar el sub-terreno."));
            return;
        }

        String tenantName = command.getArgs(1);
        LeaseContract contract = LeaseManager.getInstance().findAwaitingAssignByTenantName(player.getUniqueId(), tenantName);

        if (contract == null) {
            if (player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
                for (LeaseContract c : LeaseManager.getInstance().getAll().values()) {
                    if (c.getStatus() == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN
                            && c.getTenantName().equalsIgnoreCase(tenantName)) {
                        contract = c;
                        break;
                    }
                }
            }
            if (contract == null) {
                player.sendMessage(CC.translate("&7No hay contratos esperando asignación para &f" + tenantName + "&7."));
                return;
            }
        }

        String error = LeaseManager.getInstance().getAssignError(contract, player);
        if (error != null) {
            player.sendMessage(CC.translate("&7" + error));
            Terrain parent = TerrainManager.getInstance().getTerrain(contract.getParentTerrainId());
            if (parent != null && parent.getOrigin() != null) {
                player.sendMessage(CC.translate("&7Terreno padre &f" + parent.getId()
                        + " &7— origen: &f" + parent.getOrigin().getBlockX() + ", " + parent.getOrigin().getBlockZ()
                        + " &7— tamaño: &f" + parent.getSizeInBlocks() + "x" + parent.getSizeInBlocks() + " bloques"));
                player.sendMessage(CC.translate("&7El sub-terreno ocupa &f" + (contract.getChunks() * 16) + "x" + (contract.getChunks() * 16) + " bloques."));
                player.sendMessage(CC.translate("&7Asegúrate de que tu posición más ese tamaño queden dentro del padre."));
            }
            return;
        }

        boolean assigned = LeaseManager.getInstance().assignSubTerrain(contract, player);
        if (!assigned) {
            player.sendMessage(CC.translate("&7No se pudo asignar el sub-terreno."));
            return;
        }

        player.sendMessage(CC.translate("&a✓ Sub-terreno &f" + contract.getSubTerrainId() + " &asignado. Contrato activo."));

        Player tenant = Bukkit.getPlayerExact(contract.getTenantName());
        if (tenant != null) {
            tenant.sendMessage(CC.translate("&a&lContrato activo. &7Tu terreno: &f" + contract.getSubTerrainId()));
            tenant.sendMessage(CC.translate("&7Próximo pago: &f$" + (int) contract.getPricePerCycle() + " &7en &f" + contract.getCycleDays() + " días."));
        }
    }

    private void handleCancel(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease cancel <contractId>"));
            return;
        }
        String contractId = command.getArgs(1);
        boolean cancelled = LeaseManager.getInstance().cancelContract(contractId, player);
        if (!cancelled) {
            player.sendMessage(CC.translate("&7No se pudo cancelar el contrato."));
            return;
        }
        LeaseContract contract = LeaseManager.getInstance().getContract(contractId);
        if (contract != null && contract.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD) {
            player.sendMessage(CC.translate("&7Contrato cancelado. El inquilino tiene &f" + LeaseManager.GRACE_PERIOD_DAYS + " días &7para desalojar."));
        } else {
            player.sendMessage(CC.translate("&7Contrato cancelado."));
        }
    }

    private void handleEvict(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
            player.sendMessage(CC.translate("&cNo tienes permiso."));
            return;
        }
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease evict <contractId>"));
            return;
        }
        boolean evicted = LeaseManager.getInstance().forceEvict(command.getArgs(1), player);
        player.sendMessage(evicted
                ? CC.translate("&7Inquilino desalojado forzosamente.")
                : CC.translate("&7No se pudo desalojar."));
    }

    private void handlePay(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease pay <contractId>"));
            return;
        }
        String contractId = command.getArgs(1);
        LeaseContract contract = LeaseManager.getInstance().getContract(contractId);
        if (contract == null) {
            player.sendMessage(CC.translate("&7Contrato no encontrado."));
            return;
        }
        if (!contract.getTenantId().equals(player.getUniqueId())) {
            player.sendMessage(CC.translate("&7Este contrato no es tuyo."));
            return;
        }
        boolean paid = LeaseManager.getInstance().tryPayGrace(contractId);
        player.sendMessage(paid
                ? CC.translate("&7Pago realizado. Contrato reactivado.")
                : CC.translate("&7No tienes fondos suficientes. Necesitas &f$" + (int) contract.getPricePerCycle() + "&7."));
    }

    private void handleList(Player player) {
        List<LeaseContract> asOwner = LeaseManager.getInstance().getContractsByOwner(player.getUniqueId());
        List<LeaseContract> asTenant = LeaseManager.getInstance().getContractsByTenant(player.getUniqueId());

        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7Como dueño (&f" + asOwner.size() + "&7):"));
        for (LeaseContract c : asOwner) {
            player.sendMessage(CC.translate("  &7- &f" + (c.getSubTerrainId() != null ? c.getSubTerrainId() : c.getContractId())
                    + " &8| &7Inquilino: &f" + c.getTenantName() + " &8| " + formatStatus(c.getStatus())));
        }
        player.sendMessage(CC.translate("  &7Como inquilino (&f" + asTenant.size() + "&7):"));
        for (LeaseContract c : asTenant) {
            player.sendMessage(CC.translate("  &7- &f" + (c.getSubTerrainId() != null ? c.getSubTerrainId() : c.getContractId())
                    + " &8| &7Dueño: &f" + c.getOwnerName() + " &8| " + formatStatus(c.getStatus())));
        }
        player.sendMessage(CC.translate("&8&m                                   "));
    }

    private void handleInfo(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease info <contractId>"));
            return;
        }
        LeaseContract contract = LeaseManager.getInstance().getContract(command.getArgs(1));
        if (contract == null) {
            player.sendMessage(CC.translate("&7Contrato no encontrado."));
            return;
        }
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7ID: &f" + contract.getContractId()));
        player.sendMessage(CC.translate("  &7Padre: &f" + contract.getParentTerrainId() + " &7| Sub: &f" + (contract.getSubTerrainId() != null ? contract.getSubTerrainId() : "Sin asignar")));
        player.sendMessage(CC.translate("  &7Dueño: &f" + contract.getOwnerName() + " &7| Inquilino: &f" + contract.getTenantName()));
        player.sendMessage(CC.translate("  &7Pago: &f$" + (int) contract.getPricePerCycle() + " &7c/" + contract.getCycleDays() + " días"));
        player.sendMessage(CC.translate("  &7Estado: " + formatStatus(contract.getStatus())));
        if (contract.getStatus() == LeaseContract.ContractStatus.ACTIVE) {
            player.sendMessage(CC.translate("  &7Próximo pago en: &f" + contract.getDaysUntilPayment() + " día(s)"));
        }
        if (contract.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD) {
            player.sendMessage(CC.translate("  &cGracia termina en: &f" + contract.getDaysUntilGraceEnd() + " día(s)"));
        }
        player.sendMessage(CC.translate("&8&m                                   "));
    }

    private String formatStatus(LeaseContract.ContractStatus status) {
        switch (status) {
            case PENDING_OWNER: return "&ePendiente (dueño)";
            case PENDING_TENANT: return "&ePendiente (inquilino)";
            case AWAITING_SUBTERRAIN: return "&6Esperando sub-terreno";
            case ACTIVE: return "&aActivo";
            case GRACE_PERIOD: return "&cPeríodo de gracia";
            case EXPIRED: return "&8Expirado";
            case CANCELLED: return "&8Cancelado";
            default: return "&7-";
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7Comandos de Arrendamiento"));
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &f/lease &7— Menú visual"));
        player.sendMessage(CC.translate("  &f/lease offer <terreno> <jugador> <chunks> <precio> <días>"));
        player.sendMessage(CC.translate("  &f/lease request <terreno> <chunks> <precio> <días> <dueño>"));
        player.sendMessage(CC.translate("  &f/lease accept <nombre_del_otro_jugador>"));
        player.sendMessage(CC.translate("  &f/lease assign <nombre_del_inquilino> &8(párate en el chunk)"));
        player.sendMessage(CC.translate("  &f/lease cancel <contractId>"));
        player.sendMessage(CC.translate("  &f/lease pay <contractId> &8(pago en gracia)"));
        player.sendMessage(CC.translate("  &f/lease info <contractId>"));
        player.sendMessage(CC.translate("  &f/lease list"));
        player.sendMessage(CC.translate("  &f/lease evict <contractId> &8(Admin)"));
        player.sendMessage(CC.translate("&8&m                                   "));
    }
}