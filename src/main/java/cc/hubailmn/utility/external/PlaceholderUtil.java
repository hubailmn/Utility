package cc.hubailmn.utility.external;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderUtil {

    public static boolean isHooked() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static String parse(Player player, String text) {
        if (isHooked()) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public static String parse(OfflinePlayer player, String text) {
        if (isHooked()) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public static String parse(String text) {
        if (isHooked()) {
            return PlaceholderAPI.setPlaceholders(null, text);
        }
        return text;
    }

    public static String stripPlaceholders(String text) {
        return text.replaceAll("%[^%]+%", "");
    }

}
