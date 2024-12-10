package org.betterbox.betterQuests;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventManager implements Listener {
    private final BetterQuests betterQuests;
    private final PluginLogger pluginLogger;
    private final ConfigManager configManager;

    public EventManager(PluginLogger pluginLogger, BetterQuests betterQuests, ConfigManager configManager) {
        this.pluginLogger = pluginLogger;
        this.betterQuests = betterQuests;
        this.configManager =configManager;
    }
    /*
    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            PersistentDataContainer pdc = villager.getPersistentDataContainer();
            if (pdc.has(betterQuests.getVillagerKey(), PersistentDataType.STRING)) {
                AttributeInstance attribute = villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (attribute != null) {
                    attribute.setBaseValue(0);
                }
                villager.setAI(false);
                villager.setInvulnerable(true);
                villager.setCollidable(false);
            }
        }
    }

     */
    @EventHandler
    public void onVillagerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            PersistentDataContainer pdc = villager.getPersistentDataContainer();
            String tag = pdc.get(betterQuests.getVillagerKey(), PersistentDataType.STRING);
            if (tag != null && tag.equals("betterquests")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        String transactionID = UUID.randomUUID().toString();
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerInteractEntity called with parameters: "+event);
            // Sprawdzenie, czy Villager ma odpowiednią nazwę, np. "Quest Villager"
            if (villager.getCustomName() != null ) {
                PersistentDataContainer pdc = villager.getPersistentDataContainer();
                String tag = pdc.get(betterQuests.getVillagerKey(), PersistentDataType.STRING);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerInteractEntity villager tags: "+tag);
                betterQuests.sendLogToElasticsearch("EventManager.onPlayerInteractEntity villager tags: "+tag, "DEBUG");
                if (tag != null && tag.equals("betterquests")) {
                    if(event.getPlayer().isSneaking() && event.getPlayer().isOp()){
                        villager.remove();
                    }
                    Player player = event.getPlayer();
                    if (checkAndRemoveItems(player)) {
                        showRewardItems(player);
                        player.getInventory().addItem(betterQuests.rewardItem);
                        betterQuests.sendLogToElasticsearch("EventManager.onPlayerInteractEntity rewarding player: " + player + " with: " + betterQuests.rewardItem, "DEBUG");
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerInteractEntity rewarding player: " + player + " with: " + betterQuests.rewardItem);
                        // Odtwarzanie dźwięku akceptacji i wyświetlanie cząsteczek
                        villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        villager.getWorld().spawnParticle(Particle.HEART, villager.getLocation().add(0, 1, 0), 10);

                    } else {
                        showRequiredItems(player);
                        // Odtwarzanie dźwięku odmowy i wyświetlanie cząsteczek
                        villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        villager.getWorld().spawnParticle(Particle.SMOKE_NORMAL, villager.getLocation().add(0, 1, 0), 10);

                    }
                }
                //openNPCDialog(player);
            }
        }
    }
    private void showRequiredItems(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.showRequiredItems called with parameters: "+player);
        DecimalFormat df = new DecimalFormat("#.##");

        Duration fadeIn = Duration.ofMillis(300);  // czas pojawiania się
        Duration stay = Duration.ofMillis(2000);    // czas wyświetlania
        Duration fadeOut = Duration.ofMillis(300); // czas znikania
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Component TitleComponent;
        TitleComponent = Component.text(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Not enough items! Required items:");
        // Inicjalizacja StringBuilder

        Component SubtitleComponent = Component.text(ChatColor.GOLD +configManager.getActiveRequiredItemStacksString());
        // Notify the killer
        Title killerTitle = Title.title(TitleComponent,SubtitleComponent,times);
        player.showTitle(killerTitle);
        player.sendMessage(ChatColor.DARK_RED+ ""+ChatColor.BOLD + "Not enough items! Required items: "+configManager.getActiveRequiredItemStacksString());
    }

    private void showRewardItems(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event: notifyPlayersAboutPoints called with parameters: "+player);
        DecimalFormat df = new DecimalFormat("#.##");

        Duration fadeIn = Duration.ofMillis(300);  // czas pojawiania się
        Duration stay = Duration.ofMillis(5000);    // czas wyświetlania
        Duration fadeOut = Duration.ofMillis(300); // czas znikania
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Component TitleComponent;
        ItemStack item = betterQuests.rewardItem;
        String name =  betterQuests.rewardItem.getType().name().replace("_", " ").toLowerCase();
        if(item.hasItemMeta() && item.getItemMeta().displayName() != null){
            // Używanie PlainTextComponentSerializer do konwersji Component na String
            name = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName())+" x"+item.getAmount();
        }

        TitleComponent = Component.text(ChatColor.GREEN + "" + ChatColor.BOLD + "Reward: "+name);
        Component SubtitleComponent = Component.text(ChatColor.GOLD +"Removed: "+configManager.getActiveRequiredItemStacksString());
        // Notify the killer
        Title killerTitle = Title.title(TitleComponent,SubtitleComponent,times);
        player.showTitle(killerTitle);
    }
    public boolean checkAndRemoveItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        Map<Material, Integer> neededItems = new HashMap<>();

        // Zliczanie potrzebnych przedmiotów
        for (ItemStack requiredItem : configManager.activeRequiredItemStacks) {
            Material material = requiredItem.getType();
            int amount = requiredItem.getAmount();
            neededItems.put(material, neededItems.getOrDefault(material, 0) + amount);
        }

        // Sprawdzanie, czy gracz ma wszystkie potrzebne przedmioty w odpowiedniej ilości
        for (Map.Entry<Material, Integer> entry : neededItems.entrySet()) {
            if (inventory.contains(entry.getKey(), entry.getValue())) {
                continue;
            } else {
                return false; // Gracz nie ma wystarczającej ilości któregoś z przedmiotów
            }
        }

        // Usuwanie przedmiotów, jeśli gracz ma wszystkie potrzebne
        for (Map.Entry<Material, Integer> entry : neededItems.entrySet()) {
            inventory.removeItem(new ItemStack(entry.getKey(), entry.getValue()));
        }

        return true;
    }

    private void openNPCDialog(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "BetterQuests");

        ItemStack accept = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + "A");
        accept.setItemMeta(acceptMeta);

        ItemStack decline = new ItemStack(Material.RED_WOOL, 1);
        ItemMeta declineMeta = decline.getItemMeta();
        declineMeta.setDisplayName(ChatColor.RED + "Odrzuć");
        decline.setItemMeta(declineMeta);

        inv.setItem(11, accept);
        inv.setItem(15, decline);

        player.openInventory(inv);
        player.sendMessage(ChatColor.GOLD + "NPC mówi: " + ChatColor.WHITE + "Czy chcesz zaakceptować ten quest? Jest to bardzo ważna misja!");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("BetterQuests")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (slot == 11) {  // Slot "akceptuj"
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Zaakceptowałeś quest!");
                // Tutaj można dodać kod rozpoczynający quest
            } else if (slot == 15) {  // Slot "odrzuc"
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Odrzuciłeś quest.");
                // Tutaj można obsłużyć odrzucenie questa
            }
        }
    }

}
