package org.betterbox.betterQuests;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private final BetterQuests betterQuests;
    private final FileManager fileManager;
    private final ConfigManager configManager;
    private PluginLogger pluginLogger;
    public CommandManager(JavaPlugin plugin, BetterQuests betterQuests, FileManager fileManager, PluginLogger pluginLogger,ConfigManager configManager){
        this.configManager=configManager;
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.betterQuests = betterQuests;
        this.fileManager = fileManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "CommandManager.onCommand called, sender: " + sender + ", args: " + String.join(", ", args));
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("betterquests.reload")) {
                sender.sendMessage(ChatColor.DARK_RED + " You don't have permission to do that!");
                return true;
            }
            configManager.ReloadConfig();
            sender.sendMessage(ChatColor.AQUA + " Configuration reloaded!");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("npcspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "This command can only be used by players.");
                return true;
            }
            if (!sender.hasPermission("betterquests.npcspawn")) {
                sender.sendMessage(ChatColor.DARK_RED + " You don't have permission to do that!");
                return true;
            }
            Player player = (Player) sender;
            Location loc = player.getLocation();
            Villager villager = (Villager) player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            villager.setCustomName(ChatColor.GOLD + "Quest Villager");
            villager.setCustomNameVisible(true);
            villager.setAI(false);  // Disable AI to make the villager stay in place
            sender.sendMessage(ChatColor.GREEN + " Villager NPC spawned!");
            return true;
        }
        return false;
    }
}
