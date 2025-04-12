package me.hubailmn.util.interaction;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerUtil {

    private static final Map<String, Long> cooldown = new HashMap<>();
    private static final long COOLDOWN_MILLIS = 50L;

    private PlayerUtil() {
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

    public static boolean isBedrock(OfflinePlayer player) {
        return player != null && isBedrock(player.getUniqueId());
    }

    public static boolean isBedrock(UUID uuid) {
        return uuid != null && uuid.toString().startsWith("00000000");
    }

    public static void send(Player p, Type type, String content) {
        if (p == null || type == null || content == null) return;

        switch (type) {
            case CHAT -> p.sendMessage(content);
            case ACTION -> p.sendActionBar(Component.text(content));
            case TITLE -> title(p, content, "");
        }
    }

    public static void title(Player p, String main, String sub) {
        if (p == null) return;
        p.sendTitle(main != null ? main : "", sub != null ? sub : "", 20, 80, 20);
    }

    public enum Type {
        CHAT, ACTION, TITLE
    }
}
