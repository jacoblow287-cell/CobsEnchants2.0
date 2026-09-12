package com.cobsenchants.util;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads/writes our custom enchant levels from an item's PersistentDataContainer.
 * Each enchant gets its own namespaced key, so an item can hold any combination
 * of our custom enchants regardless of what the item actually is.
 */
public class EnchantPDC {

    private final CobsEnchantsPlugin plugin;

    public EnchantPDC(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    public NamespacedKey keyFor(CustomEnchant enchant) {
        return new NamespacedKey(plugin, "ce_" + enchant.getId());
    }

    public int getLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || enchant == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer lvl = pdc.get(keyFor(enchant), PersistentDataType.INTEGER);
        return lvl == null ? 0 : lvl;
    }

    public void setLevel(ItemMeta meta, CustomEnchant enchant, int level) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (level <= 0) {
            pdc.remove(keyFor(enchant));
        } else {
            pdc.set(keyFor(enchant), PersistentDataType.INTEGER, level);
        }
    }

    public Map<CustomEnchant, Integer> getAll(ItemStack item) {
        Map<CustomEnchant, Integer> map = new LinkedHashMap<>();
        if (item == null || !item.hasItemMeta()) return map;
        for (CustomEnchant e : plugin.getEnchantRegistry().all()) {
            int lvl = getLevel(item, e);
            if (lvl > 0) map.put(e, lvl);
        }
        return map;
    }
}
