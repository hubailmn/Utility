package me.hubailmn.util.interaction.player;

import me.hubailmn.util.BasePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class PlayerMessageUtil {

    private PlayerMessageUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void send(Player p, Type type, String content) {
        if (p == null || type == null || content == null) return;

        switch (type) {
            case CHAT -> p.sendMessage(Component.text(content));
            case ACTION -> p.sendActionBar(Component.text(content));
            case TITLE -> title(p, Component.text(content), Component.empty());
        }
    }

    public static void prefixed(Player player, String content) {
        player.sendMessage(BasePlugin.getPrefix() + " " + Component.text(content));
    }

    public static void title(Player p, String main, String sub) {
        title(p, Component.text(main != null ? main : ""), Component.text(sub != null ? sub : ""));
    }

    public static void title(Player p, Component title, Component subtitle) {
        if (p == null) return;
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
        Title fullTitle = Title.title(title, subtitle, times);
        p.showTitle(fullTitle);
    }

    public static void sendCenteredMessage(Player player, String message) {
        if (player == null || message == null) return;

        int centerPx = 154;
        int messagePxSize = message.length() * 6;
        int spacesNeeded = (centerPx - messagePxSize / 2) / 4;

        player.sendMessage(Component.text(" ".repeat(Math.max(0, spacesNeeded)) + message));
    }

    public static void silentSend(Player player, Component message) {
        if (player == null || message == null) return;
        player.sendMessage(message);
    }

    public static void resetTitle(Player player) {
        if (player != null)
            player.clearTitle();
    }

    public enum Type {
        CHAT, ACTION, TITLE
    }
}
