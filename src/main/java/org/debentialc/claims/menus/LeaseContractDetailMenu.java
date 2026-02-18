package org.debentialc.claims.menus;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.claims.managers.ClaimsPermissions;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.models.LeaseContract;
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
                        infoMeta.setDisplayName(CC.translate("&f&lContrato: " + contract.getContractId()));
                        List<String> lore = new ArrayList<String>();
                        lore.add(CC.translate("&7Terreno padre: &f" + contract.getParentTerrainId()));
                        lore.add(CC.translate("&7Sub-terreno: &f" + (contract.getSubTerrainId() != null ? contract.getSubTerrainId() : "Sin asignar")));
                        lore.add(CC.translate("&7Propietario: &f" + contract.getOwnerName()));
                        lore.add(CC.translate("&7Inquilino: &f" + contract.getTenantName()));
                        lore.add(CC.translate("&7Chunks arrendados: &f" + contract.getChunks()));
                        lore.add(CC.translate("&7Precio: &f$" + (int) contract.getPricePerCycle() + " &7cada &f" + contract.getCycleDays() + " días"));
                        lore.add(CC.translate("&7Origen: " + (contract.getOrigin() == LeaseContract.ContractOrigin.OWNER_OFFER ? "&7Oferta del dueño" : "&7Solicitud del inquilino")));
                        lore.add(CC.translate("&7Estado: " + LeaseContractListMenu.formatStatus(contract.getStatus())));
                        if (contract.getStatus() == LeaseContract.ContractStatus.ACTIVE) {
                            lore.add(CC.translate("&7Próximo pago en: &f" + contract.getDaysUntilPayment() + " día(s)"));
                        }
                        if (contract.getStatus() == LeaseContract.ContractStatus.GRACE_PERIOD) {
                            lore.add(CC.translate("&cPeríodo de gracia: &f" + contract.getDaysUntilGraceEnd() + " día(s) restantes"));
                        }
                        infoMeta.setLore(lore);
                        infoItem.setItemMeta(infoMeta);
                        contents.set(1, 4, ClickableItem.empty(infoItem));

                        boolean isOwner = contract.getOwnerId().equals(player.getUniqueId());
                        boolean isTenant = contract.getTenantId().equals(player.getUniqueId());
                        boolean isAdmin = player.hasPermission(ClaimsPermissions.ADMIN_MANAGE);
                        LeaseContract.ContractStatus status = contract.getStatus();

                        if ((status == LeaseContract.ContractStatus.PENDING_TENANT && isTenant)
                                || (status == LeaseContract.ContractStatus.PENDING_OWNER && isOwner)) {
                            ItemStack acceptButton = new ItemStack(Material.EMERALD_BLOCK);
                            ItemMeta acceptMeta = acceptButton.getItemMeta();
                            acceptMeta.setDisplayName(CC.translate("&a&lAceptar Contrato"));
                            acceptMeta.setLore(Arrays.asList(
                                    CC.translate("&7Acepta este contrato de arrendamiento"),
                                    CC.translate(isOwner ? "&7Luego deberás asignar el sub-terreno" : "&7El dueño asignará el sub-terreno"),
                                    "",
                                    CC.translate("&a[CLICK PARA ACEPTAR]")
                            ));
                            acceptButton.setItemMeta(acceptMeta);
                            contents.set(2, 3, ClickableItem.of(acceptButton, e -> {
                                boolean accepted = LeaseManager.getInstance().acceptContract(contract, player);
                                if (accepted) {
                                    player.sendMessage(CC.translate("&7Contrato aceptado."));
                                    if (isOwner) {
                                        player.sendMessage(CC.translate("&7Párate en el chunk del sub-terreno y usa &f/lease assign " + contract.getTenantName()));
                                    } else {
                                        player.sendMessage(CC.translate("&7El dueño debe asignar el sub-terreno con &f/lease assign " + player.getName()));
                                    }
                                } else {
                                    player.sendMessage(CC.translate("&7No se pudo aceptar el contrato."));
                                }
                                LeaseMenu.createMainMenu(player).open(player);
                            }));
                        }

                        if (status == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN && (isOwner || isAdmin)) {
                            String error = LeaseManager.getInstance().getAssignError(contract, player);
                            ItemStack assignButton = new ItemStack(error == null ? Material.GRASS : Material.DIRT);
                            ItemMeta assignMeta = assignButton.getItemMeta();
                            assignMeta.setDisplayName(CC.translate("&6&lAsignar Sub-terreno"));
                            List<String> assignLore = new ArrayList<String>();
                            assignLore.add(CC.translate("&7Sub-terreno de &f" + contract.getChunks() + " chunk(s)"));
                            assignLore.add(CC.translate("&7ID resultante: &f" + LeaseManager.getInstance().generateSubTerrainId(contract.getParentTerrainId(), contract.getTenantName())));
                            assignLore.add("");
                            if (error == null) {
                                assignLore.add(CC.translate("&a✓ Tu posición es válida"));
                                assignLore.add(CC.translate("&6[CLICK PARA ASIGNAR AQUÍ]"));
                            } else {
                                assignLore.add(CC.translate("&c✗ " + error));
                                assignLore.add(CC.translate("&7Muévete dentro del terreno padre"));
                            }
                            assignButton.setItemMeta(assignMeta);
                            if (error == null) {
                                contents.set(2, 4, ClickableItem.of(assignButton, e -> {
                                    boolean assigned = LeaseManager.getInstance().assignSubTerrain(contract, player);
                                    if (assigned) {
                                        player.sendMessage(CC.translate("&a✓ Sub-terreno asignado. Contrato activo."));
                                    } else {
                                        player.sendMessage(CC.translate("&7No se pudo asignar. Muévete dentro del terreno padre."));
                                    }
                                    LeaseMenu.createMainMenu(player).open(player);
                                }));
                            } else {
                                contents.set(2, 4, ClickableItem.empty(assignButton));
                            }
                        }

                        if (status == LeaseContract.ContractStatus.GRACE_PERIOD && isTenant) {
                            ItemStack payButton = new ItemStack(Material.GOLD_INGOT);
                            ItemMeta payMeta = payButton.getItemMeta();
                            payMeta.setDisplayName(CC.translate("&e&lPagar Deuda"));
                            payMeta.setLore(Arrays.asList(
                                    CC.translate("&7Monto: &f$" + (int) contract.getPricePerCycle()),
                                    CC.translate("&7Tiempo restante: &f" + contract.getDaysUntilGraceEnd() + " día(s)"),
                                    "",
                                    CC.translate("&e[CLICK PARA PAGAR]")
                            ));
                            payButton.setItemMeta(payMeta);
                            contents.set(2, 5, ClickableItem.of(payButton, e -> {
                                boolean paid = LeaseManager.getInstance().tryPayGrace(contract.getContractId());
                                player.sendMessage(paid
                                        ? CC.translate("&7Pago realizado. Contrato reactivado.")
                                        : CC.translate("&7Sin fondos. Necesitas &f$" + (int) contract.getPricePerCycle() + "&7."));
                                LeaseMenu.createMainMenu(player).open(player);
                            }));
                        }

                        boolean canCancel = (isOwner || isTenant || isAdmin)
                                && (status == LeaseContract.ContractStatus.ACTIVE
                                || status == LeaseContract.ContractStatus.PENDING_OWNER
                                || status == LeaseContract.ContractStatus.PENDING_TENANT
                                || status == LeaseContract.ContractStatus.AWAITING_SUBTERRAIN);

                        if (canCancel) {
                            ItemStack cancelButton = new ItemStack(Material.REDSTONE_BLOCK);
                            ItemMeta cancelMeta = cancelButton.getItemMeta();
                            cancelMeta.setDisplayName(CC.translate("&c&lCancelar Contrato"));
                            List<String> cancelLore = new ArrayList<String>();
                            if (status == LeaseContract.ContractStatus.ACTIVE) {
                                cancelLore.add(CC.translate("&7El inquilino tendrá &f" + LeaseManager.GRACE_PERIOD_DAYS + " días &7para desalojar"));
                            } else {
                                cancelLore.add(CC.translate("&7El contrato será anulado"));
                            }
                            cancelLore.add("");
                            cancelLore.add(CC.translate("&c[CLICK PARA CANCELAR]"));
                            cancelMeta.setLore(cancelLore);
                            cancelButton.setItemMeta(cancelMeta);
                            contents.set(2, 6, ClickableItem.of(cancelButton, e -> {
                                boolean cancelled = LeaseManager.getInstance().cancelContract(contract.getContractId(), player);
                                player.sendMessage(cancelled
                                        ? CC.translate("&7Contrato cancelado.")
                                        : CC.translate("&7No se pudo cancelar."));
                                LeaseMenu.createMainMenu(player).open(player);
                            }));
                        }

                        if (isAdmin && (status == LeaseContract.ContractStatus.ACTIVE || status == LeaseContract.ContractStatus.GRACE_PERIOD)) {
                            ItemStack evictButton = new ItemStack(Material.TNT);
                            ItemMeta evictMeta = evictButton.getItemMeta();
                            evictMeta.setDisplayName(CC.translate("&4&lDesalojo Forzoso &8(Admin)"));
                            evictMeta.setLore(Arrays.asList(
                                    CC.translate("&7Elimina el sub-terreno inmediatamente"),
                                    CC.translate("&cAcción irreversible"),
                                    "",
                                    CC.translate("&4[CLICK PARA DESALOJAR]")
                            ));
                            evictButton.setItemMeta(evictMeta);
                            contents.set(3, 4, ClickableItem.of(evictButton, e -> {
                                LeaseManager.getInstance().forceEvict(contract.getContractId(), player);
                                player.sendMessage(CC.translate("&7Inquilino desalojado forzosamente."));
                                LeaseMenu.createMainMenu(player).open(player);
                            }));
                        }

                        ItemStack backButton = new ItemStack(Material.ARROW);
                        ItemMeta backMeta = backButton.getItemMeta();
                        backMeta.setDisplayName(CC.translate("&b← Atrás"));
                        backButton.setItemMeta(backMeta);
                        contents.set(3, 0, ClickableItem.of(backButton, e -> {
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