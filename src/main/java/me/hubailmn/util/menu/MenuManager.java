package me.hubailmn.util.menu;

import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.menu.type.MenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class MenuManager {

    public static final String MENU_METADATA_KEY = BasePlugin.getPluginName().toLowerCase() + "_activeMenu_" + UUID.randomUUID();

    public static MenuBuilder getActiveMenu(Player player) {
        if (player.hasMetadata(MENU_METADATA_KEY)) {
            return (MenuBuilder) player.getMetadata(MENU_METADATA_KEY).get(0).value();
        }
        return null;
    }

    public static void clearActiveMenu(Player player) {
        if (player.hasMetadata(MENU_METADATA_KEY)) {
            player.removeMetadata(MENU_METADATA_KEY, BasePlugin.getInstance());
            player.closeInventory();
        }
    }

    public static void addActiveMenu(Player player, MenuBuilder menu) {
        player.setMetadata(MENU_METADATA_KEY, new FixedMetadataValue(BasePlugin.getInstance(), menu));
    }

    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
    }
}
