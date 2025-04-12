package me.hubailmn.util.other;

import java.util.Locale;

public final class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }


    public static String capitalizeWords(String input) {
        if (input == null || input.isBlank()) return "";

        String[] words = input.replace('_', ' ').toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder(input.length());

        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return sb.toString().trim();
    }
}
