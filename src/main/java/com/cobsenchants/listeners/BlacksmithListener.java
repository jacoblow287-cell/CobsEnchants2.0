package com.cobsenchants.listeners;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Blacksmith Enchant: while sneaking, right-click cycles the armor trim material and
 * left-click cycles the trim pattern on any equipped piece that carries this enchant.
 * The current index for each is stored in the item's PDC so cycling always continues
 * from where it left off.
 */
public class BlacksmithListener implements Listener {

    private final CobsEnchantsPlugin plugin;
    private final NamespacedKey materialIndexKey;
    private final NamespacedKey patternIndexKey;
    private final List<TrimMaterial> materials = new ArrayList<>();
    private final List<TrimPattern> patterns = new ArrayList<>();
    private final Map<UUID, Long> cooldown = new HashMap<>();

    public BlacksmithListener(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.materialIndexKey = new NamespacedKey(plugin, "ce_trim_material_index");
        this.patternIndexKey = new NamespacedKey(plugin, "ce_trim_pattern_index");
        Registry.TRIM_MATERIAL.forEach(materials::add);
        Registry.TRIM_PATTERN.forEach(patterns::add);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; // only handle once per interaction, not per hand
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        boolean right = event.getAction().isRightClick();
        boolean left = event.getAction().isLeftClick();
        if (!right && !left) return;
        if (materials.isEmpty() || patterns.isEmpty()) return;

        long now = System.currentTimeMillis();
        Long last = cooldown.get(player.getUniqueId());
        if (last != null && now - last < 250) return;

        CustomEnchant blacksmith = plugin.getEnchantRegistry().get("blacksmith");
        if (blacksmith == null) return;

        EntityEquipment eq = player.getEquipment();
        if (eq == null) return;

        boolean changedAny = false;
        changedAny |= tryCycle(eq, EquipmentSlot.HEAD, eq.getHelmet(), blacksmith, right);
        changedAny |= tryCycle(eq, EquipmentSlot.CHEST, eq.getChestplate(), blacksmith, right);
        changedAny |= tryCycle(eq, EquipmentSlot.LEGS, eq.getLeggings(), blacksmith, right);
        changedAny |= tryCycle(eq, EquipmentSlot.FEET, eq.getBoots(), blacksmith, right);

        if (changedAny) {
            cooldown.put(player.getUniqueId(), now);
            event.setCancelled(true);
            player.sendMessage(right ? "§7Blacksmith: trim material changed." : "§7Blacksmith: trim pattern changed.");
        }
    }

    /** Returns true if this piece had the enchant and was cycled. */
    private boolean tryCycle(EntityEquipment eq, EquipmentSlot slot, ItemStack piece, CustomEnchant blacksmith, boolean right) {
        if (piece == null) return false;
        if (plugin.getEnchantPDC().getLevel(piece, blacksmith) <= 0) return false;

        var rawMeta = piece.getItemMeta();
        if (!(rawMeta instanceof ArmorMeta meta)) return false;

        if (right) {
            int idx = meta.getPersistentDataContainer().getOrDefault(materialIndexKey, PersistentDataType.INTEGER, -1);
            idx = Math.floorMod(idx + 1, materials.size());
            meta.getPersistentDataContainer().set(materialIndexKey, PersistentDataType.INTEGER, idx);
            meta.setTrim(new ArmorTrim(materials.get(idx), currentPattern(meta)));
        } else {
            int idx = meta.getPersistentDataContainer().getOrDefault(patternIndexKey, PersistentDataType.INTEGER, -1);
            idx = Math.floorMod(idx + 1, patterns.size());
            meta.getPersistentDataContainer().set(patternIndexKey, PersistentDataType.INTEGER, idx);
            meta.setTrim(new ArmorTrim(currentMaterial(meta), patterns.get(idx)));
        }

        piece.setItemMeta(meta);
        setSlot(eq, slot, piece);
        return true;
    }

    private void setSlot(EntityEquipment eq, EquipmentSlot slot, ItemStack item) {
        switch (slot) {
            case HEAD -> eq.setHelmet(item);
            case CHEST -> eq.setChestplate(item);
            case LEGS -> eq.setLeggings(item);
            case FEET -> eq.setBoots(item);
            default -> { }
        }
    }

    private TrimMaterial currentMaterial(ArmorMeta meta) {
        int idx = meta.getPersistentDataContainer().getOrDefault(materialIndexKey, PersistentDataType.INTEGER, 0);
        return materials.get(Math.floorMod(idx, materials.size()));
    }

    private TrimPattern currentPattern(ArmorMeta meta) {
        int idx = meta.getPersistentDataContainer().getOrDefault(patternIndexKey, PersistentDataType.INTEGER, 0);
        return patterns.get(Math.floorMod(idx, patterns.size()));
    }
}
