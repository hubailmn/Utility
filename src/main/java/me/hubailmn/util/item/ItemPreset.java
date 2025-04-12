package me.hubailmn.util.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemPreset {

    public static ItemStack GRAY_PANE = new ItemBuilder()
            .material(Material.GRAY_STAINED_GLASS_PANE)
            .name("§7")
            .build();

    private ItemPreset() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

}
