package com.cobsenchants.listeners;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.enchants.CustomEnchant;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class ProtectionListener implements Listener {

    private final CobsEnchantsPlugin plugin;

    public ProtectionListener(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // Dragon's breath lingering cloud damage doesn't always report as DamageCause.DRAGON_BREATH
        // (depending on version it can come through as MAGIC/POTION_EFFECT instead), so detect it
        // directly via the damaging entity as a fallback in addition to the normal cause check below.
        if (event instanceof EntityDamageByEntityEvent byEntity
                && byEntity.getDamager() instanceof AreaEffectCloud cloud
                && cloud.getParticle() != null
                && cloud.getParticle().name().contains("DRAGON_BREATH")) {
            CustomEnchant dragonBreath = plugin.getEnchantRegistry().get("damage_immunity_dragon_breath");
            if (dragonBreath != null && isEquipped(entity, dragonBreath)) {
                event.setCancelled(true);
                return;
            }
        }

        String enchantId = "damage_immunity_" + event.getCause().name().toLowerCase(Locale.ROOT);
        CustomEnchant enchant = plugin.getEnchantRegistry().get(enchantId);
        if (enchant == null) return;
        if (isEquipped(entity, enchant)) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                entity.setFallDistance(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (event.getAction() == EntityPotionEffectEvent.Action.REMOVED) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (event.getNewEffect() == null) return;

        PotionEffectType type = event.getNewEffect().getType();
        String enchantId = "effect_immunity_" + type.getKey().getKey();
        CustomEnchant enchant = plugin.getEnchantRegistry().get(enchantId);
        if (enchant == null) return;
        if (isEquipped(entity, enchant)) {
            event.setCancelled(true);
        }
    }

    private boolean isEquipped(LivingEntity entity, CustomEnchant enchant) {
        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return false;
        ItemStack[] pieces = {
                eq.getHelmet(), eq.getChestplate(), eq.getLeggings(), eq.getBoots(),
                eq.getItemInOffHand(), eq.getItemInMainHand()
        };
        for (ItemStack piece : pieces) {
            if (plugin.getEnchantPDC().getLevel(piece, enchant) > 0) return true;
        }
        return false;
    }
}
