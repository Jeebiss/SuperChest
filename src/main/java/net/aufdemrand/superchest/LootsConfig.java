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
 * Loots config file management/storage methods/fields
 *
 */
public class LootsConfig {

    private static FileConfiguration loots_cfg = null;
    private static File loots = null;

    /**
     * Reloads loots file from disk
     *
     */
    public static void reloadLoots() {
        if (loots_cfg == null) {
            loots = new File(Settings.getSuperChestInstance().getDataFolder(), "loots.yml");
        }
        loots_cfg = YamlConfiguration.loadConfiguration(loots);
    }


    /**
     * Gets loots currently loaded into memory
     *
     * @return
     */
    public static FileConfiguration getLoots() {
        if (loots_cfg == null) {
            reloadLoots();
        }
        return loots_cfg;
    }

    /**
     * Saves loots to disk
     *
     */
    public static void saveLoots() {
        if (loots_cfg == null || loots == null) {
            return;
        }
        try {
            loots_cfg.save(loots);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + loots, ex);
        }
    }

}
