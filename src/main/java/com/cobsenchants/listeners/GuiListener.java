package com.cobsenchants.listeners;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import com.cobsenchants.enchants.EnchantCategory;
import com.cobsenchants.gui.CategoryGui;
import com.cobsenchants.gui.GuiHolder;
import com.cobsenchants.gui.LevelSelectGui;
import com.cobsenchants.gui.MainMenuGui;
import com.cobsenchants.util.BookFactory;
import com.cobsenchants.util.RomanNumeral;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GuiListener implements Listener {

    private final CobsEnchantsPlugin plugin;

    public GuiListener(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder rawHolder = event.getInventory().getHolder();
        if (!(rawHolder instanceof GuiHolder holder)) return;

        // Never allow taking/moving items in or out of these menus.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return; // clicked their own inventory

        switch (holder.getType()) {
            case MAIN -> handleMainClick(player, slot);
            case CATEGORY -> handleCategoryClick(player, holder, slot);
            case LEVEL_SELECT -> handleLevelSelectClick(player, holder, slot, clicked);
        }
    }

    private void handleMainClick(Player player, int slot) {
        EnchantCategory category = switch (slot) {
            case MainMenuGui.SLOT_SPECIAL -> EnchantCategory.SPECIAL;
            case MainMenuGui.SLOT_UTILITY -> EnchantCategory.UTILITY;
            case MainMenuGui.SLOT_EFFECT -> EnchantCategory.EFFECT;
            case MainMenuGui.SLOT_EFFECT_ATTACK -> EnchantCategory.EFFECT_ATTACK;
            case MainMenuGui.SLOT_EFFECT_IMMUNITY -> EnchantCategory.EFFECT_IMMUNITY;
            case MainMenuGui.SLOT_DAMAGE_IMMUNITY -> EnchantCategory.DAMAGE_IMMUNITY;
            default -> null;
        };
        if (category != null) {
            CategoryGui.open(plugin, player, category, 0);
        }
    }

    private void handleCategoryClick(Player player, GuiHolder holder, int slot) {
        if (slot == CategoryGui.SLOT_BACK) {
            MainMenuGui.open(plugin, player);
            return;
        }
        if (slot == CategoryGui.SLOT_PREV) {
            CategoryGui.open(plugin, player, holder.getCategory(), holder.getPage() - 1);
            return;
        }
        if (slot == CategoryGui.SLOT_NEXT) {
            CategoryGui.open(plugin, player, holder.getCategory(), holder.getPage() + 1);
            return;
        }
        if (slot < 0 || slot >= CategoryGui.PAGE_SIZE) return;

        List<CustomEnchant> list = plugin.getEnchantRegistry().byCategory(holder.getCategory());
        int index = holder.getPage() * CategoryGui.PAGE_SIZE + slot;
        if (index < 0 || index >= list.size()) return;
        CustomEnchant enchant = list.get(index);

        if (enchant.getMaxLevel() > 1) {
            LevelSelectGui.open(plugin, player, enchant);
        } else {
            giveBook(player, enchant, 1);
        }
    }

    private void handleLevelSelectClick(Player player, GuiHolder holder, int slot, ItemStack clicked) {
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Back")) {
            CustomEnchant enchant = plugin.getEnchantRegistry().get(holder.getEnchantId());
            if (enchant != null) {
                CategoryGui.open(plugin, player, enchant.getCategory(), 0);
            }
            return;
        }

        CustomEnchant enchant = plugin.getEnchantRegistry().get(holder.getEnchantId());
        if (enchant == null) return;
        int level = slot + 1;
        if (level < 1 || level > enchant.getMaxLevel()) return;
        giveBook(player, enchant, level);
    }

    private void giveBook(Player player, CustomEnchant enchant, int level) {
        ItemStack book = BookFactory.create(plugin, enchant, level);
        var leftovers = player.getInventory().addItem(book);
        leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage("§aYou received: " + enchant.getDisplayName() + " " + RomanNumeral.of(level));
    }
}
