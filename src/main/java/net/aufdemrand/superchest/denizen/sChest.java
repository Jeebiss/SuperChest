package net.aufdemrand.superchest.denizen;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;

public class sChest {

    public static Denizen denizen;

    public static void debug(String string) {
        if (denizen != null)
            dB.log(string);
        else
            Bukkit.getLogger().info("[SuperChest]: " + string);
    }



}
