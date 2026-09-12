package com.cobsenchants.tasks;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Invis Armour doesn't remove the item - it keeps the real armor equipped (so it still protects
 * the wearer) but repeatedly tells every other player's client "this slot shows nothing" using
 * Paper's fake-equipment API. Because this is a client-side illusion rather than a real state
 * change, it has to be re-sent periodically (vanilla equipment-sync packets would otherwise
 * eventually show the real item again).
 */
public class InvisArmorTask extends BukkitRunnable {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final CobsEnchantsPlugin plugin;

    public InvisArmorTask(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        CustomEnchant invis = plugin.getEnchantRegistry().get("invis_armour");
        if (invis == null) return;

        for (Player wearer : Bukkit.getOnlinePlayers()) {
            EntityEquipment eq = wearer.getEquipment();
            if (eq == null) continue;

            applySlot(wearer, EquipmentSlot.HEAD, eq.getHelmet(), invis);
            applySlot(wearer, EquipmentSlot.CHEST, eq.getChestplate(), invis);
            applySlot(wearer, EquipmentSlot.LEGS, eq.getLeggings(), invis);
            applySlot(wearer, EquipmentSlot.FEET, eq.getBoots(), invis);
        }
    }

    private void applySlot(Player wearer, EquipmentSlot slot, ItemStack real, CustomEnchant invis) {
        boolean hide = real != null && plugin.getEnchantPDC().getLevel(real, invis) > 0;
        if (!hide) return; // let vanilla's normal equipment sync handle visible pieces

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(wearer)) continue;
            viewer.sendEquipmentChange(wearer, slot, AIR);
        }
    }
}
