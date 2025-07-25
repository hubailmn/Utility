package cc.hubailmn.utility.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextParserUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexCharacter('#')
                    .hexColors()
                    .build();

    private TextParserUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        if (input.contains("<") && input.contains(">")) {
            try {
                return MINI.deserialize(input);
            } catch (Exception ignored) {

            }
        }

        return LEGACY_SERIALIZER.deserialize(input);
    }
}
