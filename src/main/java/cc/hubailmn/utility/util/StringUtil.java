package cc.hubailmn.utility.util;

import java.util.Locale;

public final class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String capitalizeWords(String input) {
        if (input == null || input.isBlank()) return "";

        String[] words = input.replace('_', ' ').toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder(input.length());

        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }

        return sb.toString().trim();
    }

    public static String createProgressBar(int current, int max, int totalBars) {
        if (max <= 0) max = 1;
        double progress = (double) current / max;
        int progressBars = (int) (progress * totalBars);

        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i < totalBars; i++) {
            if (i < progressBars) {
                bar.append("§a|");
            } else {
                bar.append("§8|");
            }
        }
        bar.append("§7]");

        int percent = (int) (progress * 100);
        bar.append(" §e").append(percent).append("%");

        return bar.toString();
    }
}
