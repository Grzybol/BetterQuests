package org.betterbox.betterQuests;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

public final class BetterQuests extends JavaPlugin {
    FileManager fileManager;
    ConfigManager configManager;
    EventManager eventManager;
    private PluginLogger pluginLogger;
    private String folderPath;
    YamlConfiguration generatorsConfig;

    @Override
    public void onEnable() {
        //getServer().getPluginManager().registerEvents(this, this);
        java.util.logging.Logger logger = this.getLogger();

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
        configManager = new ConfigManager(this, pluginLogger, folderPath);
        fileManager = new FileManager(getDataFolder().getAbsolutePath(),this,this,pluginLogger);
        getCommand("bq").setExecutor(new CommandManager(this,this,fileManager,pluginLogger,configManager));
        eventManager = new EventManager(pluginLogger,this);
        getServer().getPluginManager().registerEvents(eventManager, this);
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Starting startGeneratorsScheduler and loadGenerators()");
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Generators loaded, starting schedulers");
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Schedulers started");
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Plugin enabled");
        logger.info("[BetterQuest] Running");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
