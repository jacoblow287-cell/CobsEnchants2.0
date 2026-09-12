package com.cobsenchants.util;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BookFactory {

    public static ItemStack create(CobsEnchantsPlugin plugin, CustomEnchant enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        String lvlStr = enchant.getMaxLevel() > 1 ? " " + RomanNumeral.of(level) : "";
        meta.setDisplayName(enchant.getDisplayName() + lvlStr);

        List<String> lore = new ArrayList<>();
        lore.add("§7" + enchant.getDescription());
        lore.add("§8Apply to any item with an anvil.");
        meta.setLore(lore);
        meta.setEnchantmentGlintOverride(true);

        plugin.getEnchantPDC().setLevel(meta, enchant, level);
        book.setItemMeta(meta);
        return book;
    }
}
