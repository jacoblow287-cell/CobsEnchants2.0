package com.cobsenchants.gui;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import com.cobsenchants.enchants.EnchantCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CategoryGui {

    public static final int PAGE_SIZE = 45;
    public static final int SLOT_BACK = 45;
    public static final int SLOT_PREV = 48;
    public static final int SLOT_NEXT = 50;

    public static void open(CobsEnchantsPlugin plugin, Player player, EnchantCategory category, int page) {
        List<CustomEnchant> list = plugin.getEnchantRegistry().byCategory(category);
        int maxPage = Math.max(0, (list.size() - 1) / PAGE_SIZE);
        if (page < 0) page = 0;
        if (page > maxPage) page = maxPage;

        GuiHolder holder = new GuiHolder(GuiHolder.Type.CATEGORY, category, null, page);
        String title = "§8" + prettyCategory(category) + " (" + (page + 1) + "/" + (maxPage + 1) + ")";
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        int start = page * PAGE_SIZE;
        int end = Math.min(list.size(), start + PAGE_SIZE);
        for (int i = start; i < end; i++) {
            CustomEnchant e = list.get(i);
            inv.setItem(i - start, enchantItem(e));
        }

        inv.setItem(SLOT_BACK, navItem(Material.ARROW, "§7« Back to Main Menu"));
        if (page > 0) inv.setItem(SLOT_PREV, navItem(Material.PAPER, "§7« Previous Page"));
        if (page < maxPage) inv.setItem(SLOT_NEXT, navItem(Material.PAPER, "§7Next Page »"));

        player.openInventory(inv);
    }

    private static String prettyCategory(EnchantCategory category) {
        return switch (category) {
            case SPECIAL -> "Special Enchants";
            case UTILITY -> "Utility Enchants";
            case EFFECT -> "Effect Enchants";
            case EFFECT_ATTACK -> "Effect Attack Enchants";
            case EFFECT_IMMUNITY -> "Effect Immunity Enchants";
            case DAMAGE_IMMUNITY -> "Damage Immunity Enchants";
        };
    }

    private static ItemStack enchantItem(CustomEnchant e) {
        ItemStack item = new ItemStack(e.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(e.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§7" + e.getDescription());
        lore.add("");
        if (e.getMaxLevel() > 1) {
            lore.add("§eClick to choose a level (1-" + e.getMaxLevel() + ")");
        } else {
            lore.add("§eClick to receive this enchanted book");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack navItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
