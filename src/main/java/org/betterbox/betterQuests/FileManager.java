package org.betterbox.betterQuests;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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
        this.pluginLogger= pluginLogger;
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
}
