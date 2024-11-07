package org.betterbox.betterQuests;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        configureAllVillagers();
        logger.info("[BetterQuest] Running");

    }
    public void configureAllVillagers() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Configuring all villagers");

        Map<String, String> allVillagers = fileManager.loadAllVillagerInfoFromFile();

        for (Map.Entry<String, String> entry : allVillagers.entrySet()) {
            String uuid = entry.getKey();
            Entity entity = this.getServer().getEntity(UUID.fromString(uuid));
            if (entity != null && entity.getType() == EntityType.VILLAGER) {
                Villager villager = (Villager) entity;

                String coloredName = ChatColor.translateAlternateColorCodes('&', entry.getValue());
                villager.setCustomName(coloredName);
                villager.setCustomNameVisible(true);

                AttributeInstance attribute = villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (attribute != null) {
                    attribute.setBaseValue(0);  // Make the villager immobile
                }

                villager.setAI(true);
                villager.setInvulnerable(true);
                villager.setCollidable(false);
            } else {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Entity with UUID: " + uuid + " is not a Villager or doesn't exist");
            }
        }
    }
    public NamespacedKey getVillagerKey() {
        return villagerKey;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
