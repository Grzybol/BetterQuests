package org.betterbox.betterQuests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventManager implements Listener {
    private final BetterQuests betterQuests;
    private final PluginLogger pluginLogger;

    public EventManager(PluginLogger pluginLogger, BetterQuests betterQuests) {
        this.pluginLogger = pluginLogger;
        this.betterQuests = betterQuests;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            // Sprawdzenie, czy Villager ma odpowiednią nazwę, np. "Quest Villager"
            if (villager.getCustomName() != null && villager.getCustomName().equals(ChatColor.GOLD + "Quest Villager")) {
                Player player = event.getPlayer();
                openNPCDialog(player);
            }
        }
    }

    private void openNPCDialog(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "BetterQuests");

        ItemStack accept = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + "Akceptuj");
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
