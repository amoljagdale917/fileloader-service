package com.loader.facv.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FixedWidthParserUtilsTest {

    @Test
    void shouldNormalizeByPaddingForShortInput() {
        String normalized = FixedWidthParserUtils.normalize("ABC", 5);

        Assertions.assertEquals("ABC  ", normalized);
    }

    @Test
    void shouldNormalizeByTruncatingForLongInput() {
        String normalized = FixedWidthParserUtils.normalize("ABCDEFG", 4);

        Assertions.assertEquals("ABCD", normalized);
    }

    @Test
    void shouldNormalizeNullByReturningSpaces() {
        String normalized = FixedWidthParserUtils.normalize(null, 3);

        Assertions.assertEquals("   ", normalized);
    }

    @Test
    void shouldRepeatSpace() {
        Assertions.assertEquals("    ", FixedWidthParserUtils.repeatSpace(4));
        Assertions.assertEquals("", FixedWidthParserUtils.repeatSpace(0));
    }

    @Test
    void shouldExtractTrimmedValueAndReturnNullForBlankRange() {
        String source = " AB CD    ";

        Assertions.assertEquals("AB CD", FixedWidthParserUtils.extract(source, 0, 6));
        Assertions.assertNull(FixedWidthParserUtils.extract(source, 6, 10));
    }

    @Test
    void shouldThrowForInvalidArguments() {
        IllegalArgumentException normalizeNegative = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> FixedWidthParserUtils.normalize("A", -1)
        );
        IllegalArgumentException repeatNegative = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> FixedWidthParserUtils.repeatSpace(-1)
        );
        IllegalArgumentException extractNullSource = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> FixedWidthParserUtils.extract(null, 0, 1)
        );
        IllegalArgumentException extractBadRange = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> FixedWidthParserUtils.extract("ABC", 2, 5)
        );

        Assertions.assertEquals("requiredLength must be zero or greater", normalizeNegative.getMessage());
        Assertions.assertEquals("length must be zero or greater", repeatNegative.getMessage());
        Assertions.assertEquals("source must not be null", extractNullSource.getMessage());
        Assertions.assertTrue(extractBadRange.getMessage().startsWith("Invalid extract range"));
    }
}
