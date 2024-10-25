package org.betterbox.betterQuests;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileManager {
    private final JavaPlugin plugin;
    private final BetterQuests betterQuests;
    private File dataFile;
    private FileConfiguration dataConfig;
    private PluginLogger pluginLogger;

    public FileManager(String folderPath, JavaPlugin plugin, BetterQuests betterQuests, PluginLogger pluginLogger) {
        // pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onEntityDamageByEntity
        this.plugin = plugin;
        this.betterQuests = betterQuests;
        this.pluginLogger = pluginLogger;
        File logFolder = new File(folderPath, "logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        dataFile = new File(logFolder, "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
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
