package net.aufdemrand.superchest;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Settings file for SuperChest
 *
 * @author Jeremy Schroeder (aufdemrand)
 */
public class Settings {

    public static SuperChest getSuperChestInstance() {
        // Use Bukkit PluginManager to get a static reference to net.aufdemrand.superchest.SuperChest Plugin
        return (SuperChest) Bukkit.getServer().getPluginManager().getPlugin("SuperChest");
    }

    //  Default loot:
    //  - #% lootname
    //  - #% lootname
    //  - #% lootname

    public static List<String> getLoots(String id) {
        if (ChestsConfig.getChests().contains(id.toUpperCase() + ".LOOTS"))
            return ChestsConfig.getChests().getStringList(id.toUpperCase() + ".LOOTS");
        else

            return getSuperChestInstance().getConfig().getStringList("Default loot");
    }

    //  Default animations:
    //    Default super chest: ENDER_SIGNAL
    //    Default monster chest: SMOKE

    public static Effect getSuperChestAnimation() {
        return Effect.valueOf(getSuperChestInstance().getConfig().getString("Default animations.Default super chest").toUpperCase());
    }

    public static Effect getMonsterChestAnimation() {
        return Effect.valueOf(getSuperChestInstance().getConfig().getString("Default animations.Default monster chest").toUpperCase());
    }

    //    Default sounds:
    //      Default on loot:
    //      Default on monster loot:
    //      Default max loots:

    public static Sound getSuperChestOpenSound() {
        return Sound.valueOf(getSuperChestInstance().getConfig().getString("Default sounds.Default on loot").toUpperCase());
    }

    public static Sound getMonsterChestOpenSound() {
        return Sound.valueOf(getSuperChestInstance().getConfig().getString("Default sounds.Default on monster loot").toUpperCase());
    }

    public static Sound getMaxLootSound() {
        return Sound.valueOf(getSuperChestInstance().getConfig().getString("Default sounds.Default max loots").toUpperCase());
    }

    //    Default options:
    //      Default respawn rate: 30m
    //      Default max loots: 5
    //      Default max loots message: "There's nothing left to loot here!"
    //      Default loot message: "Sweet! Some loot!"
    //      Default monster loot message: "Oh no! Trouble!"

    public static Double getRespawnRate(String id) {
        if (ChestsConfig.getChests().contains(id.toUpperCase() + ".OPTIONS.RESPAWN RATE"))
            return duration2seconds(ChestsConfig.getChests().getString(id.toUpperCase() + ".OPTIONS.RESPAWN RATE"));
        else
            return duration2seconds(getSuperChestInstance().getConfig().getString("Default options.Default respawn rate"));
    }

    public static int getMaxLoots(String id) {
        if (ChestsConfig.getChests().contains(id.toUpperCase() + ".OPTIONS.MAX LOOTS"))
            return ChestsConfig.getChests().getInt(id.toUpperCase() + ".OPTIONS.MAX LOOTS");
        else
            return getSuperChestInstance().getConfig().getInt("Default options.Default max loots");
    }

    public static String getMaxLootsMessage(String id) {
        if (ChestsConfig.getChests().contains(id.toUpperCase() + ".OPTIONS.MAX LOOTS MESSAGE"))
            return ChestsConfig.getChests().getString(id.toUpperCase() + ".OPTIONS.MAX LOOTS MESSAGE");
        else
            return getSuperChestInstance().getConfig().getString("Default options.Default max loots message");
    }

    public static String getLootMessage(String id) {
        if (ChestsConfig.getChests().contains(id.toUpperCase() + ".OPTIONS.LOOT MESSAGE"))
            return ChestsConfig.getChests().getString(id.toUpperCase() + ".OPTIONS.LOOT MESSAGE");
        else
            return getSuperChestInstance().getConfig().getString("Default options.Default loot message");
    }

    public static String getMonsterLootMessage(String id) {
        if (ChestsConfig.getChests().contains(id.toUpperCase() + ".OPTIONS.MONSTER LOOT MESSAGE"))
            return ChestsConfig.getChests().getString(id.toUpperCase() + ".OPTIONS.MONSTER LOOT MESSAGE");
        else
            return getSuperChestInstance().getConfig().getString("Default options.Default monster loot message");
    }


    final static Pattern matchesDurationPtrn = Pattern.compile("(?:.+:|)(\\d+(?:(|\\.\\d+)))(|t|m|s|h|d)", Pattern.CASE_INSENSITIVE);

    public static Double duration2seconds(String string) {
        if (string == null) return null;

        Matcher m = matchesDurationPtrn.matcher(string);
        if (m.matches()) {
            if (m.group().toUpperCase().endsWith("T"))
                // Matches TICKS, so 1 tick = .05 seconds
                return Double.valueOf(m.group(1)) * 0.05;

            else if (m.group().toUpperCase().endsWith("D"))
                // Matches DAYS, so 1 day = 86400 seconds
                return Double.valueOf(m.group(1)) * 86400;

            else if (m.group().toUpperCase().endsWith("M"))
                // Matches MINUTES, so 1 minute = 60 seconds
                return Double.valueOf(m.group(1)) * 60;

            else if (m.group().toUpperCase().endsWith("H"))
                // Matches HOURS, so 1 hour = 3600 seconds
                return Double.valueOf(m.group(1)) * 3600;

            else // seconds
                return Double.valueOf(m.group(1));
        }
        return null;
    }


    public String getNextLootName(Player player) {
        Set<String> lootNames = LootsConfig.getLoots().getKeys(false);

        boolean found = false;
        int x = 1;
        do {
            if (!lootNames.contains(player.getName().toUpperCase() + "S LOOT " + x))
                found = true;
            else x++;
        } while(found == false);

        return player.getName().toUpperCase() + "S LOOT " + x;
    }

}
