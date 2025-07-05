package cc.hubailmn.utility.menu;

import cc.hubailmn.utility.menu.type.MenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuManager {

    private static final Map<UUID, MenuBuilder> activeMenus = new ConcurrentHashMap<>();

    public static void setActiveMenu(Player player, MenuBuilder menu) {
        activeMenus.put(player.getUniqueId(), menu);
    }

    public static MenuBuilder getActiveMenu(Player player) {
        return activeMenus.get(player.getUniqueId());
    }

    public static boolean hasActiveMenu(Player player) {
        return activeMenus.containsKey(player.getUniqueId());
    }

    public static void clearActiveMenu(Player player) {
        activeMenus.remove(player.getUniqueId());
    }

    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasActiveMenu(player)) {
                player.closeInventory();
            }
        }
        activeMenus.clear();
    }
}
