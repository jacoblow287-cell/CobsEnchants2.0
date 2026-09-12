package com.cobsenchants.listeners;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import com.cobsenchants.enchants.EnchantCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class CombatListener implements Listener {

    private final CobsEnchantsPlugin plugin;
    private final Random random = new Random();

    public CombatListener(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEvasionCheck(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        int level = getMaxEquippedLevel(victim, "damage_evasion");
        if (level <= 0) return;
        double chance = Math.min(0.75, level * 0.25);
        if (random.nextDouble() < chance) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombatEnchants(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;

        CustomEnchant trueDamage = plugin.getEnchantRegistry().get("true_damage");
        CustomEnchant lifesteal = plugin.getEnchantRegistry().get("lifesteal_attack");
        CustomEnchant lightningAttack = plugin.getEnchantRegistry().get("lightning_attack");

        ItemStack weapon = getMainHand(attacker);

        if (victim instanceof Player && trueDamage != null) {
            int tdLevel = plugin.getEnchantPDC().getLevel(weapon, trueDamage);
            if (tdLevel > 0) {
                double extra = tdLevel * 2.0; // 1 heart = 2 health per level
                victim.setHealth(Math.max(0, victim.getHealth() - extra));
            }
        }

        if (lifesteal != null) {
            int lsLevel = plugin.getEnchantPDC().getLevel(weapon, lifesteal);
            if (lsLevel > 0) {
                double steal = 4.0; // 2 hearts
                victim.setHealth(Math.max(0, victim.getHealth() - steal));
                double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH) != null
                        ? attacker.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0;
                attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + steal));
            }
        }

        // Lightning Attack via mainhand weapon -> strikes victim
        if (lightningAttack != null && plugin.getEnchantPDC().getLevel(weapon, lightningAttack) > 0) {
            victim.getWorld().strikeLightning(victim.getLocation());
        }

        // Lightning Attack via victim's armor/offhand -> strikes attacker (retaliation)
        if (lightningAttack != null) {
            for (ItemStack piece : getArmorAndOffhand(victim)) {
                if (piece != null && plugin.getEnchantPDC().getLevel(piece, lightningAttack) > 0) {
                    attacker.getWorld().strikeLightning(attacker.getLocation());
                    break;
                }
            }
        }

        // Effect Attack via mainhand weapon -> applies to victim
        for (CustomEnchant e : plugin.getEnchantRegistry().byCategory(EnchantCategory.EFFECT_ATTACK)) {
            if (plugin.getEnchantPDC().getLevel(weapon, e) > 0) {
                PotionEffectType type = plugin.getEnchantRegistry().effectTypeFor(e.getId());
                if (type != null) {
                    victim.addPotionEffect(new PotionEffect(type, 100, 0, false, true));
                }
            }
        }

        // Effect Attack via victim's armor/offhand -> applies to attacker (retaliation)
        for (ItemStack piece : getArmorAndOffhand(victim)) {
            if (piece == null) continue;
            for (CustomEnchant e : plugin.getEnchantRegistry().byCategory(EnchantCategory.EFFECT_ATTACK)) {
                if (plugin.getEnchantPDC().getLevel(piece, e) > 0) {
                    PotionEffectType type = plugin.getEnchantRegistry().effectTypeFor(e.getId());
                    if (type != null) {
                        attacker.addPotionEffect(new PotionEffect(type, 100, 0, false, true));
                    }
                }
            }
        }
    }

    private ItemStack getMainHand(LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();
        return eq == null ? null : eq.getItemInMainHand();
    }

    private List<ItemStack> getArmorAndOffhand(LivingEntity entity) {
        List<ItemStack> list = new ArrayList<>();
        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return list;
        list.add(eq.getHelmet());
        list.add(eq.getChestplate());
        list.add(eq.getLeggings());
        list.add(eq.getBoots());
        list.add(eq.getItemInOffHand());
        return list;
    }

    private int getMaxEquippedLevel(LivingEntity entity, String enchantId) {
        CustomEnchant enchant = plugin.getEnchantRegistry().get(enchantId);
        if (enchant == null) return 0;
        int max = 0;
        for (ItemStack piece : getArmorAndOffhand(entity)) {
            max = Math.max(max, plugin.getEnchantPDC().getLevel(piece, enchant));
        }
        max = Math.max(max, plugin.getEnchantPDC().getLevel(getMainHand(entity), enchant));
        return max;
    }
}
