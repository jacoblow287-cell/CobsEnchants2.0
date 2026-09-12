package com.cobsenchants.listeners;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Real "double jump mid-air" isn't a native Bukkit event, so this uses the standard trick:
 * give survival players setAllowFlight(true) while they carry the enchant. Double-tapping
 * space while airborne then fires PlayerToggleFlightEvent instead of actually starting flight;
 * we cancel that and launch the player in their look direction instead.
 */
public class DoubleJumpListener implements Listener {

    private final CobsEnchantsPlugin plugin;

    public DoubleJumpListener(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateFlightState(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Cheap re-sync: only bother when the player is standing on ground (equipment likely settled)
        // or has just changed vertical block position.
        if (event.getPlayer().isOnGround()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
            updateFlightState(event.getPlayer());
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!event.isFlying()) return;
        if (!hasDoubleJump(player)) return;

        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(true);

        Location loc = player.getLocation();
        Vector direction = loc.getDirection().normalize();
        Vector velocity = direction.clone().multiply(1.3);
        velocity.setY(Math.max(0.5, direction.getY() * 1.3 + 0.35));
        player.setVelocity(velocity);
        player.setFallDistance(0);
    }

    private boolean hasDoubleJump(Player player) {
        CustomEnchant enchant = plugin.getEnchantRegistry().get("double_jump");
        if (enchant == null) return false;
        EntityEquipment eq = player.getEquipment();
        if (eq == null) return false;
        ItemStack[] pieces = {
                eq.getHelmet(), eq.getChestplate(), eq.getLeggings(), eq.getBoots(),
                eq.getItemInOffHand(), eq.getItemInMainHand()
        };
        for (ItemStack piece : pieces) {
            if (plugin.getEnchantPDC().getLevel(piece, enchant) > 0) return true;
        }
        return false;
    }

    private void updateFlightState(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        boolean has = hasDoubleJump(player);
        if (has && !player.getAllowFlight()) {
            player.setAllowFlight(true);
        } else if (!has && player.getAllowFlight() && !player.isFlying()) {
            player.setAllowFlight(false);
        }
    }
}
