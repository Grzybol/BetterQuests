package org.betterbox.betterQuests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.net.URL;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        kibanaTest();
        sendLogToElasticsearch("Plugin started", "INFO");
        NamespacedKey key = new NamespacedKey(this, "betterQuestsNPC");

        // Przeszukiwanie wszystkich światów w poszukiwaniu NPC
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(Villager.class)) {
                Villager villager = (Villager) entity;
                if (villager.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    // Ustaw ponownie właściwości NPC
                    villager.setInvulnerable(true);
                    villager.setCollidable(false);
                    villager.setAI(false); // Ustaw zgodnie z preferencjami
                    villager.setInvisible(true); // Ustaw zgodnie z preferencjami

                    // Opcjonalnie: Ustaw prędkość ruchu
                    AttributeInstance attribute = villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                    if (attribute != null) {
                        attribute.setBaseValue(0);
                    }

                    // Upewnij się, że nazwa jest widoczna
                    villager.setCustomNameVisible(true);
                }
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "All NPCs have been initialized with restricted properties.");



    }
    private static final Logger logger = LogManager.getLogger(BetterQuests.class);

    public void kibanaTest() {
        // Logowanie lokalne, informacja o rozpoczęciu wysyłania logów
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Rozpoczęcie wysyłania logów do Kibany");

        try {
            logger.trace("To jest TRACE log");
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Wysłano TRACE log do Kibany");

            logger.debug("To jest DEBUG log");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Wysłano DEBUG log do Kibany");

            logger.info("To jest INFO log");
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Wysłano INFO log do Kibany");

            logger.warn("To jest WARN log");
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Wysłano WARN log do Kibany");

            logger.error("To jest ERROR log");
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Wysłano ERROR log do Kibany");

            logger.fatal("To jest FATAL log");
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Wysłano FATAL log do Kibany");

        } catch (Exception e) {
            // Logowanie lokalne w przypadku błędu
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Błąd podczas wysyłania logów do Kibany: " + e.getMessage());
        }

        // Logowanie lokalne, informacja o zakończeniu wysyłania logów
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Zakończono wysyłanie logów do Kibany");
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

    public void sendLogToElasticsearch(String message, String level) {
        try {
            URL url = new URL("http://localhost:9200/betterquests/_doc");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            //connection.setRequestProperty("Authorization", "ApiKey cWk0VkNaTUJlNUh6SlFHbGM1OFE6NjJfZ25hc21RLUNYMTdkQVlJVFlxZw==");
            connection.setDoOutput(true);

            String jsonInputString = String.format("{\"timestamp\":\"%s\",\"level\":\"%s\",\"message\":\"%s\",\"logger\":\"com.betterquests\",\"thread\":\"main\"}",
                    java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now()), level, message);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                pluginLogger.log(PluginLogger.LogLevel.INFO, "Log successfully sent to Elasticsearch");
            } else {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Failed to send log to Elasticsearch");
            }
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error sending log to Elasticsearch: " + e.getMessage());
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
