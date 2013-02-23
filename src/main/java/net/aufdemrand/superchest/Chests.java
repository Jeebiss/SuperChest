package net.aufdemrand.superchest;

import net.aufdemrand.superchest.denizen.sChest;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Chests implements Listener {

    /**
     * Currently loaded SuperChests
     */
    public static Map<Block, ChestInstance>
            super_chests = new ConcurrentHashMap<Block, ChestInstance>();


    /**
     * Checks a location to see if there is a SuperChest there
     *
     * @param location  the Location to check
     * @return  true if a superchest is already registered to that location
     */
    public static boolean isChestAtLocation(Location location) {
        for (Block block : super_chests.keySet()) {
            if (location.getBlock().equals(block))
                return true;
        }
        return false;
    }


    /**
     * Checks the chests.yml to see if the supplied id is a valid SuperChest
     *
     * @param id  the id of the chest.
     * @return  true if exists.
     */
    public static boolean isValidSuperChestId(String id) {
        if (ChestsConfig.getChests().contains(id.toUpperCase()))
            return true;
        else return false;
    }


    /**
     * Creates a new ChestInstance of a SuperChest.
     *
     * @param location  the location of the new ChestInstance
     * @param id  the id of the SuperChest
     */
    public static void placeChest(Location location, String id) {
        id = id.toUpperCase();
        location.getBlock().setType(Material.CHEST);
        try {
            super_chests.put(location.getBlock(), new ChestInstance(id, location));
        } catch (Exception e) {
            // Failed to make a chest!
            sChest.debug("Oh no! Report this to aufdemrand:");
            e.printStackTrace();
        }
    }


    /**
     * Bukkit event that handles a Player interacting with a chest
     * to loot it.
     *
     * @param event  PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void lootChest(PlayerInteractEvent event) {
        // If the event is already cancelled, don't do anything.
        if (event.isCancelled()) return;
        // If the block isn't a super chest, don't do anything.
        if (!event.getClickedBlock().getType().equals(Material.CHEST)
                || !isChestAtLocation(event.getClickedBlock().getLocation())) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Update inventory
            ((Chest) event.getClickedBlock().getState()).getBlockInventory().setContents(
                    super_chests.get(event.getClickedBlock()).getRandomInventory().getContents());
            // Remove from super_chests list
            in_progress.put(event.getPlayer(), event.getClickedBlock());
        }
    }

    private static Map<Player, Block> in_progress = new ConcurrentHashMap<Player, Block>();

    @EventHandler
    public static void lootChestByOpening(InventoryCloseEvent event) {
        if (in_progress.containsKey(event.getPlayer())) {
            // Remove and reschedule...
            super_chests.get(in_progress.get(event.getPlayer()))
                    .removeAndReschedule();

            in_progress.remove(event.getPlayer());
        }
    }

    @EventHandler
    public static void lootChestByBreakage(BlockBreakEvent event) {
        if (super_chests.containsKey(event.getBlock()))
            // Remove and reschedule...
            super_chests.get(event.getBlock()).removeAndReschedule();
    }

    /**
     * Handles placing/rotateing/removal of a SuperChest by using the wand.
     *
     * @param event
     */
    @EventHandler
    public static void placeSuperChest(PlayerInteractEvent event) {

        if (event.getAction() == Action.LEFT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_AIR) return;

        // Only continue if a Player or has SuperChest.Admin permission
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("superchest.admin")) {

            if (event.getPlayer().getItemInHand() == null
                    || !event.getPlayer().getItemInHand().hasItemMeta()
                    || !event.getPlayer().getItemInHand().getItemMeta().hasDisplayName()
                    || !event.getPlayer().getItemInHand().getItemMeta().getDisplayName()
                    .startsWith("Place SuperChest")) return;

            event.setCancelled(true);

            // Get name of the SuperChest to be placed:
            String name = event.getPlayer().getItemInHand().getItemMeta().getDisplayName()
                    .replace("Place SuperChest", "").replace("'", "").trim();

            // On left click, rotate the chest
            if (event.getAction() == Action.LEFT_CLICK_BLOCK
                    && event.getClickedBlock().getType() == Material.CHEST) {
                if (event.getPlayer().isSneaking()) {
                    if (event.getClickedBlock().getData() == (byte) 5)
                        event.getClickedBlock().setData((byte) 1);
                    else
                        event.getClickedBlock().setData((byte) (event.getClickedBlock().getData() + 1));
                } else {
                    if (event.getClickedBlock().getData() == (byte) 1)
                        event.getClickedBlock().setData((byte) 5);
                    else
                        event.getClickedBlock().setData((byte) (event.getClickedBlock().getData() - 1));
                }
                // Make chest remember data for respawn
                super_chests.get(event.getClickedBlock()).setData(event.getClickedBlock().getData());
                return;
            }

            if (event.getPlayer().isSneaking()) {
                if (super_chests.containsKey(event.getClickedBlock())) {
                    super_chests.remove(event.getClickedBlock());
                    event.getClickedBlock().setType(Material.AIR);
                    event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Removed SuperChest!");
                    return;
                }
            }

            Block b = event.getClickedBlock().getLocation().add(0, 1, 0).getBlock();

            if (b.getRelative(BlockFace.DOWN).getType() == Material.CHEST
                    || b.getRelative(BlockFace.UP).getType() == Material.CHEST
                    || b.getRelative(BlockFace.EAST).getType() == Material.CHEST
                    || b.getRelative(BlockFace.WEST).getType() == Material.CHEST
                    || b.getRelative(BlockFace.NORTH).getType() == Material.CHEST
                    || b.getRelative(BlockFace.SOUTH).getType() == Material.CHEST) {

                event.getPlayer().sendMessage(ChatColor.GRAY + "Cannot place a SuperChest this close to an existing chest!");
                return;
            }

            // Place the actual Chest
            placeChest(event.getClickedBlock().getLocation().add(0, 1, 0), name);

            // Play some effects.. why not?
            event.getPlayer().playEffect(event.getClickedBlock().getLocation().add(0, 2, 0),
                    Effect.ENDER_SIGNAL, 10000);
            event.getPlayer().playSound(event.getClickedBlock().getLocation().add(0, 1, 0),
                    Sound.CHEST_OPEN, 1, 1);
        }

    }

    public static boolean saveFromItemConfiguration(ItemStack item) {

        ItemMeta itemMeta = item.getItemMeta();
        BookMeta bookMeta = (BookMeta) itemMeta;

        try {
            // First up is name
            String name = ChatColor.stripColor(bookMeta.getPage(1).split(":")[1].trim().toUpperCase());
            List<String> lores = Arrays.asList(ChatColor.stripColor(bookMeta.getPage(2)).split(":")[2]
                    .trim().split("\\n"));
            String respawn = ChatColor.stripColor(bookMeta.getPage(3).split(":")[1].trim().toUpperCase());
            Integer max_loots = Integer.valueOf(ChatColor.stripColor(bookMeta.getPage(4).split(":")[1].trim().toUpperCase()));

            ChestsConfig.getChests().set(name.toUpperCase() + ".OPTIONS.RESPAWN RATE", respawn);
            ChestsConfig.getChests().set(name.toUpperCase() + ".OPTIONS.MAX LOOTS", max_loots);
            ChestsConfig.getChests().set(name.toUpperCase() + ".LOOTS", lores);
            ChestsConfig.saveLoots();
            return true;

        } catch (Exception e) {
            sChest.debug("Oh no! Give this error to aufdemrand!");
            e.printStackTrace();
        }

        return false;
    }




    /**
     * Saves
     *
     * @param id
     * @return
     */
    public static ItemStack getChestConfiguration(String id) {
        String chestName = id.toUpperCase();

        String respawn = Settings.getRespawnRate(chestName) + "s";
        String max_loots = String.valueOf(Settings.getMaxLoots(chestName));
        List<String> loots = Settings.getLoots(chestName);
        String name = chestName.toUpperCase();

        // Get book
        ItemStack book = new ItemStack(Material.BOOK_AND_QUILL);

        ItemMeta itemMeta = book.getItemMeta();
        BookMeta bookMeta = (BookMeta) itemMeta;
        bookMeta.setDisplayName("'Chest' Configuration for '" + name + "'");
        List<String> lore = new ArrayList<String>();
        lore.add("To save, use /dscript chest --save");
        lore.add("Configuration for: " + name);
        lore.addAll(loots);
        bookMeta.setLore(lore);
        List<String> pages = new ArrayList<String>();

        pages.add(ChatColor.LIGHT_PURPLE + "# Now showing configuration for SuperChest. " +
                "Please type below the name of the chest you wish to save as, below. Continue " +
                "on to the next page once you have finished:\n" +
                ChatColor.DARK_GRAY + name);

        String lootsEntries =
                ChatColor.LIGHT_PURPLE + "# Define loots in the format '##% lootname',"
                        + " determining the percentage chance of a particular loot. "
                        + " ie: '50% Loot_1' will have a 50% chance of spawning 'Loot_1':\n";
        for (String lootentry : loots)
            lootsEntries = lootsEntries + ChatColor.DARK_GRAY + "" + lootentry + "\n";
        pages.add(lootsEntries);

        pages.add(ChatColor.LIGHT_PURPLE + "# Define the amount of time between each respawn of the chest. This timer will start once "
                + "the chest has been looted. Use the format '#({s}|m|h)', so for example '6s' means 6 seconds, and '1h' means 1 hour:\n"
                + ChatColor.DARK_GRAY + respawn);

        pages.add(ChatColor.LIGHT_PURPLE + "# Great! Now define the maximum amount of loots per person:\n"
                + ChatColor.DARK_GRAY + max_loots);

        pages.add(ChatColor.LIGHT_PURPLE + "# All set! To save this chest, exit this and use the command " +
                "'/schest chests --save'. That's it!");

        ((BookMeta) itemMeta).setPages(pages);

        book.setItemMeta(bookMeta);

        return book;
    }


}
