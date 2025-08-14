package cc.hubailmn.utility.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

public class TextParserUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexCharacter('#')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private static final LegacyComponentSerializer LEGACY_SECTION_SERIALIZER =
            LegacyComponentSerializer.legacySection();

    private static final Pattern MINI_PATTERN = Pattern.compile("<[^>]+>");

    private TextParserUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        Component result;
        boolean looksLikeMiniMessage = MINI_PATTERN.matcher(input).find();

        if (looksLikeMiniMessage) {
            try {
                result = MINI.deserialize(input);
            } catch (Exception e) {
                result = LEGACY_SERIALIZER.deserialize(input);
            }
        } else {
            result = LEGACY_SERIALIZER.deserialize(input);
        }

        return result.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String toPlainText(Component component) {
        return LEGACY_SECTION_SERIALIZER.serialize(component);
    }

    public static Component stripNewlinesPreservingStyle(Component component) {
        if (component instanceof TextComponent textComponent) {
            String cleanText = textComponent.content().replace("\n", " ");
            Component base = Component.text(cleanText, textComponent.style());

            for (Component child : textComponent.children()) {
                base = base.append(stripNewlinesPreservingStyle(child));
            }

            return base;
        }

        return component.children().isEmpty()
               ? component
               : component.children().stream()
                       .map(TextParserUtil::stripNewlinesPreservingStyle)
                       .reduce(component, Component::append);
    }

}
