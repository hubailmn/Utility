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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Objects;

public final class PlayerMessageUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private PlayerMessageUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void send(Player player, Component content) {
        if (player == null || content == null) return;
        player.sendMessage(content);
    }

    public static void send(Player player, String content) {
        if (player == null || content == null) return;
        player.sendMessage(LEGACY_SERIALIZER.deserialize(content));
    }

    public static void prefixed(Player player, Component content) {
        Component prefix = LEGACY_SERIALIZER.deserialize(BasePlugin.getPrefix());
        send(player, prefix.append(Component.space()).append(content));
    }

    public static void prefixed(Player player, String content) {
        send(player, BasePlugin.getPrefix() + " " + content);
    }

    public static void sendActionBarMessage(Player player, Component message) {
        if (player == null || message == null) return;
        player.sendActionBar(message);
    }

    public static void sendActionBarMessage(Player player, String message) {
        if (player == null || message == null) return;
        player.sendActionBar(LEGACY_SERIALIZER.deserialize(message));
    }

    public static void sendPrefixedActionBarMessage(Player player, Component message) {
        Component prefix = LEGACY_SERIALIZER.deserialize(BasePlugin.getPrefix());
        sendActionBarMessage(player, prefix.append(Component.space()).append(message));
    }

    public static void sendPrefixedActionBarMessage(Player player, String message) {
        sendActionBarMessage(player, BasePlugin.getPrefix() + " " + message);
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

    public static void sendCenteredMessage(Player player, Component message) {
        if (player == null || message == null) return;

        String legacyMessage = LEGACY_SERIALIZER.serialize(message);
        sendCenteredMessage(player, legacyMessage);
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

    public static void title(Player p, Component titleComponent, Component subtitleComponent) {
        if (p == null) return;
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
        Title fullTitle = Title.title(titleComponent, subtitleComponent, times);
        p.showTitle(fullTitle);
    }

    public static void resetTitle(Player player) {
        if (player != null) player.clearTitle();
    }

    public static Component hover(String text, String hoverText) {
        return LEGACY_SERIALIZER.deserialize(text)
                .hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(hoverText)));
    }

    public static Component hover(Component text, String hoverText) {
        return text.hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(hoverText)));
    }

    public static Component hover(Component text, Component hoverText) {
        return text.hoverEvent(HoverEvent.showText(hoverText));
    }

    public static Component clickRunCommand(String text, String command) {
        return LEGACY_SERIALIZER.deserialize(text)
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Component clickRunCommand(Component text, String command) {
        return text.clickEvent(ClickEvent.runCommand(command));
    }

    public static Component clickOpenUrl(String text, String url) {
        return LEGACY_SERIALIZER.deserialize(text)
                .clickEvent(ClickEvent.openUrl(url));
    }

    public static Component clickOpenUrl(Component text, String url) {
        return text.clickEvent(ClickEvent.openUrl(url));
    }

    public static Component hoverAndClick(String text, String hoverText, String command) {
        return LEGACY_SERIALIZER.deserialize(text)
                .hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(hoverText)))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Component hoverAndClick(Component text, String hoverText, String command) {
        return text.hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(hoverText)))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Component hoverAndClick(Component text, Component hoverText, String command) {
        return text.hoverEvent(HoverEvent.showText(hoverText))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Component gradient(String text, TextColor... colors) {
        Objects.requireNonNull(text, "Text cannot be null");
        if (text.isEmpty()) {
            return Component.empty();
        }

        Objects.requireNonNull(colors, "Colors array cannot be null");
        if (colors.length == 0) {
            return Component.text(text);
        }

        if (colors.length == 1) {
            return Component.text(text).color(colors[0]);
        }

        String[] chars = text.split("");
        int length = chars.length;
        ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();

        for (int i = 0; i < length; i++) {
            float overallFactor = (float) i / (length - 1);
            if (length == 1) {
                overallFactor = 0.0f;
            }

            float segmentSize = 1.0f / (colors.length - 1);
            int segmentIndex = (int) (overallFactor / segmentSize);

            if (segmentIndex >= colors.length - 1) {
                segmentIndex = colors.length - 2;
            }

            TextColor startColor = colors[segmentIndex];
            TextColor endColor = colors[segmentIndex + 1];

            float segmentFactor = (overallFactor - (segmentIndex * segmentSize)) / segmentSize;

            int r = (int) (startColor.red() + segmentFactor * (endColor.red() - startColor.red()));
            int g = (int) (startColor.green() + segmentFactor * (endColor.green() - startColor.green()));
            int b = (int) (startColor.blue() + segmentFactor * (endColor.blue() - startColor.blue()));

            builder.append(Component.text(chars[i]).color(TextColor.color(r, g, b)));
        }
        return builder.build();
    }

    public static void animateTitle(Player player, String[] frames, long frameDurationMillis, Sound sound, float volume, float pitch) {
        if (player == null || frames == null || frames.length == 0) return;

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= frames.length) {
                    cancel();
                    return;
                }

                String frame = frames[index++];
                player.showTitle(Title.title(
                        LEGACY_SERIALIZER.deserialize(frame),
                        Component.empty(),
                        Title.Times.times(
                                Duration.ofMillis(100),
                                Duration.ofMillis(frameDurationMillis),
                                Duration.ofMillis(100)
                        )
                ));

                if (sound != null) {
                    PlayerSoundUtil.playSound(player, sound, volume, pitch);
                }
            }
        }.runTaskTimer(BasePlugin.getInstance(), 0L, frameDurationMillis / 50L);
    }

    public static void animateTitle(Player player, Component[] frames, long frameDurationMillis, Sound sound, float volume, float pitch) {
        if (player == null || frames == null || frames.length == 0) return;

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= frames.length) {
                    cancel();
                    return;
                }

                Component frame = frames[index++];
                player.showTitle(Title.title(
                        frame,
                        Component.empty(),
                        Title.Times.times(
                                Duration.ofMillis(100),
                                Duration.ofMillis(frameDurationMillis),
                                Duration.ofMillis(100)
                        )
                ));

                if (sound != null) {
                    PlayerSoundUtil.playSound(player, sound, volume, pitch);
                }
            }
        }.runTaskTimer(BasePlugin.getInstance(), 0L, frameDurationMillis / 50L);
    }
}