package cc.hubailmn.util.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemPreset {

    public static final ItemStack GRAY_GLASS = new ItemBuilder()
            .material(Material.GRAY_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack WHITE_GLASS = new ItemBuilder()
            .material(Material.WHITE_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack LIME_GLASS = new ItemBuilder()
            .material(Material.LIME_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack RED_GLASS = new ItemBuilder()
            .material(Material.RED_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack BLACK_GLASS = new ItemBuilder()
            .material(Material.BLACK_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack BLUE_GLASS = new ItemBuilder()
            .material(Material.BLUE_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack ORANGE_GLASS = new ItemBuilder()
            .material(Material.ORANGE_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack YELLOW_GLASS = new ItemBuilder()
            .material(Material.YELLOW_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack GREEN_GLASS = new ItemBuilder()
            .material(Material.GREEN_STAINED_GLASS_PANE)
            .name("§f")
            .build();

    public static final ItemStack INFO_BOOK = new ItemBuilder()
            .material(Material.BOOK)
            .name("§b§lInformation")
            .build();

    private ItemPreset() {
        throw new UnsupportedOperationException("This is a utility class.");
    }
}
