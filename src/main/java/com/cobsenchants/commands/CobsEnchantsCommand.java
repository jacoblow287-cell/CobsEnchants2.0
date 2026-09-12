package com.cobsenchants.commands;

import com.cobsenchants.CobsEnchantsPlugin;
import com.cobsenchants.gui.MainMenuGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CobsEnchantsCommand implements CommandExecutor {

    private final CobsEnchantsPlugin plugin;

    public CobsEnchantsCommand(CobsEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        MainMenuGui.open(plugin, player);
        return true;
    }
}
