package com.cobsenchants;

import com.cobsenchants.commands.CobsEnchantsCommand;
import com.cobsenchants.enchants.EnchantRegistry;
import com.cobsenchants.listeners.AnvilListener;
import com.cobsenchants.listeners.BlacksmithListener;
import com.cobsenchants.listeners.CombatListener;
import com.cobsenchants.listeners.DoubleJumpListener;
import com.cobsenchants.listeners.GuiListener;
import com.cobsenchants.listeners.ProtectionListener;
import com.cobsenchants.tasks.EffectApplierTask;
import com.cobsenchants.tasks.InvisArmorTask;
import com.cobsenchants.util.EnchantPDC;
import org.bukkit.plugin.java.JavaPlugin;

public class CobsEnchantsPlugin extends JavaPlugin {

    private static CobsEnchantsPlugin instance;

    private EnchantRegistry enchantRegistry;
    private EnchantPDC enchantPDC;

    @Override
    public void onEnable() {
        instance = this;

        this.enchantRegistry = new EnchantRegistry();
        this.enchantPDC = new EnchantPDC(this);

        var cmd = getCommand("cobsenchants");
        if (cmd != null) {
            cmd.setExecutor(new CobsEnchantsCommand(this));
        }

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new DoubleJumpListener(this), this);
        getServer().getPluginManager().registerEvents(new BlacksmithListener(this), this);

        new EffectApplierTask(this).runTaskTimer(this, 20L, 40L);
        new InvisArmorTask(this).runTaskTimer(this, 20L, 20L);

        getLogger().info("CobsEnchants enabled with " + enchantRegistry.all().size() + " custom enchants.");
    }

    public static CobsEnchantsPlugin get() {
        return instance;
    }

    public EnchantRegistry getEnchantRegistry() {
        return enchantRegistry;
    }

    public EnchantPDC getEnchantPDC() {
        return enchantPDC;
    }
}
