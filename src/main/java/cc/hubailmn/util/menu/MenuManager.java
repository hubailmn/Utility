package cc.hubailmn.util.menu;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.menu.type.MenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class MenuManager {

    public static void setActiveMenu(Player player, MenuBuilder menu) {
        player.setMetadata("activeMenu", new FixedMetadataValue(BasePlugin.getInstance(), menu));
    }

    public static MenuBuilder getActiveMenu(Player player) {
        return player.getMetadata("activeMenu").stream()
                .filter(meta -> meta.getOwningPlugin() == BasePlugin.getInstance())
                .map(meta -> (MenuBuilder) meta.value())
                .findFirst()
                .orElse(null);
    }

    public static void clearActiveMenu(Player player) {
        if (player.hasMetadata("activeMenu")) {
            player.removeMetadata("activeMenu", BasePlugin.getInstance());
        }
    }

    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
    }
}
