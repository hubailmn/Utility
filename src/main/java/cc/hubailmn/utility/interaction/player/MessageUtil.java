package cc.hubailmn.utility.interaction.player;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.SoundUtil;
import cc.hubailmn.utility.util.TextParserUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Objects;

public final class MessageUtil {

    private MessageUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void send(CommandSender sender, Component content) {
        if (sender == null || content == null) return;
        sender.sendMessage(content);
    }

    public static void send(CommandSender sender, String content) {
        if (sender == null || content == null) return;
        sender.sendMessage(TextParserUtil.parse(content));
    }

    public static void prefixed(CommandSender sender, Component content) {
        Component prefix = TextParserUtil.parse(BasePlugin.getPrefix());
        send(sender, prefix.append(Component.space()).append(content));
    }

    public static void prefixed(CommandSender sender, String content) {
        prefixed(sender, TextParserUtil.parse(content));
    }

    public static void sendActionBarMessage(Player player, Component message) {
        if (player == null || message == null) return;
        player.sendActionBar(message);
    }

    public static void sendActionBarMessage(Player player, String message) {
        if (player == null || message == null) return;
        player.sendActionBar(TextParserUtil.parse(message));
    }

    public static void sendPrefixedActionBarMessage(Player player, Component message) {
        Component prefix = TextParserUtil.parse(BasePlugin.getPrefix());
        sendActionBarMessage(player, prefix.append(Component.space()).append(message));
    }

    public static void sendPrefixedActionBarMessage(Player player, String message) {
        sendPrefixedActionBarMessage(player, TextParserUtil.parse(message));
    }

    public static void sendCenteredMessage(Player player, String message) {
        if (player == null || message == null) return;

        int centerPx = 154;
        int messagePxSize = message.length() * 6;
        int spacesNeeded = (centerPx - messagePxSize / 2) / 4;

        player.sendMessage(
                TextParserUtil.parse(" ".repeat(Math.max(0, spacesNeeded)) + message)
        );
    }

    public static void sendCenteredMessage(Player player, Component message) {
        if (player == null || message == null) return;
        String plain = TextParserUtil.toPlainText(message);
        sendCenteredMessage(player, plain);
    }

    public static void title(Player p, String main, String sub) {
        title(p,
                main != null ? TextParserUtil.parse(main) : Component.empty(),
                sub != null ? TextParserUtil.parse(sub) : Component.empty()
        );
    }

    public static void title(Player p, Component titleComponent, Component subtitleComponent) {
        if (p == null) return;
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
        Title fullTitle = Title.title(titleComponent, subtitleComponent, times);
        p.showTitle(fullTitle);
    }

    public static void title(Player player, Component title, Component subtitle, float fadeIn, float stay, float fadeOut) {
        player.showTitle(
                Title.title(title, subtitle == null ? Component.empty() : subtitle,
                        Title.Times.times(
                                java.time.Duration.ofMillis((long) (fadeIn * 50)),
                                java.time.Duration.ofMillis((long) (stay * 50)),
                                java.time.Duration.ofMillis((long) (fadeOut * 50))
                        )
                )
        );
    }

    public static void resetTitle(Player player) {
        if (player != null) player.clearTitle();
    }

    public static Component hover(String text, String hoverText) {
        return TextParserUtil.parse(text)
                .hoverEvent(HoverEvent.showText(TextParserUtil.parse(hoverText)));
    }

    public static Component hover(Component text, String hoverText) {
        return text.hoverEvent(HoverEvent.showText(TextParserUtil.parse(hoverText)));
    }

    public static Component hover(Component text, Component hoverText) {
        return text.hoverEvent(HoverEvent.showText(hoverText));
    }

    public static Component clickRunCommand(String text, String command) {
        return TextParserUtil.parse(text)
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Component clickRunCommand(Component text, String command) {
        return text.clickEvent(ClickEvent.runCommand(command));
    }

    public static Component clickOpenUrl(String text, String url) {
        return TextParserUtil.parse(text)
                .clickEvent(ClickEvent.openUrl(url));
    }

    public static Component clickOpenUrl(Component text, String url) {
        return text.clickEvent(ClickEvent.openUrl(url));
    }

    public static Component hoverAndClick(String text, String hoverText, String command) {
        return TextParserUtil.parse(text)
                .hoverEvent(HoverEvent.showText(TextParserUtil.parse(hoverText)))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Component hoverAndClick(Component text, String hoverText, String command) {
        return text.hoverEvent(HoverEvent.showText(TextParserUtil.parse(hoverText)))
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
                        TextParserUtil.parse(frame),
                        Component.empty(),
                        Title.Times.times(
                                Duration.ofMillis(100),
                                Duration.ofMillis(frameDurationMillis),
                                Duration.ofMillis(100)
                        )
                ));

                if (sound != null) {
                    SoundUtil.playSound(player, sound, volume, pitch);
                }
            }
        }.runTaskTimer(BasePlugin.getInstance(), 0L, frameDurationMillis / 50L);
    }
}