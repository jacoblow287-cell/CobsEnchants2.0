package com.cobsenchants.gui;

import com.cobsenchants.enchants.EnchantCategory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiHolder implements InventoryHolder {

    public enum Type { MAIN, CATEGORY, LEVEL_SELECT }

    private final Type type;
    private final EnchantCategory category;
    private final String enchantId;
    private final int page;
    private Inventory inventory;

    public GuiHolder(Type type, EnchantCategory category, String enchantId, int page) {
        this.type = type;
        this.category = category;
        this.enchantId = enchantId;
        this.page = page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Type getType() {
        return type;
    }

    public EnchantCategory getCategory() {
        return category;
    }

    public String getEnchantId() {
        return enchantId;
    }

    public int getPage() {
        return page;
    }
}
