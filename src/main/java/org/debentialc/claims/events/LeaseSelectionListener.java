package org.debentialc.claims.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.debentialc.claims.managers.LeaseManager;
import org.debentialc.claims.managers.TerrainManager;
import org.debentialc.claims.models.LeaseContract;
import org.debentialc.claims.models.LeaseSelectionSession;
import org.debentialc.claims.models.Terrain;
import org.debentialc.service.CC;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeaseSelectionListener implements Listener {

    private static final String TOOL_NAME_TAG = "§6§l[Selector de Arrendamiento]";
    private static final int BLOCK_TOLERANCE = 1;

    private static final Map<UUID, LeaseSelectionSession> sessions = new HashMap<UUID, LeaseSelectionSession>();

    public static void startSession(Player player, String contractId, LeaseSelectionSession.SessionMode mode) {
        LeaseSelectionSession session = new LeaseSelectionSession(contractId, mode);
        sessions.put(player.getUniqueId(), session);
        player.getInventory().setItemInHand(buildTool());

        LeaseContract contract = LeaseManager.getInstance().getContract(contractId);
        int chunks = contract != null ? contract.getChunks() : 0;
        int requiredBlocks = chunks * 16;

        String actionLabel = mode == LeaseSelectionSession.SessionMode.ASSIGN ? "Asignación"
                : mode == LeaseSelectionSession.SessionMode.MOVE ? "Traslado (Admin)" : "Traslado";

        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &6&lModo Selección — " + actionLabel));
        player.sendMessage(CC.translate("  &7Contrato: &f" + contractId));
        player.sendMessage(CC.translate("  &7Área requerida: &f" + requiredBlocks + "×" + requiredBlocks
                + " bloques &8(±" + BLOCK_TOLERANCE + " bloque(s) de tolerancia)"));
        player.sendMessage(CC.translate("  &8— — — — — — — — — — — — — —"));
        player.sendMessage(CC.translate("  &eClic izquierdo &7en una esquina → &fPos1"));
        player.sendMessage(CC.translate("  &eClic derecho &7en la esquina opuesta → &fPos2"));
        player.sendMessage(CC.translate("  &7Escribe &f/lease cancel &7para salir del modo."));
        player.sendMessage(CC.translate("&8&m                                   "));
    }

    public static void cancelSession(Player player) {
        if (!sessions.containsKey(player.getUniqueId())) return;
        sessions.remove(player.getUniqueId());
        removeTool(player);
        player.sendMessage(CC.translate("&7Modo de selección cancelado."));
    }

    public static boolean hasSession(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public static LeaseSelectionSession getSession(UUID playerId) {
        return sessions.get(playerId);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!sessions.containsKey(uuid)) return;
        if (!isSelectionTool(player.getItemInHand())) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        event.setCancelled(true);

        LeaseSelectionSession session = sessions.get(uuid);
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            session.setPos1(block.getLocation());
            player.sendMessage(CC.translate("  &ePos1 &8→ &fX=" + block.getX() + " Y=" + block.getY() + " Z=" + block.getZ()));
            if (session.isComplete()) {
                processSelection(player, session);
            } else {
                player.sendMessage(CC.translate("  &7Ahora &eclic derecho &7en la esquina opuesta para &fPos2&7."));
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            session.setPos2(block.getLocation());
            player.sendMessage(CC.translate("  &ePos2 &8→ &fX=" + block.getX() + " Y=" + block.getY() + " Z=" + block.getZ()));
            if (session.isComplete()) {
                processSelection(player, session);
            } else {
                player.sendMessage(CC.translate("  &7Ahora &eclic izquierdo &7en la otra esquina para &fPos1&7."));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }

    private void processSelection(Player player, LeaseSelectionSession session) {
        LeaseContract contract = LeaseManager.getInstance().getContract(session.getContractId());
        if (contract == null) {
            sessions.remove(player.getUniqueId());
            removeTool(player);
            player.sendMessage(CC.translate("&cContrato no encontrado. Modo cancelado."));
            return;
        }

        org.bukkit.Location p1 = session.getPos1();
        org.bukkit.Location p2 = session.getPos2();

        if (!p1.getWorld().getName().equals(p2.getWorld().getName())) {
            player.sendMessage(CC.translate("  &cPos1 y Pos2 deben estar en el mismo mundo. Intenta de nuevo."));
            session.setPos1(null);
            session.setPos2(null);
            return;
        }

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;
        int requiredBlocks = contract.getChunks() * 16;

        Terrain parent = TerrainManager.getInstance().getTerrain(contract.getParentTerrainId());
        if (parent != null && parent.getOrigin() != null) {
            int px = parent.getOrigin().getBlockX();
            int pz = parent.getOrigin().getBlockZ();
            int pSize = parent.getSizeInBlocks();
            player.sendMessage(CC.translate("  &7Terreno padre: &fX[" + px + " → " + (px + pSize)
                    + "] Z[" + pz + " → " + (pz + pSize) + "] (" + pSize + " bloques)"));
        } else {
            player.sendMessage(CC.translate("  &cTerreno padre no encontrado o sin origen."));
        }
        player.sendMessage(CC.translate("&8&m                                   "));

        boolean okX = Math.abs(sizeX - requiredBlocks) <= BLOCK_TOLERANCE;
        boolean okZ = Math.abs(sizeZ - requiredBlocks) <= BLOCK_TOLERANCE;

        if (!okX || !okZ) {
            player.sendMessage(CC.translate("  &c✗ Tamaño incorrecto — vuelve a marcar Pos1 y Pos2"));
            if (!okX) {
                int diff = sizeX - requiredBlocks;
                player.sendMessage(CC.translate("  &7Eje X: " + (diff > 0 ? "&csobra(n) &f" + diff : "&cfalta(n) &f" + Math.abs(diff)) + " &7bloque(s)"));
            }
            if (!okZ) {
                int diff = sizeZ - requiredBlocks;
                player.sendMessage(CC.translate("  &7Eje Z: " + (diff > 0 ? "&csobra(n) &f" + diff : "&cfalta(n) &f" + Math.abs(diff)) + " &7bloque(s)"));
            }
            session.setPos1(null);
            session.setPos2(null);
            return;
        }

        int ox = minX;
        int oz = minZ;
        int size = requiredBlocks;
        World world = p1.getWorld();
        int y = Math.max(p1.getBlockY(), p2.getBlockY());

        LeaseSelectionSession.SessionMode mode = session.getMode();
        sessions.remove(player.getUniqueId());
        removeTool(player);

        if (mode == LeaseSelectionSession.SessionMode.ASSIGN) {
            doAssign(player, contract, world, ox, oz, y, size, sizeX, sizeZ);
        } else if (mode == LeaseSelectionSession.SessionMode.MOVE) {
            doMoveRequest(player, contract, world, ox, oz, y, sizeX, sizeZ);
        } else if (mode == LeaseSelectionSession.SessionMode.MOVE) {
            doMoveAdmin(player, contract, world, ox, oz, y, sizeX, sizeZ);
        }
    }

    private void doAssign(Player player, LeaseContract contract, World world,
                          int ox, int oz, int y, int size, int sizeX, int sizeZ) {
        String error = LeaseManager.getInstance().validateSelectionForAssign(contract, world, ox, oz, size);
        if (error != null) {
            player.sendMessage(CC.translate("&8&m                                   "));
            player.sendMessage(CC.translate("  &cSelección inválida"));
            player.sendMessage(CC.translate("  &7" + error));
            player.sendMessage(CC.translate("  &7Usa &f/lease assign " + contract.getTenantName() + " &7para intentar de nuevo."));
            player.sendMessage(CC.translate("&8&m                                   "));
            return;
        }

        boolean ok = LeaseManager.getInstance().assignSubTerrainAtPos(contract, player, world, ox, oz, y);
        if (!ok) {
            player.sendMessage(CC.translate("&cNo se pudo asignar el sub-terreno. Intenta de nuevo."));
            return;
        }

        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &aSub-terreno &f" + contract.getSubTerrainId() + " &aasignado."));
        player.sendMessage(CC.translate("  &7Origen: &fX=" + ox + " Z=" + oz + " &8| Área: &f" + sizeX + "×" + sizeZ + " bloques"));
        player.sendMessage(CC.translate("&8&m                                   "));

        Player tenant = Bukkit.getPlayerExact(contract.getTenantName());
        if (tenant != null) {
            tenant.sendMessage(CC.translate("&a&lContrato activo. &7Tu terreno: &f" + contract.getSubTerrainId()));
            tenant.sendMessage(CC.translate("&7Próximo pago: &f$" + (int) contract.getPricePerCycle() + " &7en &f" + contract.getCycleDays() + " día(s)."));
        }
    }

    private void doMoveRequest(Player player, LeaseContract contract, World world,
                               int ox, int oz, int y, int sizeX, int sizeZ) {
        int size = contract.getChunks() * 16;
        String error = LeaseManager.getInstance().validateSelectionForMove(contract, world, ox, oz, size);
        if (error != null) {
            player.sendMessage(CC.translate("&8&m                                   "));
            player.sendMessage(CC.translate("  &cSelección inválida"));
            player.sendMessage(CC.translate("  &7" + error));
            player.sendMessage(CC.translate("  &7Usa &f/lease move " + contract.getTenantName() + " &7para intentar de nuevo."));
            player.sendMessage(CC.translate("&8&m                                   "));
            return;
        }

        LeaseManager.getInstance().requestMoveWithSelection(contract, player, world, ox, oz, y);

        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7Solicitud de traslado enviada a &f" + contract.getTenantName() + "&7."));
        player.sendMessage(CC.translate("  &7Nueva área: &fX=" + ox + " Z=" + oz + " — " + sizeX + "×" + sizeZ + " bloques"));
        player.sendMessage(CC.translate("  &7Esperando respuesta del inquilino."));
        player.sendMessage(CC.translate("&8&m                                   "));

        Player tenant = Bukkit.getPlayerExact(contract.getTenantName());
        if (tenant != null) {
            tenant.sendMessage(CC.translate("&8&m                                   "));
            tenant.sendMessage(CC.translate("  &e&lSolicitud de Traslado — &f" + contract.getSubTerrainId()));
            tenant.sendMessage(CC.translate("  &7El dueño &f" + player.getName() + " &7quiere reubicar tu sub-terreno."));
            tenant.sendMessage(CC.translate("  &7Nueva área: &fX=" + ox + " Z=" + oz + " — " + sizeX + "×" + sizeZ + " bloques"));
            tenant.sendMessage(CC.translate("  &a/lease movaccept &8| &c/lease movdecline"));
            tenant.sendMessage(CC.translate("&8&m                                   "));
        }
    }

    private void doMoveAdmin(Player player, LeaseContract contract, World world,
                             int ox, int oz, int y, int sizeX, int sizeZ) {
        boolean ok = LeaseManager.getInstance().adminMoveSubTerrain(contract.getContractId(), player, world, ox, oz, y);
        if (!ok) {
            player.sendMessage(CC.translate("&cNo se pudo mover el sub-terreno. Consulta el debug de arriba."));
            return;
        }
        player.sendMessage(CC.translate("&8&m                                   "));
        player.sendMessage(CC.translate("  &7Sub-terreno &f" + contract.getSubTerrainId() + " &7movido &8(Admin)&7."));
        player.sendMessage(CC.translate("  &7Nueva área: &fX=" + ox + " Z=" + oz + " — " + sizeX + "×" + sizeZ + " bloques"));
        player.sendMessage(CC.translate("&8&m                                   "));
    }

    private static ItemStack buildTool() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TOOL_NAME_TAG);
        meta.setLore(Arrays.asList(
                CC.translate("&eClic izquierdo &7— Primera esquina &8(Pos1)"),
                CC.translate("&eClic derecho &7— Esquina opuesta &8(Pos2)"),
                CC.translate("&8/lease cancel para salir del modo")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static boolean isSelectionTool(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().equals(TOOL_NAME_TAG);
    }

    private static void removeTool(Player player) {
        if (isSelectionTool(player.getItemInHand())) player.setItemInHand(null);
    }
}