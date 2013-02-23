package net.aufdemrand.superchest;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.superchest.denizen.sChest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class SuperChest extends JavaPlugin {



    @Override
    public void onEnable() {

        // Hook denizen
        sChest.denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

        sChest.debug(ChatColor.LIGHT_PURPLE + "+-- SuperChest --------------------+");
        sChest.debug(ChatColor.YELLOW + "    _            _");
        sChest.debug(ChatColor.YELLOW + "   /_`   _  _  _/ `/_ _   __/_");
        sChest.debug(ChatColor.YELLOW + "  ._//_//_//_'//_,/ //_'_\\ / ");
        sChest.debug(ChatColor.YELLOW + "       /   " + ChatColor.DARK_GRAY + " by aufdemrand");
        sChest.debug(ChatColor.LIGHT_PURPLE + "+-- brought to you by VindiCraft --+");

        // Load config
        saveDefaultConfig();
        reloadConfig();

        // Register classes extending Listener
        getServer().getPluginManager().registerEvents(new Chests(), this);
        getServer().getPluginManager().registerEvents(new Loots(), this);

        // Create loots.yml/chests.yml if it doesn't exist
        saveResource("loots.yml", false);
        saveResource("chests.yml", false);

        LootsConfig.reloadLoots();
        ChestsConfig.reloadChests();
    }

    @Override
    public void onDisable() {

        // Save loots to disk before disabling
        LootsConfig.saveLoots();

        // Despawn super chests
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {

        // Console commands, sender is NOT a Player
        if (!(sender instanceof Player)) {



        }

        // Player commands
        if (sender instanceof Player) {
            // Get player object from sender
            Player player = (Player) sender;

            // Build the command
            String command = "";
            for (String arg : args) command = command + arg.toUpperCase() + " ";
            command = command.replace("\"", "").replace("'", "").trim();

            if (command.startsWith("LOOTS --EDIT ")
                    || command.startsWith("LOOT --EDIT ")
                    || command.startsWith("LOOTS --CREATE ")
                    || command.startsWith("LOOT --CREATE ")) {

                if (command.split(" ", 3).length < 3) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Must specify a loot to edit! Not enough arguments.");
                }

                // Get the loot name -- this is the argument after --open
                String lootName = command.split(" ", 3)[2];
                // Put the loot in a chest interface
                Loots.openLootWindow(lootName, player);
                player.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Now editing LOOT: " + ChatColor.YELLOW + lootName);

                return true;
            }

            else if (command.startsWith("LOOTS --REMOVE ")
                    || command.startsWith("LOOT --REMOVE ")) {

                if (command.split(" ", 3).length < 3) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Must specify a loot to edit! Not enough arguments.");
                }

                // Get the loot name -- this is the argument after --remove
                String lootName = command.split(" ", 3)[2];
                // Remove the loot entry if it exists.
                Loots.removeLoot(lootName);
                player.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Loot removed: " + ChatColor.YELLOW + lootName);

                return true;
            }

            else if (command.startsWith("LOOTS --LIST")
                    || command.startsWith("LOOT --LIST")) {
                player.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Listing all defined LOOTS: " + ChatColor.YELLOW
                        + Loots.getLootIds().toString());
                return true;
            }

            else if (command.startsWith("LOOTS")
                    || command.startsWith("LOOT")) {

                Inventory loots = Bukkit.getServer().createInventory(null, InventoryType.CHEST);
                // Check if this loot exists:

                Set<String> all_loots = LootsConfig.getLoots().getConfigurationSection("loots")
                        .getKeys(false);

                if (all_loots == null || all_loots.size() == 0) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "No loots defined! Use '/schest loot --edit loot name' "
                            + "to create one.");
                    return true;
                }

                String filterName = null;

                if (command.startsWith("LOOTS --FILTER ")
                        || command.startsWith("LOOT --FILTER "))
                    filterName = command.split(" ", 3)[2];

                if (filterName == null)
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Now displaying as many LOOTS as possible. To filter the results,"
                            + "use the command '/schest loots --filter [string]'.");
                else
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Now displaying as many LOOTS with a filter of '" + filterName + ".");

                int x = 0;
                for (String loot_name : all_loots) {
                    if (filterName == null
                            || loot_name.toUpperCase().contains(filterName.toUpperCase())) {
                        if (x > 27) break;
                        loots.addItem(Loots.getLootConfiguration(loot_name));
                        x++;
                    }
                }

                player.openInventory(loots);
            }

            else if (command.startsWith("CHESTS --PLACE ")
                    || command.startsWith("CHEST --PLACE ")) {

                if (command.split(" ", 3).length < 3) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Must specify a loot to edit! Not enough arguments.");
                }

                // Get the chest name -- this is the argument after --name
                String chestName = command.split(" ", 3)[2];


                if (!Chests.isValidSuperChestId(chestName)) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Not a valid Super Chest: " + ChatColor.YELLOW + chestName);
                    return true;

                } else {

                    ItemStack wand = getWandFor(chestName);

                    if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
                        player.setItemInHand(wand);
                    else player.getWorld().dropItem(player.getLocation(), wand);

                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "With the wand, right click a block to add a Super Chest: " + ChatColor.YELLOW + chestName);
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "To remove a chest from the world, sneak and right click a Super Chest with any wand.");
                }
            }

            else if (command.startsWith("CHESTS --LIST")
                    || command.startsWith("CHEST --LIST")) {
                dB.echoDebug(ChestsConfig.getChests().saveToString());
                Set<String> chests = ChestsConfig.getChests().getKeys(false);

                player.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Listing all defined CHESTS: " + ChatColor.YELLOW
                        + chests.toString());

                return true;
            }

            else if (command.startsWith("CHESTS --EDIT ")
                    || command.startsWith("CHEST --EDIT ")
                    || command.startsWith("CHESTS --CREATE ")
                    || command.startsWith("CHEST --CREATE ")) {

                if (command.split(" ", 3).length < 3) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Must specify a chest to edit! Not enough arguments.");
                }

                // Get name of the chest
                String chestName = command.split(" ", 3)[2];

                // Get the book config
                ItemStack bookConfig = Chests.getChestConfiguration(chestName);

                // Place book in hands
                if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
                    player.setItemInHand(bookConfig);
                else player.getWorld().dropItem(player.getLocation(), bookConfig);

                player.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Open the book to edit: " + ChatColor.YELLOW + chestName);

                return true;
            }

            else if (command.startsWith("CHESTS --SAVE")
                    || command.startsWith("CHEST --SAVE")) {

                if (Chests.saveFromItemConfiguration(player.getItemInHand())) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Saved chest configuration: " + ChatColor.YELLOW
                            + player.getItemInHand().getItemMeta().getLore().get(1).split(":")[1].trim());
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Invalid SuperChest configuration!");
                }
               return true;
            }

            else if (command.startsWith("CHEST")) {

                Inventory chests = Bukkit.getServer().createInventory(null, InventoryType.CHEST);
                // Check if this loot exists:

                Set<String> all_chests = ChestsConfig.getChests().getKeys(false);

                if (all_chests == null || all_chests.size() == 0) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "No chests found! Use '/schest chest --edit chest name' "
                            + "to create one.");
                    return true;
                }

                String filterName = null;

                if (command.startsWith("CHESTS --FILTER ")
                        || command.startsWith("CHEST --FILTER "))
                    filterName = command.split(" ", 3)[2];

                if (filterName == null)
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Now displaying as many CHESTS as possible. To filter the results,"
                            + "use the command '/schest chests --filter name'.");
                else
                    player.sendMessage(ChatColor.LIGHT_PURPLE
                            + "Now displaying as many CHESTS with a filter of '" + filterName + ".");

                int x = 0;
                for (String chest_name : all_chests) {
                    if (filterName == null
                            || chest_name.toUpperCase().contains(filterName.toUpperCase())) {
                        if (x > 27) break;
                        chests.addItem(Chests.getChestConfiguration(chest_name));
                        chests.addItem(getWandFor(chest_name));
                        x++;
                    }
                }

                player.openInventory(chests);
                return true;
            }

            player.sendMessage(ChatColor.LIGHT_PURPLE + "SuperChest commands:");
            player.sendMessage(ChatColor.YELLOW + "To view loots:");
            player.sendMessage(ChatColor.YELLOW + "/schest loots");
            player.sendMessage(ChatColor.YELLOW + "/schest loots --filter [string]");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "To define/edit/remove loots:");
            player.sendMessage(ChatColor.YELLOW + "/schest loot --create [loot_name]");
            player.sendMessage(ChatColor.YELLOW + "/schest loot --edit [loot_name]");
            player.sendMessage(ChatColor.YELLOW + "/schest loot --remove [loot_name]");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "To view chests:");
            player.sendMessage(ChatColor.YELLOW + "/schest chests");
            player.sendMessage(ChatColor.YELLOW + "/schest chests --filter [string]");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "To define/edit/remove/save SuperChests:");
            player.sendMessage(ChatColor.YELLOW + "/schest chest --create [chest_name]");
            player.sendMessage(ChatColor.YELLOW + "/schest chest --edit [chest_name]");
            player.sendMessage(ChatColor.YELLOW + "/schest chest --remove [chest_name]");
            player.sendMessage(ChatColor.YELLOW + "/schest chest --save");
            return true;
        }

        return true;
    }

    private ItemStack getWandFor(String id) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("Place SuperChest '" + id.toUpperCase() + "'");
        wand.setItemMeta(meta);
        return wand;
    }

}
