package cc.hubailmn.util.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemPreset {

    public static final ItemStack GRAY_GlASS = (new ItemBuilder())
            .material(Material.GRAY_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack WHITE_GlASS = (new ItemBuilder())
            .material(Material.WHITE_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack LIME_GLASS = (new ItemBuilder())
            .material(Material.LIME_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    private ItemPreset() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

}
