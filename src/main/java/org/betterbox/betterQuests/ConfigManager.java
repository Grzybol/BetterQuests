package org.betterbox.betterQuests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {
    private JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private File configFile = null;
    List<String> logLevels = null;
    List<String> requiredItems = null;
    List<ItemStack> requiredItemStacks = new ArrayList<>();
    ItemStack rewardItem = null;
    Set<PluginLogger.LogLevel> enabledLogLevels;
    private Map<Integer, String> rankHierarchy;
    public List<ItemStack> activeRequiredItemStacks= new ArrayList<>();
    private BetterQuests betterQuests;
    String folderPath;

    public ConfigManager(JavaPlugin plugin, PluginLogger pluginLogger, String folderPath, BetterQuests betterQuests) {
        this.folderPath=folderPath;
        this.plugin = plugin;
        this.betterQuests=betterQuests;
        this.pluginLogger = pluginLogger;
        this.rankHierarchy = new LinkedHashMap<>();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: calling configureLogger");
        configureLogger();

    }
    private void CreateExampleConfigFile(String folderPath){
        File exampleConfigFile = new File(folderPath, "config.yml");
        try (InputStream in = plugin.getResource("exampleFiles/config.yml")) {
            if (in == null) {
                plugin.getLogger().severe("Resource 'exampleFiles/config.yml not found.");
                return;
            }
            Files.copy(in, exampleConfigFile.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml to " + exampleConfigFile + ": " + e.getMessage());
        }
    }

    private void configureLogger() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: configureLogger called");
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            CreateExampleConfigFile(folderPath);
        }
        ReloadConfig();
    }
    public void ReloadConfig(){
        String folderPath = plugin.getDataFolder().getAbsolutePath();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig called");
        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.reloadConfig();
        logLevels = plugin.getConfig().getStringList("log_level");
        enabledLogLevels = new HashSet<>();
        if (logLevels == null || logLevels.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"ConfigManager: ReloadConfig: no config file or no configured log levels! Saving default settings.");
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            updateConfig("log_level:\n  - INFO\n  - WARNING\n  - ERROR");

        }
        for (String level : logLevels) {
            try {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig: adding "+level.toUpperCase());
                enabledLogLevels.add(PluginLogger.LogLevel.valueOf(level.toUpperCase()));
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig: current log levels: "+ Arrays.toString(enabledLogLevels.toArray()));

            } catch (IllegalArgumentException e) {
                // Jeśli podano nieprawidłowy poziom logowania, zaloguj błąd
                plugin.getServer().getLogger().warning("Invalid log level in config: " + level);
            }
        }

        //REQUIRED ITEMS SECTION
        int numberOfDifferentResources = 3;
        int quantityOfResources= 64;
        int refreshTimeInMinutes = 30;
        requiredItems=new ArrayList<>();
        requiredItemStacks=new ArrayList<>();
        rewardItem=null;
        String rewardItemPath =null;



        YamlConfiguration configData = YamlConfiguration.loadConfiguration(configFile);
        if(configData.contains("numberOfDifferentResources")){
            numberOfDifferentResources = configData.getInt("numberOfDifferentResources");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager.ReloadConfig loaded numberOfDifferentResources:" + numberOfDifferentResources);
        }
        if(configData.contains("quantityOfResources")){
            quantityOfResources = configData.getInt("quantityOfResources");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager.ReloadConfig loaded quantityOfResources:" + quantityOfResources);
        }
        if(configData.contains("refreshTimeInMinutes")){
            refreshTimeInMinutes = configData.getInt("refreshTimeInMinutes");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager.ReloadConfig loaded refreshTimeInMinutes:" + refreshTimeInMinutes);
        }

        requiredItems = plugin.getConfig().getStringList("requiredItems");
        if (requiredItems == null || requiredItems.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"ConfigManager: ReloadConfig: no config file or no configured required items! Saving default settings.");
            //CreateExampleConfigFile(folderPath);
            return;
        }

        if(configData.contains("rewardItemPath")){
            rewardItemPath = configData.getString("rewardItemPath");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager.ReloadConfig loaded rewardItemPath:" + rewardItemPath);
            betterQuests.rewardItem=loadItemStackFromFile(rewardItemPath);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager.ReloadConfig loaded rewardItemStack, current betterQuests.rewardItem:" + betterQuests.rewardItem);
        }else{
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "ConfigManager.ReloadConfig rewardItemPath not specified in config file!");
        }
        for (String requiredItem : requiredItems) {
            try {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"CConfigManager.ReloadConfig loading requiredItem "+requiredItem);
                addItemStack(requiredItem,quantityOfResources);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager.ReloadConfig current log levels: "+ Arrays.toString(enabledLogLevels.toArray()));

            } catch (IllegalArgumentException e) {
                // Jeśli podano nieprawidłowy poziom logowania, zaloguj błąd
                plugin.getServer().getLogger().warning("Invalid item level in config: " + e);
            }
        }
        scheduleRandomSelection(refreshTimeInMinutes,numberOfDifferentResources);


        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager.ReloadConfig calling pluginLogger.setEnabledLogLevels(enabledLogLevels) with parameters: "+ Arrays.toString(enabledLogLevels.toArray()));
        // Ustawienie aktywnych poziomów logowania w loggerze
        pluginLogger.setEnabledLogLevels(enabledLogLevels);
    }
    public String getActiveRequiredItemStacksString(){
        StringBuilder subtitleBuilder = new StringBuilder();

        for (ItemStack itemStack : activeRequiredItemStacks){
            String name = itemStack.getType().name().replace("_", " ").toLowerCase();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.showRequiredItems checking: "+itemStack);

            // Dodanie nazwy do StringBuildera wraz z przecinkiem i spacją
            subtitleBuilder.append(name).append(" x"+itemStack.getAmount()+", ");
        }
        // Konwersja StringBuildera do Stringa
        String subtitleString;
        if (subtitleBuilder.length() > 0) {
            // Usunięcie ostatniego przecinka i spacji
            return subtitleString = subtitleBuilder.substring(0, subtitleBuilder.length() - 2);
        } else {
            return subtitleString = ""; // Ustawienie pustego stringa, jeśli brak nazw
        }
    }
    public ItemStack loadItemStackFromFile(String relativeFilePath) {
        try {
            File file = new File(plugin.getDataFolder(), relativeFilePath);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Loading ItemStack from file: " + file.getAbsolutePath()+", provided relativeFilePath: "+relativeFilePath);

            if (!file.exists()) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadItemStackFromFile, error: file doesn't exist. filePath: " + file.getAbsolutePath());
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            ItemStack itemStack = config.getItemStack("item");
            if (itemStack == null) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadItemStackFromFile, error: itemStack=null. filePath: " + file.getAbsolutePath());
                return null;
            }
            return itemStack;
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadItemStackFromFile, filePath: " + relativeFilePath + ", error: " + e.getMessage());
            return null;
        }
    }


    public void addItemStack(String materialName, int amount) {
        // Sprawdzenie czy podany materiał istnieje w enumie Material
        Material material = Material.matchMaterial(materialName);
        if (material != null) {
            // Tworzenie nowego ItemStack z określonym materiałem i ilością
            ItemStack itemStack = new ItemStack(material, amount);
            // Dodanie ItemStack do listy
            requiredItemStacks.add(itemStack);
        } else {
            // Obsługa sytuacji, gdy nazwa materiału jest nieprawidłowa
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"addItemStack, no material found for materialName: "+materialName);
        }
    }
    public void updateConfig(String configuration) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: updateConfig called with parameters "+ configuration);
        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));

            // Dodaj nowe zmienne konfiguracyjne
            lines.add("###################################");
            lines.add(configuration);
            // Tutaj możemy dodać nowe zmienne konfiguracyjne
            // ...

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
    }
    public void scheduleRandomSelection(int minutes, int numberOfDiffMaterials) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Sprawdzamy czy lista zawiera wystarczająco elementów
                if (requiredItemStacks.size() < numberOfDiffMaterials) {
                    System.err.println("Nie ma wystarczająco elementów w requiredItemStacks, aby wylosować " + numberOfDiffMaterials + " elementów.");
                    return;
                }

                // Czyszczenie starej listy
                activeRequiredItemStacks.clear();

                // Kopia listy, żeby móc ją bezpiecznie tasować
                List<ItemStack> shuffledList = new ArrayList<>(requiredItemStacks);
                Collections.shuffle(shuffledList);

                // Dodawanie wylosowanych elementów do activeRequiredItemStacks
                for (int i = 0; i < numberOfDiffMaterials; i++) {
                    activeRequiredItemStacks.add(shuffledList.get(i));
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("BetterQuests"), 0, 20 * 60 * minutes);
    }
}
