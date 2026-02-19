package org.debentialc.claims.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.claims.events.LeaseSelectionListener;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.claims.models.LeaseSelectionSession;
import org.debentialc.service.CC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaseContractDetailMenu {

    public static SmartInventory createDetailMenu(final LeaseContract contract, final Player viewer) {
        return SmartInventory.builder()
                .id("lease_detail_" + contract.getContractId())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.fillBorders(ClickableItem.empty(TerritoryMenu.createGlassPane((short) 8)));

                        ItemStack infoItem = new ItemStack(Material.PAPER);
                        ItemMeta infoMeta = infoItem.getItemMeta();
                        infoMeta.setDisplayName(CC.translate("&f&l" + contract.getContractId()));
                        List<String> lore = new ArrayList<String>();
                        lore.add(CC.translate("&7Terreno padre: &f" + contract.getParentTerrainId()));
                        lore.add(CC.translate("&7Sub-terreno:   &f" + (contract.getSubTerrainId() != null ? contract.getSubTerrainId() : "&8Sin asignar")));
                        lore.add(CC.translate("&8&m                         "));
                        lore.add(CC.translate("&7Dueño:     &f" + contract.getOwnerName()));
                        lore.add(CC.translate("&7Inquilino: &f" + contract.getTenantName()));
                        lore.add(CC.translate("&8&m                         "));
                        lore.add(CC.translate("&7Chunks: &f" + contract.getChunks() + " &8(" + (contract.getChunks() * 16) + "×" + (contract.getChunks() * 16) + " bloques)"));
                        lore.add(CC.translate("&7Precio: &f$" + (int) contract.getPricePerCycle() + " &7cada &f" + contract.getCycleDays() + " días"));
                        lore.add(CC.translate("&7Origen: " + (contract.getOrigin() == LeaseContract.ContractOrigin.OWNER_OFFER ? "&7Oferta del dueño" : "&7Solicitud del inquilino")));
                        lore.add(CC.translate("&7Estado: " + LeaseContractListMenu.formatStatus(contract.getStatus())));
                        if (contract.getStatus() == LeaseContract.ContractStatus.ACTIVE) {
                            lore.add(CC.translate("&8&m                         "));
                            lore.add(CC.translate("&7Próximo pago: &f" + contract.getDaysUntilPayment() + " día(s)"));
                        }
                        if (contract.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD) {
                            lore.add(CC.translate("&8&m                         "));
                            lore.add(CC.translate("&cGracia expira en: &f" + contract.getDaysUntilGraceEnd() + " día(s)"));
                        }
                        if (contract.isHasPendingMove()) {
                            lore.add(CC.translate("&8&m                         "));
                            lore.add(CC.translate("&e⟳ Traslado pendiente de aprobación del inquilino"));
                        }
                        infoMeta.setLore(lore);
                        infoItem.setItemMeta(infoMeta);
                        contents.set(1, 4, ClickableItem.empty(infoItem));

                        boolean isOwner = contract.getOwnerId().equals(player.getUniqueId());
                        boolean isTenant = contract.getTenantId().equals(player.getUniqueId());
                        boolean isAdmin = player.hasPermission(ClaimsPermissions.ADMIN_MANAGE);
                        LeaseContract.ContractStatus status = contract.getStatus();

                        boolean pendingForMe = (status == LeaseContract.ContractStatus.PENDING_TENANT && isTenant)
                                || (status == LeaseContract.ContractStatus.PENDING_OWNER && isOwner);
                        if (pendingForMe) {
                            ItemStack acceptBtn = new ItemStack(Material.EMERALD_BLOCK);
                            ItemMeta m = acceptBtn.getItemMeta();
                            m.setDisplayName(CC.translate("&a&lAceptar Contrato"));
                            m.setLore(Arrays.asList(
                                    CC.translate("&7Acepta el contrato de arrendamiento."),
                                    CC.translate(isOwner
                                            ? "&7Luego asignarás el sub-terreno con &f/lease assign"
                                            : "&7El dueño asignará el sub-terreno."),
                                    "",
                                    CC.translate("&a▶ Click para aceptar")
                            ));
                            acceptBtn.setItemMeta(m);
                            contents.set(2, 3, ClickableItem.of(acceptBtn, e -> {
                                boolean accepted = LeaseManager.getInstance().acceptContract(contract, player);
                                if (accepted) {
                                    player.sendMessage(CC.translate("&7Contrato &f" + contract.getContractId() + " &7aceptado."));
                                    if (isOwner) {
                                        player.sendMessage(CC.translate("&7Usa &f/lease assign " + contract.getTenantName() + " &7para asignar el sub-terreno."));
                                    } else {
                                        player.sendMessage(CC.translate("&7El dueño usará &f/lease assign " + player.getName() + " &7para asignarte el terreno."));
                                    }
                                } else {
                                    player.sendMessage(CC.translate("&7No se pudo aceptar."));
                                }
                                player.closeInventory();
                            }));
                        }

                        if (status == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN && (isOwner || isAdmin)) {
                            int req = contract.getChunks() * 16;
                            ItemStack assignBtn = new ItemStack(Material.STICK);
                            ItemMeta m = assignBtn.getItemMeta();
                            m.setDisplayName(CC.translate("&6&lAsignar Sub-terreno"));
                            m.setLore(Arrays.asList(
                                    CC.translate("&7Área requerida: &f" + req + "×" + req + " bloques &8(±1 tolerancia)"),
                                    CC.translate("&7ID resultante: &f" + LeaseManager.getInstance().generateSubTerrainId(contract.getParentTerrainId(), contract.getTenantName())),
                                    "",
                                    CC.translate("&7Recibirás una &fBlaze Rod &7especial."),
                                    CC.translate("&e✦ Clic izq &7→ Pos1    &e✦ Clic der &7→ Pos2"),
                                    "",
                                    CC.translate("&6▶ Click para entrar en modo selección")
                            ));
                            assignBtn.setItemMeta(m);
                            contents.set(2, 4, ClickableItem.of(assignBtn, e -> {
                                player.closeInventory();
                                if (LeaseSelectionListener.hasSession(player.getUniqueId())) {
                                    player.sendMessage(CC.translate("&7Ya estás en modo selección. Usa &f/lease cancel &7para salir primero."));
                                    return;
                                }
                                LeaseSelectionListener.startSession(player, contract.getContractId(), LeaseSelectionSession.SessionMode.ASSIGN);
                            }));
                        }

                        if (status == LeaseContract.ContractStatus.ACTIVE && isOwner && !contract.isHasPendingMove() && contract.getSubTerrainId() != null) {
                            ItemStack moveBtn = new ItemStack(Material.COMPASS);
                            ItemMeta m = moveBtn.getItemMeta();
                            m.setDisplayName(CC.translate("&b&lMover Sub-terreno"));
                            m.setLore(Arrays.asList(
                                    CC.translate("&7Solicita reubicar el sub-terreno de &f" + contract.getTenantName() + "&7."),
                                    CC.translate("&7El inquilino deberá aceptar el traslado."),
                                    "",
                                    CC.translate("&7Recibirás una &fBlaze Rod &7especial."),
                                    CC.translate("&e✦ Clic izq &7→ Pos1    &e✦ Clic der &7→ Pos2"),
                                    "",
                                    CC.translate("&b▶ Click para entrar en modo selección")
                            ));
                            moveBtn.setItemMeta(m);
                            contents.set(2, 5, ClickableItem.of(moveBtn, e -> {
                                player.closeInventory();
                                if (LeaseSelectionListener.hasSession(player.getUniqueId())) {
                                    player.sendMessage(CC.translate("&7Ya estás en modo selección. Usa &f/lease cancel &7para salir primero."));
                                    return;
                                }
                                LeaseSelectionListener.startSession(player, contract.getContractId(), LeaseSelectionSession.SessionMode.MOVE);
                            }));
                        }

                        if (status == LeaseContract.ContractStatus.ACTIVE && isOwner && contract.isHasPendingMove()) {
                            ItemStack waitBtn = new ItemStack(Material.WATCH);
                            ItemMeta m = waitBtn.getItemMeta();
                            m.setDisplayName(CC.translate("&e&lTraslado Pendiente"));
                            m.setLore(Arrays.asList(
                                    CC.translate("&7Esperando respuesta de &f" + contract.getTenantName() + "&7."),
                                    "",
                                    CC.translate("&7El inquilino debe usar:"),
                                    CC.translate("&f/lease movaccept &7o &f/lease movdecline")
                            ));
                            waitBtn.setItemMeta(m);
                            contents.set(2, 5, ClickableItem.empty(waitBtn));
                        }

                        if (status == LeaseContract.ContractStatus.ACTIVE && isTenant && contract.isHasPendingMove()) {
                            ItemStack acceptMoveBtn = new ItemStack(Material.EMERALD);
                            ItemMeta ma = acceptMoveBtn.getItemMeta();
                            ma.setDisplayName(CC.translate("&a&lAceptar Traslado"));
                            ma.setLore(Arrays.asList(
                                    CC.translate("&7Nueva posición: &fX=" + contract.getPendingMoveX1() + " Z=" + contract.getPendingMoveZ1()),
                                    "",
                                    CC.translate("&a▶ Click para aceptar")
                            ));
                            acceptMoveBtn.setItemMeta(ma);
                            contents.set(2, 4, ClickableItem.of(acceptMoveBtn, e -> {
                                boolean ok = LeaseManager.getInstance().acceptMove(contract.getContractId(), player);
                                player.sendMessage(ok
                                        ? CC.translate("&7Traslado aceptado. Tu sub-terreno fue movido.")
                                        : CC.translate("&7No se pudo aceptar. La posición ya no es válida."));
                                player.closeInventory();
                            }));

                            ItemStack declineMoveBtn = new ItemStack(Material.REDSTONE_BLOCK);
                            ItemMeta md = declineMoveBtn.getItemMeta();
                            md.setDisplayName(CC.translate("&c&lRechazar Traslado"));
                            md.setLore(Arrays.asList(
                                    CC.translate("&7Tu sub-terreno permanecerá en su lugar actual."),
                                    "",
                                    CC.translate("&c▶ Click para rechazar")
                            ));
                            declineMoveBtn.setItemMeta(md);
                            contents.set(2, 5, ClickableItem.of(declineMoveBtn, e -> {
                                boolean ok = LeaseManager.getInstance().declineMove(contract.getContractId(), player);
                                player.sendMessage(ok ? CC.translate("&7Traslado rechazado.") : CC.translate("&7Error al rechazar."));
                                player.closeInventory();
                            }));
                        }

                        if (status == LeaseContract.ContractStatus.GRACE_PERIOD && isTenant) {
                            ItemStack payBtn = new ItemStack(Material.GOLD_INGOT);
                            ItemMeta m = payBtn.getItemMeta();
                            m.setDisplayName(CC.translate("&e&lPagar Deuda"));
                            m.setLore(Arrays.asList(
                                    CC.translate("&7Monto: &f$" + (int) contract.getPricePerCycle()),
                                    CC.translate("&7Gracia expira en: &f" + contract.getDaysUntilGraceEnd() + " día(s)"),
                                    "",
                                    CC.translate("&e▶ Click para pagar")
                            ));
                            payBtn.setItemMeta(m);
                            contents.set(2, 5, ClickableItem.of(payBtn, e -> {
                                boolean paid = LeaseManager.getInstance().tryPayGrace(contract.getContractId());
                                player.sendMessage(paid
                                        ? CC.translate("&7Pago realizado. Contrato reactivado.")
                                        : CC.translate("&7Sin fondos. Necesitas &f$" + (int) contract.getPricePerCycle() + "&7."));
                                player.closeInventory();
                            }));
                        }

                        boolean canCancel = (isOwner || isTenant || isAdmin)
                                && (status == LeaseContract.ContractStatus.ACTIVE
                                || status == LeaseContract.ContractStatus.PENDING_OWNER
                                || status == LeaseContract.ContractStatus.PENDING_TENANT
                                || status == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN);
                        if (canCancel) {
                            ItemStack cancelBtn = new ItemStack(Material.REDSTONE_BLOCK);
                            ItemMeta m = cancelBtn.getItemMeta();
                            m.setDisplayName(CC.translate("&c&lCancelar Contrato"));
                            List<String> cl = new ArrayList<String>();
                            cl.add(status == LeaseContract.ContractStatus.ACTIVE
                                    ? CC.translate("&7El inquilino tendrá &f" + LeaseManager.GRACE_PERIOD_DAYS + " días &7para desalojar.")
                                    : CC.translate("&7El contrato será anulado inmediatamente."));
                            cl.add("");
                            cl.add(CC.translate("&c▶ Click para cancelar"));
                            m.setLore(cl);
                            cancelBtn.setItemMeta(m);
                            contents.set(2, 6, ClickableItem.of(cancelBtn, e -> {
                                boolean cancelled = LeaseManager.getInstance().cancelContract(contract.getContractId(), player);
                                player.sendMessage(cancelled
                                        ? CC.translate("&7Contrato cancelado.")
                                        : CC.translate("&7No se pudo cancelar."));
                                player.closeInventory();
                            }));
                        }

                        boolean isActiveOrGrace = status == LeaseContract.ContractStatus.ACTIVE
                                || status == LeaseContract.ContractStatus.GRACE_PERIOD;
                        if (isAdmin && isActiveOrGrace) {
                            ItemStack evictBtn = new ItemStack(Material.TNT);
                            ItemMeta m = evictBtn.getItemMeta();
                            m.setDisplayName(CC.translate("&4&lDesalojo Forzoso &8(Admin)"));
                            m.setLore(Arrays.asList(
                                    CC.translate("&7Elimina el sub-terreno inmediatamente."),
                                    CC.translate("&cAcción irreversible."),
                                    "",
                                    CC.translate("&4▶ Click para desalojar")
                            ));
                            evictBtn.setItemMeta(m);
                            contents.set(3, 3, ClickableItem.of(evictBtn, e -> {
                                LeaseManager.getInstance().forceEvict(contract.getContractId(), player);
                                player.sendMessage(CC.translate("&7Inquilino desalojado forzosamente."));
                                player.closeInventory();
                            }));

                            ItemStack movAdminBtn = new ItemStack(Material.COMMAND);
                            ItemMeta mm = movAdminBtn.getItemMeta();
                            mm.setDisplayName(CC.translate("&5&lMover Sub-terreno &8(Admin)"));
                            mm.setLore(Arrays.asList(
                                    CC.translate("&7Mueve el sub-terreno sin pedir aprobación"),
                                    CC.translate("&7al inquilino."),
                                    "",
                                    CC.translate("&7Recibirás una &fBlaze Rod &7especial."),
                                    "",
                                    CC.translate("&5▶ Click para entrar en modo selección")
                            ));
                            movAdminBtn.setItemMeta(mm);
                            contents.set(3, 5, ClickableItem.of(movAdminBtn, e -> {
                                player.closeInventory();
                                if (contract.getSubTerrainId() == null) {
                                    player.sendMessage(CC.translate("&7Sub-terreno sin asignar."));
                                    return;
                                }
                                if (LeaseSelectionListener.hasSession(player.getUniqueId())) {
                                    player.sendMessage(CC.translate("&7Ya estás en modo selección. Usa &f/lease cancel &7primero."));
                                    return;
                                }
                                LeaseSelectionListener.startSession(player, contract.getContractId(), LeaseSelectionSession.SessionMode.MOVE);
                            }));
                        }

                        ItemStack backBtn = new ItemStack(Material.ARROW);
                        ItemMeta bm = backBtn.getItemMeta();
                        bm.setDisplayName(CC.translate("&7← Atrás"));
                        backBtn.setItemMeta(bm);
                        contents.set(3, 0, ClickableItem.of(backBtn, e -> {
                            LeaseMenu.createMainMenu(player).open(player);
                        }));
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {
                    }
                })
                .size(4, 9)
                .title(CC.translate("&f&lDetalle del Contrato"))
                .build();
    }
}