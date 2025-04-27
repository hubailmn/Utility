package me.hubailmn.util.interaction.player;

import me.hubailmn.util.BasePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class PlayerMessageUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private PlayerMessageUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void send(Player p, Type type, String content) {
        if (p == null || type == null || content == null) return;

        Component message = LEGACY_SERIALIZER.deserialize(content);

        switch (type) {
            case CHAT -> p.sendMessage(message);
            case ACTION -> p.sendActionBar(message);
            case TITLE -> title(p, message, Component.empty());
        }
    }

    public static void prefixed(Player player, String content) {
        if (player == null || content == null) return;

        Component prefixComponent = LEGACY_SERIALIZER.deserialize(BasePlugin.getPrefix() + " ");
        Component messageComponent = LEGACY_SERIALIZER.deserialize(content);

        player.sendMessage(prefixComponent.append(messageComponent));
    }

    public static void title(Player p, String main, String sub) {
        title(p,
                LEGACY_SERIALIZER.deserialize(main != null ? main : ""),
                LEGACY_SERIALIZER.deserialize(sub != null ? sub : ""));
    }

    public static void title(Player p, Component title, Component subtitle) {
        if (p == null) return;

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(2000),
                Duration.ofMillis(500)
        );
        Title fullTitle = Title.title(title, subtitle, times);
        p.showTitle(fullTitle);
    }

    public static void sendCenteredMessage(Player player, String message) {
        if (player == null || message == null) return;

        Component centeredMessage = LEGACY_SERIALIZER.deserialize(centerMessage(message));
        player.sendMessage(centeredMessage);
    }

    private static String centerMessage(String message) {
        int centerPx = 154;
        int messagePxSize = message.length() * 6;
        int spacesNeeded = (centerPx - messagePxSize / 2) / 4;
        return " ".repeat(Math.max(0, spacesNeeded)) + message;
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
