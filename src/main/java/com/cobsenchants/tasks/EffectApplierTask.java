package com.cobsenchants.tasks;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import com.cobsenchants.enchants.EnchantCategory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The "<effect> Effect" enchant category grants a potion effect while worn/held anywhere.
 * Potion effects expire on their own, so we refresh them periodically with a short duration
 * that overlaps the task interval, giving a continuous effect with no visible flicker.
 */
public class EffectApplierTask extends BukkitRunnable {

    private static final int REFRESH_DURATION_TICKS = 80; // 4s, task runs every 2s (40 ticks)

    private final CobsEnchantsPlugin plugin;

    public EffectApplierTask(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            EntityEquipment eq = player.getEquipment();
            if (eq == null) continue;

            ItemStack[] pieces = {
                    eq.getHelmet(), eq.getChestplate(), eq.getLeggings(), eq.getBoots(),
                    eq.getItemInOffHand(), eq.getItemInMainHand()
            };

            for (CustomEnchant enchant : plugin.getEnchantRegistry().byCategory(EnchantCategory.EFFECT)) {
                int best = 0;
                for (ItemStack piece : pieces) {
                    best = Math.max(best, plugin.getEnchantPDC().getLevel(piece, enchant));
                }
                if (best > 0) {
                    PotionEffectType type = plugin.getEnchantRegistry().effectTypeFor(enchant.getId());
                    if (type != null) {
                        player.addPotionEffect(new PotionEffect(type, REFRESH_DURATION_TICKS, best - 1, true, false, true));
                    }
                }
            }
        }
    }
}
