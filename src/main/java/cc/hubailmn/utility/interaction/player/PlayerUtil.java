package cc.hubailmn.utility.interaction.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.UUID;

public final class PlayerUtil {

    private PlayerUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    public static boolean isVanished(Player player) {
        if (player == null || !player.hasMetadata("vanished")) {
            return false;
        }

        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }

        return false;
    }

    public static void heal(Player player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            player.setHealth(maxHealthAttr.getValue());
            player.setSaturation(20);
        }
    }

    public static boolean isBedrock(OfflinePlayer player) {
        return player != null && isBedrock(player.getUniqueId());
    }

    public static boolean isBedrock(UUID uuid) {
        return uuid != null && uuid.toString().startsWith("00000000");
    }

    public static boolean isLikelyBedrockName(String name) {
        if (name == null) return false;

        return name.startsWith("*") ||
                name.startsWith(".") ||
                name.startsWith("_") ||
                name.toLowerCase().startsWith("bedrock_") ||
                name.toLowerCase().startsWith("f_") ||
                name.contains("|") ||
                name.length() > 15;
    }

    public static boolean isLikelyBedrock(Player player) {
        return isBedrock(player.getUniqueId()) || isLikelyBedrockName(player.getName());
    }

    public static void teleport(Player player, Location location) {
        if (player == null || location == null) return;
        player.teleportAsync(location);
    }

    public static int getPing(Player player) {
        return player != null ? player.getPing() : -1;
    }

    public static String getDisplayName(Player player) {
        return player != null ? player.displayName().toString() : "";
    }

    public static PlayerPlatform getPlatform(Player player) {
        if (player == null) return PlayerPlatform.UNKNOWN;
        if (isBedrock(player.getUniqueId()) || isLikelyBedrockName(player.getName())) {
            return PlayerPlatform.BEDROCK;
        }
        return PlayerPlatform.JAVA;
    }

    public enum PlayerPlatform {
        JAVA, BEDROCK, UNKNOWN
    }
}

