package me.hubailmn.util.interaction.player;

import me.hubailmn.util.BasePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class PlayerMessageUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private PlayerMessageUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    // ===================== BASIC SEND =====================
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
        player.sendMessage(
                LEGACY_SERIALIZER.deserialize(BasePlugin.getPrefix() + " " + content)
        );
    }

    public static void sendCenteredMessage(Player player, String message) {
        if (player == null || message == null) return;

        int centerPx = 154;
        int messagePxSize = message.length() * 6;
        int spacesNeeded = (centerPx - messagePxSize / 2) / 4;

        player.sendMessage(
                LEGACY_SERIALIZER.deserialize(" ".repeat(Math.max(0, spacesNeeded)) + message)
        );
    }

    public static void silentSend(Player player, Component message) {
        if (player == null || message == null) return;
        player.sendMessage(message);
    }

    public static void title(Player p, String main, String sub) {
        title(p,
                LEGACY_SERIALIZER.deserialize(main != null ? main : ""),
                LEGACY_SERIALIZER.deserialize(sub != null ? sub : "")
        );
    }

    public static void title(Player p, Component title, Component subtitle) {
        if (p == null) return;
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
        Title fullTitle = Title.title(title, subtitle, times);
        p.showTitle(fullTitle);
    }

    public static void resetTitle(Player player) {
        if (player != null) player.clearTitle();
    }

    // ===================== COMPONENT HELPERS =====================

    public static Component hover(String text, String hoverText) {
        return Component.text()
                .content(text)
                .hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(hoverText)))
                .build();
    }

    public static Component clickRunCommand(String text, String command) {
        return Component.text()
                .content(text)
                .clickEvent(ClickEvent.runCommand(command))
                .build();
    }

    public static Component clickOpenUrl(String text, String url) {
        return Component.text()
                .content(text)
                .clickEvent(ClickEvent.openUrl(url))
                .build();
    }

    public static Component hoverAndClick(String text, String hoverText, String command) {
        return Component.text()
                .content(text)
                .hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(hoverText)))
                .clickEvent(ClickEvent.runCommand(command))
                .build();
    }

    public static Component gradient(String text, TextColor... colors) {
        if (colors.length == 0) return Component.text(text);

        String[] chars = text.split("");
        int sections = chars.length / colors.length;
        if (sections == 0) sections = 1;

        ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();
        int colorIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            if (i % sections == 0 && colorIndex + 1 < colors.length) {
                colorIndex++;
            }
            builder.append(Component.text(chars[i]).color(colors[colorIndex]));
        }
        return builder.build();
    }

    public static void animateTitle(Player player, String[] frames, long frameDurationMillis) {
        if (player == null || frames == null || frames.length == 0) return;

        new Thread(() -> {
            try {
                for (String frame : frames) {
                    player.showTitle(Title.title(
                            LEGACY_SERIALIZER.deserialize(frame),
                            Component.empty(),
                            Title.Times.times(
                                    Duration.ofMillis(100),
                                    Duration.ofMillis(frameDurationMillis),
                                    Duration.ofMillis(100)
                            )
                    ));
                    Thread.sleep(frameDurationMillis);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    // ===================== ENUMS =====================

    public enum Type {
        CHAT, ACTION, TITLE
    }
}
