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

        if (yaw >= 337.5 || yaw < 22.5) return 0f;
        if (yaw >= 22.5 && yaw < 67.5) return 45f;
        if (yaw >= 67.5 && yaw < 112.5) return 90f;
        if (yaw >= 112.5 && yaw < 157.5) return 135f;

        if (yaw >= 157.5 && yaw < 202.5) return 180;
        if (yaw >= 202.5 && yaw < 247.5) return 225f;
        if (yaw >= 247.5 && yaw < 292.5) return 270f;
        if (yaw >= 292.5 && yaw < 337.5) return 315f;

        return 0f;
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