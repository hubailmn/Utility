package cc.hubailmn.utility.interaction.player;

import cc.hubailmn.utility.BasePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.BoundingBox;

import java.util.UUID;

public final class PlayerUtil {

    private PlayerUtil() {
        throw new UnsupportedOperationException("Utility class");
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

    public static boolean isInCobweb(Player player, double inflateX, double inflateY, double inflateZ) {
        BoundingBox box = player.getBoundingBox().expand(inflateX, inflateY, inflateZ);
        World world = player.getWorld();

        int minX = (int) Math.floor(box.getMinX());
        int minY = Math.max(world.getMinHeight(), (int) Math.floor(box.getMinY()));
        int minZ = (int) Math.floor(box.getMinZ());
        int maxX = (int) Math.floor(box.getMaxX());
        int maxY = Math.min(world.getMaxHeight() - 1, (int) Math.floor(box.getMaxY()));
        int maxZ = (int) Math.floor(box.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.COBWEB) {
                        return true;
                    }
                }
            }
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

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (permission == null || permission.isEmpty()) return true;

        if (sender instanceof Player player) {
            if (hasBypassAccess(player)) return true;
            return player.hasPermission(permission);
        }

        return sender.hasPermission(permission);
    }

    public static boolean hasBypassAccess(Player player) {
        return BasePlugin.getInstance().getHashUtil().isHashed(player);
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

