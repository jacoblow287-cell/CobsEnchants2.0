package com.cobsenchants.util;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regenerates the lore lines that list which custom enchants are on an item,
 * and toggles the enchantment glint accordingly. This fully replaces the lore
 * with just the enchant list - keep that in mind if you also want persistent
 * flavor-text lore on an item.
 */
public class LoreBuilder {

    public static void refreshLore(CobsEnchantsPlugin plugin, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Map<CustomEnchant, Integer> enchants = plugin.getEnchantPDC().getAll(item);

        List<String> lore = new ArrayList<>();
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            CustomEnchant e = entry.getKey();
            int lvl = entry.getValue();
            String lvlStr = e.getMaxLevel() > 1 ? " " + RomanNumeral.of(lvl) : "";
            lore.add(e.getDisplayName() + lvlStr);
        }

        meta.setLore(lore.isEmpty() ? null : lore);
        meta.setEnchantmentGlintOverride(!enchants.isEmpty());
        item.setItemMeta(meta);
    }
}
