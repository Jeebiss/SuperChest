package net.aufdemrand.superchest;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.superchest.denizen.sChest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Loots implements Listener {

    /**
     * Gets a Inventory of the loot.
     *
     * @param id  id of the loot.
     * @return  bukkit Inventory of the items.
     */
    public static Inventory getLoot(String id) {
        String lootName = id.toUpperCase();
        Inventory loot = Bukkit.getServer().createInventory(null, InventoryType.CHEST);
        // Check if this loot exists:
        if (LootsConfig.getLoots().contains("loots." + lootName)) {
            for (int x = 0; x<=26; x++) {
                if (LootsConfig.getLoots().contains("loots." + lootName + "." + x)) {
                    loot.setItem(x, LootsConfig.getLoots().getItemStack("loots." + lootName + "." + x));
                } else loot.addItem(new ItemStack(Material.AIR));
            }
        } else sChest.debug("Oh no! Tried to get LOOT '" + id + "', but it couldn't be found!");
        return loot;
    }

    public static Set<String> getLootIds() {
        return LootsConfig.getLoots().getConfigurationSection("loots")
                .getKeys(false);
    }

    /**
     * Removes a specific loot from the config.
     *
     * @param id  the id of the loot.
     */
    public static void removeLoot(String id) {
        String lootName = id.toUpperCase();
        // Check loots.yml for a valid entry.
        if (LootsConfig.getLoots().contains("loots." + lootName))
            LootsConfig.getLoots().set("loots." + lootName, null);
        // Loot removed
        LootsConfig.saveLoots();
    }

    /**
     * Opens a loot window for a Player when given a specified loot-id.
     *
     */
    public static void openLootWindow(String id, Player player) {
        dB.echoDebug(id + " - " + player.getName());
        Inventory loot = Loots.getLoot(id);
        // Open the chest interface to the Player
        player.openInventory(loot);
        // Track the window, to update the loot on close.
        loot_windows.put(player, id.toUpperCase());
    }

    /**
     * Detects if a Player has clicked on a Loot Item, and if so, opens
     * the loot editor for the specified loot.
     *
     * @param event
     */
    @EventHandler
    public static void clickOnLootItem(InventoryClickEvent event) {
        if (!event.getWhoClicked().isOp()) return;
        if ( event.getCurrentItem() != null
                && event.getCurrentItem().hasItemMeta()
                && event.getCurrentItem().getItemMeta().hasDisplayName()) {

            if (event.getCurrentItem().getItemMeta().getDisplayName().toUpperCase()
                    .startsWith("SHOW ALL CHESTS")) {
                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
                final Player player = (Player) event.getWhoClicked();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Settings.getSuperChestInstance(),
                        new Runnable() {
                            @Override
                            public void run() {
                                player.performCommand("schest chests");
                            }
                        }, 5);
            }

            else if (event.getCurrentItem().getItemMeta().getDisplayName().toUpperCase()
                    .startsWith("SHOW ALL LOOTS")) {
                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
                final Player player = (Player) event.getWhoClicked();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Settings.getSuperChestInstance(),
                        new Runnable() {
                            @Override
                            public void run() {
                                player.performCommand("schest loots");
                            }
                        }, 5);
            }

            else if (event.getCurrentItem().getItemMeta().getDisplayName().toUpperCase()
                    .startsWith("CREATE NEW LOOT '")) {
                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
                final Player player = (Player) event.getWhoClicked();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Settings.getSuperChestInstance(),
                        new Runnable() {
                            @Override
                            public void run() {
                                player.performCommand("schest loot --edit ");
                            }
                        }, 5);
            }


            else if (event.getCurrentItem().getItemMeta().getDisplayName().toUpperCase()
                    .startsWith("SUPERCHEST LOOT '")) {
                event.setCancelled(true);
                // Player (op) has clicked a loot. Open the loot window.
                event.getWhoClicked().closeInventory();
                final String loot_id = event.getCurrentItem().getItemMeta().getDisplayName().split("'")[1].replace("'", "").trim();
                final Player player = (Player) event.getWhoClicked();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Settings.getSuperChestInstance(),
                        new Runnable() {
                            @Override
                            public void run() {
                                sChest.debug("Running..." + loot_id + player.getName());
                                openLootWindow(loot_id, player);
                            }
                        }, 5);
            }


        }
    }


    /**
     * Keeps track of loot_windows in order to listen for the Player to close the loot
     * editor and save the loot.
     *
     */
    public static Map<Player, String> loot_windows = new ConcurrentHashMap<Player, String>();

    /**
     * Detects and deals with an exit of the Loot Editor
     *
     * @param event  InventoryCloseEvent
     */
    @EventHandler
    public void closeLootWindow(InventoryCloseEvent event) {
        if (loot_windows.containsKey(event.getPlayer())) {
            int x = 0;
            // Clear the current entry in loots.yml
            LootsConfig.getLoots().set("loots." + loot_windows.get(event.getPlayer()), null);
            // Save the loot window to loots.yml
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null) {
                    dB.echoDebug("loots." + loot_windows.get(event.getPlayer()) + "." + x);
                    LootsConfig.getLoots().set("loots." + loot_windows.get(event.getPlayer()) + "." + x, item);
                }
                x++;
            }
        } else return;

        ((Player) event.getPlayer()).sendMessage("Saving loot '" + loot_windows.get(event.getPlayer()) + "'.");
        // Save the loot
        LootsConfig.saveLoots();
        // Remove from map... Player has exited the Loot Editor.
        loot_windows.remove(event.getPlayer());
    }


    public static ItemStack getLootConfiguration(String id) {
        String loot_name = id.toUpperCase();

        ItemStack loot = new ItemStack(Material.CHEST);

        ItemMeta itemMeta = loot.getItemMeta();
        itemMeta.setDisplayName("SuperChest Loot '" + loot_name + "'");
        List<String> lore = new ArrayList<String>();
        lore.add("Click to edit the contents of this loot.");
        itemMeta.setLore(lore);
        loot.setItemMeta(itemMeta);

        return loot;
    }


}
