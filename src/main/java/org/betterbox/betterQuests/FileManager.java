package org.betterbox.betterQuests;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileManager {
    private final JavaPlugin plugin;
    private final BetterQuests betterQuests;
    private File dataFile;
    private FileConfiguration dataConfig;
    private PluginLogger pluginLogger;
    public String folderPath;

    public FileManager(String folderPath, JavaPlugin plugin, BetterQuests betterQuests, PluginLogger pluginLogger) {
        // pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onEntityDamageByEntity
        this.plugin = plugin;
        this.betterQuests = betterQuests;
        this.pluginLogger = pluginLogger;
        this.folderPath=folderPath;
        File logFolder = new File(folderPath, "logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        dataFile = new File(folderPath, "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveVillagerInfoToFile(String uuid, String name) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "FileRewardManager.saveVillagerInfoToFile called, uuid: " + uuid + ", name: " + name);
        try {
            File dataFile = new File(folderPath, "data.yml");
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            config.set(uuid + ".name", name);
            config.save(dataFile);

        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Cannot save the uuid: " + uuid + ", error: " + e.getMessage());
        }
    }
    public Map<String, String> loadAllVillagerInfoFromFile() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "FileRewardManager.loadAllVillagerInfoFromFile called");
        Map<String, String> villagerInfo = new HashMap<>();

        try {
            File dataFile = new File(folderPath, "data.yml");
            if (!dataFile.exists()) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Data file does not exist.");
                return villagerInfo;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            for (String key : config.getKeys(false)) {
                String name = config.getString(key + ".name");
                if (name != null) {
                    villagerInfo.put(key, name);
                } else {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR, "No name entry found for uuid: " + key);
                }
            }
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error loading villager info from file: " + e.getMessage());
        }

        return villagerInfo;
    }
    public void saveItemStackToFile(String fileName, ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "FileRewardManager.saveItemStackToFile called, fileName: " + fileName);
        File customRewardsFolder = new File(plugin.getDataFolder() + File.separator + "customRewards");
        if (!customRewardsFolder.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.INFO, "customRewardsFolder does not exist, creating a new one");
            customRewardsFolder.mkdirs();
        }
        try {
            File rewardFile = new File(customRewardsFolder, fileName + ".yml");
            pluginLogger.log(PluginLogger.LogLevel.INFO, "rewardItem will be saved to " + fileName + ".yml");
            rewardFile.createNewFile();
            FileConfiguration rewardFileConfig = YamlConfiguration.loadConfiguration(rewardFile);
            rewardFileConfig.set("item",itemStack);
            rewardFileConfig.save(rewardFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "rewardItem " + fileName + " saved");

        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Cannot save the item : " + fileName + ", error: " + e.getMessage());
        }
    }

}
