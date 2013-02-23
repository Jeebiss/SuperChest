package net.aufdemrand.superchest.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Press
 * Date: 2/22/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomItems {

    public static ItemStack getNew(ItemStack base, String display) {
        return getNew(base, display, null, null);
    }

    public static ItemStack getNew(ItemStack base, String display, String alt, String alt2) {
        List<String> lore = new ArrayList<String>();
        if (alt != null) lore.add(alt);
        if (alt2 != null) lore.add(alt2);
        return getNew(base, display, lore);
    }

    public static ItemStack getNew(ItemStack base, String display, List<String> alts) {
        ItemMeta meta = base.getItemMeta();
        meta.setDisplayName(display);
        meta.setLore(alts);
        base.setItemMeta(meta);
        return base;
    }

}
