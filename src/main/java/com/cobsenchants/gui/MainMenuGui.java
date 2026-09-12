package com.cobsenchants.gui;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.EnchantCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MainMenuGui {

    // Fixed slots so GuiListener can map clicks back to categories without parsing text.
    public static final int SLOT_SPECIAL = 10;
    public static final int SLOT_UTILITY = 11;
    public static final int SLOT_EFFECT = 12;
    public static final int SLOT_EFFECT_ATTACK = 13;
    public static final int SLOT_EFFECT_IMMUNITY = 14;
    public static final int SLOT_DAMAGE_IMMUNITY = 15;

    public static void open(CobsEnchantsPlugin plugin, Player player) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.MAIN, null, null, 0);
        Inventory inv = Bukkit.createInventory(holder, 27, "§8CobsEnchants");
        holder.setInventory(inv);

        inv.setItem(SLOT_SPECIAL, item(Material.SHIELD, "§bSpecial Enchants",
                "§7Damage Evasion, Double Jump, True Damage,", "§7Lifesteal Attack, Lightning Attack"));
        inv.setItem(SLOT_UTILITY, item(Material.SMITHING_TABLE, "§bUtility Enchants",
                "§7Invis Armour, Blacksmith Enchant"));
        inv.setItem(SLOT_EFFECT, item(Material.POTION, "§dEffect Enchants",
                "§7Grants a potion effect while", "§7worn/held (up to level 10)"));
        inv.setItem(SLOT_EFFECT_ATTACK, item(Material.IRON_SWORD, "§cEffect Attack Enchants",
                "§7Applies an effect to whoever", "§7you hit, or whoever hits you"));
        inv.setItem(SLOT_EFFECT_IMMUNITY, item(Material.MILK_BUCKET, "§aEffect Immunity Enchants",
                "§7Makes you immune to a", "§7specific potion effect"));
        inv.setItem(SLOT_DAMAGE_IMMUNITY, item(Material.TOTEM_OF_UNDYING, "§eDamage Immunity Enchants",
                "§7Makes you immune to a", "§7specific damage type"));


        player.openInventory(inv);
    }

    private static ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }
}
