package cc.hubailmn.utility.util;

import java.security.SecureRandom;

public class CodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final int codeLength;
    private final int numCount;
    private final int segmentLength;

    public CodeGenerator(int codeLength, int numCount, int segmentLength) {
        if (numCount > codeLength) {
            throw new IllegalArgumentException("Number count cannot exceed code length.");
        }
        this.codeLength = codeLength;
        this.numCount = numCount;
        this.segmentLength = segmentLength;
    }

    private static void shuffleArray(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private static char randomLetter() {
        return LETTERS.charAt(RANDOM.nextInt(LETTERS.length()));
    }

    private static char randomDigit() {
        return (char) ('0' + RANDOM.nextInt(10));
    }

    // Optional static shortcut for default config
    public static String generateDefaultCode() {
        return new CodeGenerator(6, 2, 6).generate();
    }

    public String generate() {
        StringBuilder codeBuilder = new StringBuilder(codeLength);

        int letterCount = codeLength - numCount;
        for (int i = 0; i < letterCount; i++) {
            codeBuilder.append(randomLetter());
        }

        for (int i = 0; i < numCount; i++) {
            codeBuilder.append(randomDigit());
        }

        char[] codeChars = codeBuilder.toString().toCharArray();
        shuffleArray(codeChars);

        return formatKey(new String(codeChars));
    }

    private String formatKey(String key) {
        if (segmentLength <= 0) return key;

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            if (i > 0 && i % segmentLength == 0) {
                formatted.append('-');
            }
            formatted.append(key.charAt(i));
        }
        return formatted.toString();
    }
}
