package com.loader.facv.util;

public final class FixedWidthParserUtils {

    private FixedWidthParserUtils() {
    }

    public static String normalize(String line, int requiredLength) {
        if (requiredLength < 0) {
            throw new IllegalArgumentException("requiredLength must be zero or greater");
        }

        if (line == null) {
            return repeatSpace(requiredLength);
        }

        if (line.length() >= requiredLength) {
            return line.substring(0, requiredLength);
        }

        StringBuilder sb = new StringBuilder(line);
        while (sb.length() < requiredLength) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String repeatSpace(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be zero or greater");
        }
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = ' ';
        }
        return new String(chars);
    }

    public static String extract(String source, int startInclusive, int endExclusive) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (startInclusive < 0 || endExclusive < startInclusive || endExclusive > source.length()) {
            throw new IllegalArgumentException("Invalid extract range: [" + startInclusive + ", " + endExclusive
                    + ") for source length " + source.length());
        }
        String value = source.substring(startInclusive, endExclusive);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }
}
