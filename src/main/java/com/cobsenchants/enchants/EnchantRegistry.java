package com.cobsenchants.enchants;

import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds and stores every CustomEnchant. The Effect / Effect Attack / Effect Immunity /
 * Damage Immunity categories are generated dynamically from PotionEffectType and
 * DamageCause so that every effect and every damage type in the game gets covered
 * automatically, instead of hand-listing dozens of near-identical enchants.
 */
@SuppressWarnings("deprecation")
public class EnchantRegistry {

    private final Map<String, CustomEnchant> byId = new LinkedHashMap<>();
    private final Map<EnchantCategory, List<CustomEnchant>> byCategory = new EnumMap<>(EnchantCategory.class);

    private final Map<String, PotionEffectType> effectTypeByEnchantId = new HashMap<>();
    private final Map<String, EntityDamageEvent.DamageCause> damageCauseByEnchantId = new HashMap<>();

    public EnchantRegistry() {
        for (EnchantCategory cat : EnchantCategory.values()) {
            byCategory.put(cat, new ArrayList<>());
        }
        registerSpecials();
        registerUtility();
        registerEffectEnchants();
        registerEffectAttackEnchants();
        registerEffectImmunityEnchants();
        registerDamageImmunityEnchants();
        linkEffectConflicts();
    }

    private void register(CustomEnchant e) {
        byId.put(e.getId(), e);
        byCategory.get(e.getCategory()).add(e);
    }

    private void registerSpecials() {
        register(new CustomEnchant("damage_evasion", "§bDamage Evasion", 3, EnchantCategory.SPECIAL,
                Material.SHIELD, "Chance per level to fully evade incoming damage (25% per level, capped at 75%)."));
        register(new CustomEnchant("double_jump", "§bDouble Jump", 1, EnchantCategory.SPECIAL,
                Material.FEATHER, "Jump again mid-air to launch yourself in the direction you are looking."));
        register(new CustomEnchant("true_damage", "§bTrue Damage", 5, EnchantCategory.SPECIAL,
                Material.NETHERITE_SWORD, "Deals 1 extra true damage heart per level to players you hit, ignoring armor."));
        register(new CustomEnchant("lifesteal_attack", "§bLifesteal Attack", 1, EnchantCategory.SPECIAL,
                Material.REDSTONE, "Steals 2 hearts from your target on hit and heals you for 2 hearts."));
        register(new CustomEnchant("lightning_attack", "§bLightning Attack", 1, EnchantCategory.SPECIAL,
                Material.LIGHTNING_ROD, "In a weapon: strikes lightning on whoever you hit. In armor/offhand: strikes lightning on whoever hits you."));
    }

    private void registerUtility() {
        register(new CustomEnchant("invis_armour", "§bInvis Armour", 1, EnchantCategory.UTILITY,
                Material.GLASS, "Hides this armor piece visually on your body while keeping its protection - other players won't see it."));
        register(new CustomEnchant("blacksmith", "§bBlacksmith Enchant", 1, EnchantCategory.UTILITY,
                Material.SMITHING_TABLE, "Shift + right-click to scroll the trim material, shift + left-click to scroll the trim pattern, on any piece with this enchant."));
    }

    private String niceEffectName(PotionEffectType type) {
        String key = type.getKey().getKey();
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private void registerEffectEnchants() {
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type == null) continue;
            String id = "effect_" + type.getKey().getKey();
            String name = niceEffectName(type);
            CustomEnchant e = new CustomEnchant(id, "§d" + name + " Effect", 10, EnchantCategory.EFFECT,
                    Material.POTION, "Grants " + name + " (level = amplifier) while worn or held anywhere.");
            register(e);
            effectTypeByEnchantId.put(id, type);
        }
    }

    private void registerEffectAttackEnchants() {
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type == null) continue;
            String id = "effect_attack_" + type.getKey().getKey();
            String name = niceEffectName(type);
            CustomEnchant e = new CustomEnchant(id, "§c" + name + " Attack", 1, EnchantCategory.EFFECT_ATTACK,
                    Material.IRON_SWORD, "In a weapon: applies " + name + " to whoever you hit. In armor/offhand: applies " + name + " to whoever hits you.");
            register(e);
            effectTypeByEnchantId.put(id, type);
        }
    }

    private void registerEffectImmunityEnchants() {
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type == null) continue;
            String id = "effect_immunity_" + type.getKey().getKey();
            String name = niceEffectName(type);
            CustomEnchant e = new CustomEnchant(id, "§a" + name + " Immunity", 1, EnchantCategory.EFFECT_IMMUNITY,
                    Material.MILK_BUCKET, "Immune to the " + name + " effect being applied while worn/held.");
            register(e);
            effectTypeByEnchantId.put(id, type);
        }
    }

    private String niceCauseName(EntityDamageEvent.DamageCause cause) {
        String[] parts = cause.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase(Locale.ROOT)).append(' ');
        }
        return sb.toString().trim();
    }

    private void registerDamageImmunityEnchants() {
        for (EntityDamageEvent.DamageCause cause : EntityDamageEvent.DamageCause.values()) {
            String id = "damage_immunity_" + cause.name().toLowerCase(Locale.ROOT);
            String name = niceCauseName(cause);
            String description = "Immune to " + name + " damage while worn/held (helmet/chest/legs/boots/offhand/mainhand).";

            // CONTACT is vanilla's umbrella cause for touching cactus/sweet berry bushes - give it a
            // clearer name than the raw enum would produce.
            if (cause == EntityDamageEvent.DamageCause.CONTACT) {
                name = "Cactus & Berry Bush";
                description = "Immune to cactus and sweet berry bush contact damage while worn/held.";
            }

            CustomEnchant e = new CustomEnchant(id, "§e" + name + " Immunity", 1, EnchantCategory.DAMAGE_IMMUNITY,
                    Material.TOTEM_OF_UNDYING, description);
            register(e);
            damageCauseByEnchantId.put(id, cause);
        }
    }

    /** Self-Effect X and Immunity-to-X don't make sense together, so they conflict like protection/fire protection. */
    private void linkEffectConflicts() {
        for (Map.Entry<String, PotionEffectType> entry : new HashMap<>(effectTypeByEnchantId).entrySet()) {
            String id = entry.getKey();
            if (!id.startsWith("effect_") || id.startsWith("effect_attack_") || id.startsWith("effect_immunity_")) continue;
            PotionEffectType type = entry.getValue();
            String immunityId = "effect_immunity_" + type.getKey().getKey();
            CustomEnchant effectEnchant = byId.get(id);
            CustomEnchant immunityEnchant = byId.get(immunityId);
            if (effectEnchant != null && immunityEnchant != null) {
                effectEnchant.addConflict(immunityId);
                immunityEnchant.addConflict(id);
            }
        }
    }

    public CustomEnchant get(String id) {
        return byId.get(id);
    }

    public Collection<CustomEnchant> all() {
        return byId.values();
    }

    public List<CustomEnchant> byCategory(EnchantCategory cat) {
        return byCategory.get(cat);
    }

    public PotionEffectType effectTypeFor(String enchantId) {
        return effectTypeByEnchantId.get(enchantId);
    }

    public EntityDamageEvent.DamageCause damageCauseFor(String enchantId) {
        return damageCauseByEnchantId.get(enchantId);
    }
}
