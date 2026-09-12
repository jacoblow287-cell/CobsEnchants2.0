package com.cobsenchants.enchants;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents one "virtual" custom enchant. These are NOT registered into the vanilla
 * enchantment registry - they are tracked entirely via PersistentDataContainer keys on
 * items (see {@link com.cobsenchants.util.EnchantPDC}) plus lore text and a glint override.
 * This lets us apply them to literally any item in the game through the anvil.
 */
public class CustomEnchant {

    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final EnchantCategory category;
    private final Material icon;
    private final String description;
    private final Set<String> conflicts = new HashSet<>();

    public CustomEnchant(String id, String displayName, int maxLevel, EnchantCategory category, Material icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.category = category;
        this.icon = icon;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public EnchantCategory getCategory() {
        return category;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getConflicts() {
        return conflicts;
    }

    public void addConflict(String otherId) {
        conflicts.add(otherId);
    }

    public boolean conflictsWith(String otherId) {
        return conflicts.contains(otherId);
    }
}
