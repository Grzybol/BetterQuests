package org.betterbox.betterQuests;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

public final class BetterQuests extends JavaPlugin {
    public long lastReset;
    FileManager fileManager;
    ConfigManager configManager;
    EventManager eventManager;
    private PluginLogger pluginLogger;
    private String folderPath;
    YamlConfiguration generatorsConfig;

    public ItemStack rewardItem;
    private NamespacedKey villagerKey;
    private Placeholders placeholdersManager;

    @Override
    public void onEnable() {
        //getServer().getPluginManager().registerEvents(this, this);
        java.util.logging.Logger logger = this.getLogger();
        villagerKey = new NamespacedKey(this, "custom_villager");
        folderPath = getDataFolder().getAbsolutePath();
        logger.info("[BetterQuest] Initializing");
        logger.info("[BetterQuest] Author " + this.getDescription().getAuthors());
        logger.info("[BetterQuest] Version  " + this.getDescription().getVersion());
        logger.info("[BetterQuest] " + this.getDescription().getDescription());
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
        pluginLogger = new PluginLogger(folderPath, defaultLogLevels,this);
        folderPath =getDataFolder().getAbsolutePath();
        configManager = new ConfigManager(this, pluginLogger, folderPath,this);
        fileManager = new FileManager(getDataFolder().getAbsolutePath(),this,this,pluginLogger);
        getCommand("bq").setExecutor(new CommandManager(this,this,fileManager,pluginLogger,configManager));
        eventManager = new EventManager(pluginLogger,this, configManager);
        getServer().getPluginManager().registerEvents(eventManager, this);
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Starting startGeneratorsScheduler and loadGenerators()");
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Generators loaded, starting schedulers");
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Schedulers started");
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Plugin enabled");
        // Inicjalizacja Placeholders
        placeholdersManager = new Placeholders(this,configManager);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            boolean success = placeholdersManager.register();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterQuests: Placeholders zostały zarejestrowane w PlaceholderAPI. success="+success);
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "BetterQuests: Warning: PlaceholderAPI not found, placeholders will NOT be available.");
        }
        logger.info("[BetterQuest] Running");

    }
    public NamespacedKey getVillagerKey() {
        return villagerKey;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
