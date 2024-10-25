package org.betterbox.betterQuests;

import net.kyori.adventure.platform.facet.Facet;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
                sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.DARK_RED + " You don't have permission to do that!");
                return true;
            }
            configManager.ReloadConfig();
            sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.AQUA + " Configuration reloaded!");
            return true;
        }else if (args.length == 1 && args[0].equalsIgnoreCase("help")){
            if (!sender.hasPermission("betterquests.help")) {
                sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.DARK_RED + " You don't have permission to do that!");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.GREEN+" /bq npcspawn <name>");
            sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.GREEN+" /bq delete <name>");
            sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.GREEN+" /bq reload");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("npcspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "This command can only be used by players.");
                return true;
            }
            if (!sender.hasPermission("betterquests.npcspawn")) {
                sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.DARK_RED + " You don't have permission to do that!");
                return true;
            }
            Player player = (Player) sender;
            Location loc = player.getLocation();
            Villager villager = (Villager) player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            // Tłumaczenie kodów kolorów w nazwie
            String coloredName = ChatColor.translateAlternateColorCodes('&', args[1]);
            villager.setCustomName(coloredName);
            villager.setCustomNameVisible(true);
            AttributeInstance attribute = villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (attribute != null) {
                attribute.setBaseValue(0);
            }
            villager.setAI(true);  // Disable AI to make the villager stay in place
            villager.setInvulnerable(true); // Uczyń wieśniaka nieśmiertelnym

            // Dodanie niestandardowego tagu do PDC
            PersistentDataContainer pdc = villager.getPersistentDataContainer();
            pdc.set(betterQuests.getVillagerKey(), PersistentDataType.STRING, "betterQuestsNPC");

            sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests] "+ChatColor.AQUA+ " Villager NPC spawned!");
            return true;
        }else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.DARK_RED + "This command can only be used by players.");
                return true;
            }
            if (!sender.hasPermission("betterquests.npcdelete")) {
                sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.DARK_RED + "You don't have permission to do that!");
                return true;
            }

            Player player = (Player) sender;
            String villagerName = args[1];
            boolean found = false;
            player.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests] "+ChatColor.AQUA+" Shift+Right click to delete NPC");
            for (Entity entity : player.getWorld().getEntities()) {
                if (entity instanceof Villager) {
                    Villager villager = (Villager) entity;
                    if (villager.getCustomName() != null && villager.getCustomName().equalsIgnoreCase(villagerName)) {
                        villager.remove();
                        found = true;
                        sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.AQUA + "Villager '" + villagerName + "' has been deleted.");
                        break; // Usuwa pierwszy znaleziony wieśniak o podanej nazwie
                    }
                }
            }

            if (!found) {
                sender.sendMessage(ChatColor.RED + "No Villager found with the name '" + villagerName + "'.");
            }
            return true;
        }else if (args.length == 2 && args[0].equalsIgnoreCase("saveitem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.DARK_RED + "This command can only be used by players.");
                return true;
            }
            if (!sender.hasPermission("betterquests.saveitem")) {
                sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.DARK_RED + " You don't have permission to do that!");
                return true;
            }
            Player player = (Player) sender;
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            fileManager.saveItemStackToFile(args[1],itemStack);
            sender.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"[BetterQuests]"+ChatColor.AQUA + " Itemstack saved to file");
            return true;
        }
        return false;
    }
}
