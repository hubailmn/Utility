package cc.hubailmn.utility.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {

    public static boolean isInside(Location location, Location min, Location max) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= Math.min(min.getX(), max.getX()) && x <= Math.max(min.getX(), max.getX())
                && y >= Math.min(min.getY(), max.getY()) && y <= Math.max(min.getY(), max.getY())
                && z >= Math.min(min.getZ(), max.getZ()) && z <= Math.max(min.getZ(), max.getZ());
    }

    public static float getCardinalYaw(float originalYaw) {
        float yaw = (originalYaw % 360 + 360) % 360;
        return Math.round(yaw / 45f) * 45f % 360;
    }

    public static Location fixLocation(Location location) {
        double x = location.getBlockX() + 0.5;
        double y = location.getY();
        double z = location.getBlockZ() + 0.5;

        float yaw = LocationUtil.getCardinalYaw(location.getYaw());
        float pitch = 0.0f;

        return new Location(location.getWorld(), x, y, z, yaw, pitch);
    }

    public static String serialize(Location location) {
        if (location == null) return "";
        return location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();
    }

    public static Location deserialize(String input) {
        if (input == null || input.isEmpty()) return null;
        String[] parts = input.split(";");
        if (parts.length < 6) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}