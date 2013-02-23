package net.aufdemrand.superchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class ChestInstance {

    /**
     * Creates a new SuperChest.
     *
     * @param id
     * @param location
     */
    public ChestInstance(String id, Location location) {
        this.id = id;
        this.data = 0;
        this.location = location;
    }

    /**
     * Sets the block data associated with this loot. This affects the orientation
     * of the Chest Block.
     *
     */
    public void setData(byte data) {
        data = data;
    }

    public Inventory getRandomInventory() {
        List<String> possibleLoots = Settings.getLoots(id);
        return Loots.getLoot(getLootFromList(possibleLoots));
    }

    private final Location location;
    private final String id;

    public byte data;

    /**
     * Calculates percentage of the supplied loots and generates a random number accordingly
     * in order to pick an outcome.
     *
     * @param lootList
     * @return
     */
    private String getLootFromList(List<String> lootList) {

        // Requires a List<String> in the format of:
        // #% Name of Loot

        Map<String, String> lootSelector = new HashMap<String, String>();
        int count = 0;
        for (String loot : lootList) {
            try {
                String percentages = count + ",";
                String lootName = loot.split("%", 2)[1].trim();
                count = count + Integer.valueOf(loot.split("%", 2)[0]);
                percentages = percentages + count;
                lootSelector.put(lootName, percentages);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Random selector = new Random();
        int selection = selector.nextInt(count);
        String loot_name = null;
        for (Map.Entry<String, String> loot : lootSelector.entrySet()) {
            int low = Integer.valueOf(loot.getValue().split("\\,")[0]);
            int high = Integer.valueOf(loot.getValue().split("\\,")[1]);
            if (selection >= low && selection < high) loot_name = loot.getKey();
        }
        return loot_name;
    }

    public void removeAndReschedule() {
        Double seconds = Settings.getRespawnRate(id);
        location.getBlock().setType(Material.AIR);
        location.getWorld()
                .playEffect(location, Settings.getSuperChestAnimation(), 2);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Settings.getSuperChestInstance(), new Runnable() {

            @Override
            public void run() {
                // Location of new chest
                location.getBlock().setType(Material.CHEST);
                location.getBlock().setData(data);
            }

        }, seconds.longValue() * 20);

    }


}
