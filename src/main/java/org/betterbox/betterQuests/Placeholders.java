package org.betterbox.betterQuests;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {
    private final BetterQuests plugin;
    private final ConfigManager configManager;
    private final Lang lang;

    public Placeholders(BetterQuests plugin, ConfigManager configManager, Lang lang) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.lang = lang;
    }

    @Override
    public boolean canRegister() {
        return plugin.isEnabled();
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public String getIdentifier() {
        return "bq"; // Prefix Twoich placeholderów, np. %betterquests_placeholder%
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    public String formatTime(long milliseconds) {
        if (milliseconds < 0) {
            return lang.timeIsUp;
        }

        long totalSeconds = milliseconds / 1000;
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder timeString = new StringBuilder();

        if (days > 0) {
            timeString.append(days).append(" ").append(lang.dayString).append(" | ");
        }
        if (hours > 0) {
            timeString.append(hours).append(" ").append(lang.hourString).append(" | ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append(" ").append(lang.minuteString).append(" | ");
        }
        if (seconds > 0 || timeString.length() == 0) {
            timeString.append(seconds).append(" ").append(lang.secondsString);
        }

        return timeString.toString().trim();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player != null) {
        }
        double points=0;
        switch (identifier) {
            case "time_left":
                long elapsedTime = System.currentTimeMillis() - plugin.lastReset;
                long remainingTime = (configManager.refreshTimeInMinutes * 60 * 1000) - elapsedTime;
                return formatTime(remainingTime);
            case "current_requirements":
                return configManager.getActiveRequiredItemStacksString();
            case "reward_item":
                if (plugin.rewardItem != null) {
                    return PlainTextComponentSerializer.plainText().serialize(plugin.rewardItem.getItemMeta().displayName())+" x"+plugin.rewardItem.getAmount();
                } else {
                    return lang.noRewardset;
                }
        }
        return null; // Zwróć null, jeśli placeholder nie jest obsługiwany
    }
}
