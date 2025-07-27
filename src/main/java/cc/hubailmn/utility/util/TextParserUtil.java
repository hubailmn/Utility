package cc.hubailmn.utility.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextParserUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexCharacter('#')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private TextParserUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        Component result;
        if (input.contains("<") && input.contains(">")) {
            try {
                result = MINI.deserialize(input);
            } catch (Exception ignored) {
                result = LEGACY_SERIALIZER.deserialize(input);
            }
        } else {
            result = LEGACY_SERIALIZER.deserialize(input);
        }

        return result.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String toPlainText(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

}
