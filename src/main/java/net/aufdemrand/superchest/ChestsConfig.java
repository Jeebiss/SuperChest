package net.aufdemrand.superchest;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Chests config file management/storage methods/fields
 *
 */
public class ChestsConfig {

    private static FileConfiguration chests_cfg = null;
    private static File chests = null;

    /**
     * Reloads chests file from disk
     *
     */
    public static void reloadChests() {
        if (chests_cfg == null) {
            chests = new File(Settings.getSuperChestInstance().getDataFolder(), "chests.yml");
        }
        chests_cfg = YamlConfiguration.loadConfiguration(chests);
    }

    /**
     * Gets chests currently in memory.
     *
     * @return
     */
    public static FileConfiguration getChests() {
        if (chests_cfg == null) {
            reloadChests();
        }
        return chests_cfg;
    }

    /**
     * Saves loots to disk
     *
     */
    public static void saveLoots() {
        if (chests_cfg == null || chests == null) {
            return;
        }
        try {
            chests_cfg.save(chests);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + chests, ex);
        }
    }

}
