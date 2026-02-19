package org.debentialc.claims.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.debentialc.claims.events.LeaseSelectionListener;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.menus.LeaseMenu;
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.claims.models.LeaseSelectionSession;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;
import org.debentialc.service.commands.BaseCommand;
import org.debentialc.service.commands.Command;
import org.debentialc.service.commands.CommandArgs;

import java.io.IOException;
import java.util.List;

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

        if (sub.equals("cancel")) {
            if (LeaseSelectionListener.hasSession(player.getUniqueId())) {
                LeaseSelectionListener.cancelSession(player);
            } else {
                player.sendMessage(CC.translate("&7No estás en ningún modo de selección."));
            }
            return;
        }

        if (LeaseSelectionListener.hasSession(player.getUniqueId())) {
            player.sendMessage(CC.translate("&7Estás en modo de selección. Usa &f/lease cancel &7para salir primero."));
            return;
        }

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
            case "assign":
                handleAssign(player, command);
                break;
            case "move":
                handleMove(player, command);
                break;
            case "movaccept":
                handleMoveAccept(player);
                break;
            case "movdecline":
                handleMoveDecline(player);
                break;
            case "movadmin":
                handleMoveAdmin(player, command);
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
        if (terrain == null) { player.sendMessage(CC.translate("&7Terreno &f" + terrainId + " &7no encontrado.")); return; }
        if (!terrain.isOwner(player.getUniqueId()) && !player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) { player.sendMessage(CC.translate("&7No eres el propietario.")); return; }
        if (terrain.getChunks() < LeaseManager.MIN_CHUNKS_TO_LEASE) { player.sendMessage(CC.translate("&7Se necesitan al menos &f" + LeaseManager.MIN_CHUNKS_TO_LEASE + " chunks&7.")); return; }
        if (LeaseManager.getInstance().isSubTerrain(terrainId)) { player.sendMessage(CC.translate("&7No puedes subarrendar un sub-terreno.")); return; }

        Player target = Bukkit.getPlayer(command.getArgs(2));
        if (target == null) { player.sendMessage(CC.translate("&7Jugador no encontrado o no está en línea.")); return; }
        if (target.equals(player)) { player.sendMessage(CC.translate("&7No puedes ofrecerte a ti mismo.")); return; }

        int chunks; double price; int cycleDays;
        try {
            chunks = Integer.parseInt(command.getArgs(3));
            price = Double.parseDouble(command.getArgs(4));
            cycleDays = Integer.parseInt(command.getArgs(5));
        } catch (NumberFormatException e) { player.sendMessage(CC.translate("&7Valores numéricos inválidos.")); return; }

        if (cycleDays < LeaseManager.MIN_CYCLE_DAYS || cycleDays > LeaseManager.MAX_CYCLE_DAYS) { player.sendMessage(CC.translate("&7Ciclo entre &f" + LeaseManager.MIN_CYCLE_DAYS + " &7y &f" + LeaseManager.MAX_CYCLE_DAYS + " &7días.")); return; }
        if (chunks < 1 || chunks >= terrain.getChunks()) { player.sendMessage(CC.translate("&7Chunks entre 1 y " + (terrain.getChunks() - 1) + ".")); return; }
        if (price <= 0) { player.sendMessage(CC.translate("&7El precio debe ser mayor a 0.")); return; }

        LeaseContract contract = LeaseManager.getInstance().offerContract(terrain, player, target.getUniqueId(), target.getName(), chunks, price, cycleDays);
        if (contract == null) { player.sendMessage(CC.translate("&7No se pudo crear el contrato.")); return; }

        player.sendMessage(CC.translate("&7Oferta enviada. ID del contrato: &f" + contract.getContractId()));
        target.sendMessage(CC.translate("&8&m                                   "));
        target.sendMessage(CC.translate("  &6&lOferta de Arrendamiento"));
        target.sendMessage(CC.translate("  &7De: &f" + player.getName() + " &8| Terreno: &f" + terrainId));
        target.sendMessage(CC.translate("  &7Chunks: &f" + chunks + " &8| Precio: &f$" + (int) price + " &7c/" + cycleDays + " días"));
        target.sendMessage(CC.translate("  &aEscribe &f/lease accept " + player.getName() + " &apara aceptar"));
        target.sendMessage(CC.translate("&8&m                                   "));
    }

    private void handleRequest(Player player, CommandArgs command) {
        if (command.length() < 6) {
            player.sendMessage(CC.translate("&7Uso: &f/lease request <terreno> <chunks> <precio> <días> <dueño>"));
            return;
        }
        String terrainId = command.getArgs(1);
        Terrain terrain = TerrainManager.getInstance().getTerrain(terrainId);
        if (terrain == null) { player.sendMessage(CC.translate("&7Terreno no encontrado.")); return; }
        if (!terrain.hasOwner()) { player.sendMessage(CC.translate("&7Ese terreno no tiene propietario.")); return; }
        if (terrain.getChunks() < LeaseManager.MIN_CHUNKS_TO_LEASE) { player.sendMessage(CC.translate("&7Mínimo &f" + LeaseManager.MIN_CHUNKS_TO_LEASE + " chunks&7.")); return; }

        int chunks; double price; int cycleDays;
        try {
            chunks = Integer.parseInt(command.getArgs(2));
            price = Double.parseDouble(command.getArgs(3));
            cycleDays = Integer.parseInt(command.getArgs(4));
        } catch (NumberFormatException e) { player.sendMessage(CC.translate("&7Valores inválidos.")); return; }

        if (cycleDays < LeaseManager.MIN_CYCLE_DAYS || cycleDays > LeaseManager.MAX_CYCLE_DAYS) { player.sendMessage(CC.translate("&7Ciclo entre &f" + LeaseManager.MIN_CYCLE_DAYS + " &7y &f" + LeaseManager.MAX_CYCLE_DAYS + " días.")); return; }
        if (chunks < 1 || chunks >= terrain.getChunks()) { player.sendMessage(CC.translate("&7Chunks entre 1 y " + (terrain.getChunks() - 1) + ".")); return; }
        if (price <= 0) { player.sendMessage(CC.translate("&7Precio mayor a 0.")); return; }

        LeaseContract contract = LeaseManager.getInstance().requestContract(terrain, player, terrain.getOwner(), terrain.getOwnerName(), chunks, price, cycleDays);
        if (contract == null) { player.sendMessage(CC.translate("&7No se pudo crear la solicitud.")); return; }

        player.sendMessage(CC.translate("&7Solicitud enviada. ID: &f" + contract.getContractId()));
        Player owner = Bukkit.getPlayerExact(terrain.getOwnerName());
        if (owner != null) {
            owner.sendMessage(CC.translate("&8&m                                   "));
            owner.sendMessage(CC.translate("  &6&lSolicitud de Arrendamiento"));
            owner.sendMessage(CC.translate("  &7De: &f" + player.getName() + " &8| Terreno: &f" + terrainId));
            owner.sendMessage(CC.translate("  &7Chunks: &f" + chunks + " &8| Precio ofrecido: &f$" + (int) price + " &7c/" + cycleDays + " días"));
            owner.sendMessage(CC.translate("  &aEscribe &f/lease accept " + player.getName() + " &apara aceptar"));
            owner.sendMessage(CC.translate("&8&m                                   "));
        }
    }

    private void handleAccept(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease accept <nombre_jugador>"));
            return;
        }
        LeaseContract contract = LeaseManager.getInstance().findPendingByPlayerName(player.getUniqueId(), command.getArgs(1));
        if (contract == null) { player.sendMessage(CC.translate("&7No hay contrato pendiente con &f" + command.getArgs(1) + "&7.")); return; }

        boolean accepted = LeaseManager.getInstance().acceptContract(contract, player);
        if (!accepted) { player.sendMessage(CC.translate("&7No se pudo aceptar.")); return; }

        boolean isOwner = contract.getOwnerId().equals(player.getUniqueId());
        player.sendMessage(CC.translate("&7Contrato &f" + contract.getContractId() + " &7aceptado."));
        if (isOwner) {
            player.sendMessage(CC.translate("&7Usa &f/lease assign " + contract.getTenantName() + " &7para asignar el sub-terreno."));
            Player tenant = Bukkit.getPlayerExact(contract.getTenantName());
            if (tenant != null) tenant.sendMessage(CC.translate("&7El dueño &f" + player.getName() + " &7aceptó. Espera a que asigne el sub-terreno."));
        } else {
            player.sendMessage(CC.translate("&7El dueño usará &f/lease assign " + player.getName() + " &7para asignarte el terreno."));
            Player owner = Bukkit.getPlayerExact(contract.getOwnerName());
            if (owner != null) {
                owner.sendMessage(CC.translate("&7&f" + player.getName() + " &7aceptó el contrato &f" + contract.getContractId() + "&7."));
                owner.sendMessage(CC.translate("&7Usa &f/lease assign " + player.getName() + " &7para asignar el sub-terreno."));
            }
        }
    }

    private void handleAssign(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease assign <nombre_del_inquilino>"));
            return;
        }
        String tenantName = command.getArgs(1);
        LeaseContract contract = LeaseManager.getInstance().findAwaitingAssignByTenantName(player.getUniqueId(), tenantName);

        if (contract == null && player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) {
            for (LeaseContract c : LeaseManager.getInstance().getAll().values()) {
                if (c.getStatus() == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN
                        && c.getTenantName().equalsIgnoreCase(tenantName)) {
                    contract = c;
                    break;
                }
            }
        }

        if (contract == null) {
            player.sendMessage(CC.translate("&7No hay contrato esperando asignación para &f" + tenantName + "&7."));
            return;
        }

        Terrain parent = TerrainManager.getInstance().getTerrain(contract.getParentTerrainId());
        if (parent == null || !parent.isCommitted() || parent.getOrigin() == null) {
            player.sendMessage(CC.translate("&7El terreno padre no existe o no está generado."));
            return;
        }

        int chunks = contract.getChunks();
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &6&lModo de Asignación — &f" + contract.getContractId()));
        player.sendMessage(CC.translate("  &7Inquilino: &f" + tenantName + " &8| Chunks: &f" + chunks));
        player.sendMessage(CC.translate("  &7Terreno padre: &f" + parent.getId()
                + " &8[" + parent.getOrigin().getBlockX() + "," + parent.getOrigin().getBlockZ()
                + " → " + (parent.getOrigin().getBlockX() + parent.getSizeInBlocks()) + "," + (parent.getOrigin().getBlockZ() + parent.getSizeInBlocks()) + "]"));
        player.sendMessage(CC.translate("&8&m                                   "));

        LeaseSelectionListener.startSession(player, contract.getContractId(), LeaseSelectionSession.SessionMode.ASSIGN);
    }

    private void handleMove(Player player, CommandArgs command) {
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease move <nombre_del_inquilino>"));
            return;
        }
        String tenantName = command.getArgs(1);
        LeaseContract contract = LeaseManager.getInstance().findActiveContractByTenantName(player.getUniqueId(), tenantName);

        if (contract == null) {
            player.sendMessage(CC.translate("&7No tienes un contrato activo con &f" + tenantName + "&7."));
            return;
        }
        if (contract.getSubTerrainId() == null) {
            player.sendMessage(CC.translate("&7El sub-terreno aún no ha sido asignado."));
            return;
        }
        if (contract.isHasPendingMove()) {
            player.sendMessage(CC.translate("&7Ya hay una solicitud de traslado pendiente para &f" + tenantName + "&7."));
            return;
        }

        Terrain parent = TerrainManager.getInstance().getTerrain(contract.getParentTerrainId());
        if (parent == null || !parent.isCommitted() || parent.getOrigin() == null) {
            player.sendMessage(CC.translate("&7El terreno padre no existe."));
            return;
        }

        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &6&lModo de Traslado — &f" + contract.getSubTerrainId()));
        player.sendMessage(CC.translate("  &7Inquilino: &f" + tenantName + " &8| Chunks: &f" + contract.getChunks()));
        player.sendMessage(CC.translate("  &7Terreno padre: &f" + parent.getId()
                + " &8[" + parent.getOrigin().getBlockX() + "," + parent.getOrigin().getBlockZ()
                + " → " + (parent.getOrigin().getBlockX() + parent.getSizeInBlocks()) + "," + (parent.getOrigin().getBlockZ() + parent.getSizeInBlocks()) + "]"));
        player.sendMessage(CC.translate("  &7El inquilino deberá aceptar el traslado."));
        player.sendMessage(CC.translate("&8&m                                   "));

        LeaseSelectionListener.startSession(player, contract.getContractId(), LeaseSelectionSession.SessionMode.MOVE);
    }

    private void handleMoveAccept(Player player) {
        LeaseContract contract = LeaseManager.getInstance().findPendingMoveForTenant(player.getUniqueId());
        if (contract == null) { player.sendMessage(CC.translate("&7No tienes ninguna solicitud de traslado pendiente.")); return; }
        boolean accepted = LeaseManager.getInstance().acceptMove(contract.getContractId(), player);
        player.sendMessage(accepted
                ? CC.translate("&7Traslado aceptado. Tu sub-terreno fue movido.")
                : CC.translate("&7No se pudo aceptar el traslado. La posición ya no es válida."));
    }

    private void handleMoveDecline(Player player) {
        LeaseContract contract = LeaseManager.getInstance().findPendingMoveForTenant(player.getUniqueId());
        if (contract == null) { player.sendMessage(CC.translate("&7No tienes ninguna solicitud de traslado pendiente.")); return; }
        boolean declined = LeaseManager.getInstance().declineMove(contract.getContractId(), player);
        player.sendMessage(declined ? CC.translate("&7Traslado rechazado.") : CC.translate("&7Error al rechazar."));
    }

    private void handleMoveAdmin(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) { player.sendMessage(CC.translate("&cNo tienes permiso.")); return; }
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&7Uso: &f/lease movadmin <contractId>"));
            player.sendMessage(CC.translate("&7Selecciona la nueva zona con clic izquierdo y derecho."));
            return;
        }
        String contractId = command.getArgs(1);
        LeaseContract contract = LeaseManager.getInstance().getContract(contractId);
        if (contract == null) { player.sendMessage(CC.translate("&7Contrato no encontrado.")); return; }
        if (contract.getSubTerrainId() == null) { player.sendMessage(CC.translate("&7Sub-terreno sin asignar.")); return; }
        LeaseSelectionListener.startSession(player, contractId, LeaseSelectionSession.SessionMode.MOVE);
    }

    private void handleEvict(Player player, CommandArgs command) {
        if (!player.hasPermission(ClaimsPermissions.ADMIN_MANAGE)) { player.sendMessage(CC.translate("&cNo tienes permiso.")); return; }
        if (command.length() < 2) { player.sendMessage(CC.translate("&7Uso: &f/lease evict <contractId>")); return; }
        boolean evicted = LeaseManager.getInstance().forceEvict(command.getArgs(1), player);
        player.sendMessage(evicted ? CC.translate("&7Desalojado forzosamente.") : CC.translate("&7No se pudo desalojar."));
    }

    private void handlePay(Player player, CommandArgs command) {
        if (command.length() < 2) { player.sendMessage(CC.translate("&7Uso: &f/lease pay <contractId>")); return; }
        LeaseContract contract = LeaseManager.getInstance().getContract(command.getArgs(1));
        if (contract == null) { player.sendMessage(CC.translate("&7Contrato no encontrado.")); return; }
        if (!contract.getTenantId().equals(player.getUniqueId())) { player.sendMessage(CC.translate("&7No es tu contrato.")); return; }
        boolean paid = LeaseManager.getInstance().tryPayGrace(command.getArgs(1));
        player.sendMessage(paid ? CC.translate("&7Pago realizado. Contrato reactivado.") : CC.translate("&7Sin fondos. Necesitas &f$" + (int) contract.getPricePerCycle() + "&7."));
    }

    private void handleList(Player player) {
        List<LeaseContract> asOwner = LeaseManager.getInstance().getContractsByOwner(player.getUniqueId());
        List<LeaseContract> asTenant = LeaseManager.getInstance().getContractsByTenant(player.getUniqueId());
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7Como dueño (&f" + asOwner.size() + "&7):"));
        for (LeaseContract c : asOwner) {
            player.sendMessage(CC.translate("  &8- &f" + c.getContractId() + " &8| &7Inquilino: &f" + c.getTenantName()
                    + " &8| " + formatStatus(c.getStatus())
                    + (c.isHasPendingMove() ? " &e⟳" : "")));
        }
        player.sendMessage(CC.translate("  &7Como inquilino (&f" + asTenant.size() + "&7):"));
        for (LeaseContract c : asTenant) {
            player.sendMessage(CC.translate("  &8- &f" + c.getContractId() + " &8| &7Dueño: &f" + c.getOwnerName()
                    + " &8| " + formatStatus(c.getStatus())
                    + (c.isHasPendingMove() ? " &e⟳ /lease movaccept o /lease movdecline" : "")));
        }
        player.sendMessage(CC.translate("&8&m                                   "));
    }

    private void handleInfo(Player player, CommandArgs command) {
        if (command.length() < 2) { player.sendMessage(CC.translate("&7Uso: &f/lease info <contractId>")); return; }
        LeaseContract c = LeaseManager.getInstance().getContract(command.getArgs(1));
        if (c == null) { player.sendMessage(CC.translate("&7Contrato no encontrado.")); return; }
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7ID: &f" + c.getContractId()));
        player.sendMessage(CC.translate("  &7Padre: &f" + c.getParentTerrainId() + " &8| Sub: &f" + (c.getSubTerrainId() != null ? c.getSubTerrainId() : "Sin asignar")));
        player.sendMessage(CC.translate("  &7Dueño: &f" + c.getOwnerName() + " &8| Inquilino: &f" + c.getTenantName()));
        player.sendMessage(CC.translate("  &7Pago: &f$" + (int) c.getPricePerCycle() + " &7c/" + c.getCycleDays() + " días &8| Chunks: &f" + c.getChunks()));
        player.sendMessage(CC.translate("  &7Estado: " + formatStatus(c.getStatus())));
        if (c.getStatus() == LeaseContract.ContractStatus.ACTIVE) player.sendMessage(CC.translate("  &7Próximo pago: &f" + c.getDaysUntilPayment() + " día(s)"));
        if (c.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD) player.sendMessage(CC.translate("  &cGracia termina en: &f" + c.getDaysUntilGraceEnd() + " día(s)"));
        if (c.isHasPendingMove()) player.sendMessage(CC.translate("  &e⟳ Traslado pendiente de aceptación del inquilino"));
        player.sendMessage(CC.translate("&8&m                                   "));
    }

    private String formatStatus(LeaseContract.ContractStatus s) {
        switch (s) {
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
        player.sendMessage(CC.translate("  &f/lease accept <nombre_jugador>"));
        player.sendMessage(CC.translate("  &f/lease assign <nombre_inquilino> &8— entra en modo selección"));
        player.sendMessage(CC.translate("  &f/lease move <nombre_inquilino> &8— entra en modo selección"));
        player.sendMessage(CC.translate("  &f/lease movaccept &8— acepta traslado pendiente"));
        player.sendMessage(CC.translate("  &f/lease movdecline &8— rechaza traslado pendiente"));
        player.sendMessage(CC.translate("  &f/lease movadmin <contractId> &8(Admin)"));
        player.sendMessage(CC.translate("  &f/lease cancel &8— cancela modo selección activo"));
        player.sendMessage(CC.translate("  &f/lease pay <contractId>"));
        player.sendMessage(CC.translate("  &f/lease info <contractId>"));
        player.sendMessage(CC.translate("  &f/lease list"));
        player.sendMessage(CC.translate("  &f/lease evict <contractId> &8(Admin)"));
        player.sendMessage(CC.translate("&8&m                                   "));
    }
}