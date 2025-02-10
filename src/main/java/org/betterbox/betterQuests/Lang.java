package org.betterbox.betterQuests;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Lang {
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    public String noRewardset = "No reward set";
    public String timeIsUp = "Time is up!";
    public String dayString = "days";
    public String hourString = "hours";
    public String minuteString = "minutes";
    public String secondsString = "seconds";
    public String notEnoughItemsRequiredItems = "Not enough items! Required items:";
    public String rewardString = "Reward: ";
    public String noPermission = " You don't have permission to do that!";



    public Lang(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        loadLangFile();
    }

    public void loadLangFile() {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Lang.loadLangFile called", transactionID);
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang directory...", transactionID);
            langDir.mkdirs();
        }

        File langFile = new File(langDir, "lang.yml");
        if (!langFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            createDefaultLangFile(langFile, transactionID);
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Loading lang.yml file...", transactionID);
        FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        validateAndLoadConfig(config, langFile, transactionID);
    }

    private void createDefaultLangFile(File langFile, String transactionID) {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            langFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
            setDefaultValues(config);
            config.save(langFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file created successfully!", transactionID);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error creating lang.yml file: " + e.getMessage(), transactionID);
        }
    }

    private void setDefaultValues(FileConfiguration config) {
        config.set("noRewardset", noRewardset);
        config.set("timeIsUp", timeIsUp);
        config.set("dayString", dayString);
        config.set("hourString", hourString);
        config.set("minuteString", minuteString);
        config.set("secondsString", secondsString);
        config.set("notEnoughItemsRequiredItems", notEnoughItemsRequiredItems);
        config.set("rewardString", rewardString);
        config.set("noPermission", noPermission);

    }

    private void validateAndLoadConfig(FileConfiguration config, File langFile, String transactionID) {
        boolean saveRequired = false;

        if (!config.contains("noRewardset")) {
            config.set("noRewardset", noRewardset);
            saveRequired = true;
        } else {
            noRewardset = config.getString("noRewardset");
        }

        if (!config.contains("timeIsUp")) {
            config.set("timeIsUp", timeIsUp);
            saveRequired = true;
        } else {
            timeIsUp = config.getString("timeIsUp");
        }

        if (!config.contains("dayString")) {
            config.set("dayString", dayString);
            saveRequired = true;
        } else {
            dayString = config.getString("dayString");
        }

        if (!config.contains("hourString")) {
            config.set("hourString", hourString);
            saveRequired = true;
        } else {
            hourString = config.getString("hourString");
        }

        if (!config.contains("minuteString")) {
            config.set("minuteString", minuteString);
            saveRequired = true;
        } else {
            minuteString = config.getString("minuteString");
        }

        if (!config.contains("secondsString")) {
            config.set("secondsString", secondsString);
            saveRequired = true;
        } else {
            secondsString = config.getString("secondsString");
        }

        if (!config.contains("notEnoughItemsRequiredItems")) {
            config.set("notEnoughItemsRequiredItems", notEnoughItemsRequiredItems);
            saveRequired = true;
        } else {
            notEnoughItemsRequiredItems = config.getString("notEnoughItemsRequiredItems");
        }

        if (!config.contains("rewardString")) {
            config.set("rewardString", rewardString);
            saveRequired = true;
        } else {
            rewardString = config.getString("rewardString");
        }

        if (!config.contains("noPermission")) {
            config.set("noPermission", noPermission);
            saveRequired = true;
        } else {
            noPermission = config.getString("noPermission");
        }


        if (saveRequired) {
            try {
                config.save(langFile);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file updated with missing values", transactionID);
            } catch (IOException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error saving lang.yml file: " + e.getMessage(), transactionID);
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file loaded successfully!", transactionID);
    }
}