package cc.hubailmn.util.interaction.player;

import cc.hubailmn.util.interaction.CSend;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class PlayerSoundUtil {

    private static final Map<String, Long> cooldown = new HashMap<>();
    private static final long COOLDOWN_MILLIS = 50L;

    private PlayerSoundUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void playSound(Player listener, String... s) {
        if (listener == null || s == null || s.length == 0) return;

        long current = System.currentTimeMillis();
        if (cooldown.getOrDefault(listener.getName(), 0L) > current) return;

        cooldown.put(listener.getName(), current + COOLDOWN_MILLIS);

        for (String param : s) {
            try {
                String[] parts = param.split("/");
                Sound sound = Sound.valueOf(parts[0].toUpperCase());
                float volume = Float.parseFloat(parts[1]);
                float pitch = Float.parseFloat(parts[2]);
                listener.playSound(listener.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                CSend.warn("Failed to play sound: " + param + " for player " + listener.getName());
            }
        }
    }

    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player != null && sound != null)
            player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
