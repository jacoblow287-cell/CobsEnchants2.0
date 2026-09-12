package com.cobsenchants.listeners;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import com.cobsenchants.util.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Lets any item + an enchanted book carrying one of our custom enchants combine in an anvil,
 * regardless of whether vanilla would normally allow that enchant on that item type.
 * Also allows combining two items that already carry our custom enchants (union of enchants,
 * levels increase the same way vanilla enchant levels do: same level = +1, capped at max level).
 */
public class AnvilListener implements Listener {

    private final CobsEnchantsPlugin plugin;

    public AnvilListener(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack base = inv.getItem(0);
        ItemStack addition = inv.getItem(1);
        if (base == null || base.getType() == Material.AIR) return;
        if (addition == null || addition.getType() == Material.AIR) return;

        Map<CustomEnchant, Integer> additionEnchants = plugin.getEnchantPDC().getAll(addition);
        if (additionEnchants.isEmpty()) return; // nothing of ours to merge, let vanilla handle it

        ItemStack vanillaResult = event.getResult();
        ItemStack result = (vanillaResult != null && vanillaResult.getType() != Material.AIR)
                ? vanillaResult.clone()
                : base.clone();

        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta == null) return;

        Map<CustomEnchant, Integer> baseEnchants = plugin.getEnchantPDC().getAll(base);

        int cost = 0;
        boolean anyApplied = false;

        for (Map.Entry<CustomEnchant, Integer> entry : additionEnchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int addLevel = entry.getValue();

            boolean conflict = false;
            for (CustomEnchant existing : baseEnchants.keySet()) {
                if (existing.getId().equals(enchant.getId())) continue;
                if (existing.conflictsWith(enchant.getId()) || enchant.conflictsWith(existing.getId())) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) continue;

            int baseLevel = baseEnchants.getOrDefault(enchant, 0);
            int newLevel;
            if (baseLevel == addLevel) {
                newLevel = Math.min(enchant.getMaxLevel(), baseLevel + 1);
            } else {
                newLevel = Math.max(baseLevel, addLevel);
            }

            plugin.getEnchantPDC().setLevel(resultMeta, enchant, newLevel);
            cost += Math.max(1, newLevel);
            anyApplied = true;
        }

        if (!anyApplied) {
            event.setResult(null);
            return;
        }

        result.setItemMeta(resultMeta);
        LoreBuilder.refreshLore(plugin, result);

        int existingCost = inv.getRepairCost();
        inv.setRepairCost(Math.min(39, existingCost + cost));

        event.setResult(result);
    }
}
