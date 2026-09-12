package com.cobsenchants.gui;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import com.cobsenchants.util.RomanNumeral;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LevelSelectGui {

    public static void open(CobsEnchantsPlugin plugin, Player player, CustomEnchant enchant) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.LEVEL_SELECT, enchant.getCategory(), enchant.getId(), 0);
        int size = 27;
        String title = "§8Select Level: " + ChatColor.stripColor(enchant.getDisplayName());
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        int max = Math.min(enchant.getMaxLevel(), size - 1);
        for (int lvl = 1; lvl <= max; lvl++) {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(enchant.getDisplayName() + " " + RomanNumeral.of(lvl));
            List<String> lore = new ArrayList<>();
            lore.add("§7Level " + lvl);
            lore.add("§eClick to receive this book");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(lvl - 1, item);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.setDisplayName("§7« Back");
        back.setItemMeta(bMeta);
        inv.setItem(size - 1, back);

        player.openInventory(inv);
    }
}
