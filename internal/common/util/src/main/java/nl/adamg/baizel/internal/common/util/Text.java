package nl.adamg.baizel.internal.common.util;

import javax.annotation.CheckForNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.Adler32;

@SuppressWarnings("unused")
public final class Text {
    private Text() {}

    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        var z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        return String.format("%.1f %sB", (double) bytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    public static String formatDuration(Duration duration) {
        var ms = duration.toMillis();
        if (ms < 1000) {
            return ms + "ms";
        }
        if (ms < (60 * 1000)) {
            return Text.roundDoubleString(ms / 1000d, 0) + "s";
        }
        if (ms < (60 * 60 * 1000)) {
            return Text.roundDoubleString(ms / (1000d * 60), 0) + "m";
        }
        if (ms < (24 * 60 * 60 * 1000)) {
            return Text.roundDoubleString(ms / (1000d * 60 * 60), 0) + "h";
        }
        return duration.toString();
    }

    /** Ignores letter case and non-alphanumeric characters. */
    public static boolean equalsIgnoreFormat(String a, String b) {
        return toFormatAgnostic(a).equals(toFormatAgnostic(b));
    }

    /** Ignores letter case and non-alphanumeric characters. */
    public static String toFormatAgnostic(String string) {
        return string.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    /** counts number of occurrences of the second string in the first string */
    public static int count(String haystack, String needle) {
        if (haystack.isEmpty() || needle.isEmpty()) {
            return 0;
        }
        var count = 0;
        for (var i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }

    /** Adler32 of UTF-8 bytes. Faster than CRC32. */
    public static long checksum(String text) {
        var alg = new Adler32();
        alg.update(text.getBytes(StandardCharsets.UTF_8));
        return alg.getValue();
    }

    public static String roundDoubleString(double input, int maxDecimalDigits) {
        var m = Math.pow(10, maxDecimalDigits);
        var rounded = ((int) Math.round(input * m)) / m;
        var string = "" + rounded;
        if (maxDecimalDigits == 0) {
            return string.replaceAll("\\..*", "");
        }
        return string;
    }

    public static String emptyIfNull(@CheckForNull String nullable) {
        if (nullable == null) {
            return "";
        }
        return nullable;
    }

    @CheckForNull
    public static String find(String haystack, Pattern needle) {
        var matcher = needle.matcher(haystack);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static URI uri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static URI uri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return null if not base-64 utf-8-encoded string of printable characters.
     */
    @CheckForNull
    public static String tryDecodeBase64(String input) {
        if(input.isEmpty()) {
            return "";
        }
        if (input.length() % 4 != 0) {
            return null;
        }
        if (input.matches(".*[^A-Za-z0-9+/=].*")) {
            return null;
        }
        try {
            var decoded = Base64.getDecoder().decode(input);
            var result = new String(decoded);
            if (isPrintableUtf8(result)) {
                return result;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return null;
    }

    public static boolean isPrintableUtf8(String s) {
        return s.codePoints().allMatch(Text::isPrintableCodePoint);
    }

    public static boolean isPrintableCodePoint(int cp) {
        var asciiPrintableStart = 0x20;
        var asciiPrintableEnd = 0x7E;
        var noncharStart = 0xFDD0;
        var noncharEnd = 0xFDEF;
        var noncharMask  = 0xFFFE;
        var nonPrintableCategories = Set.of(
                Character.CONTROL,
                Character.FORMAT,
                Character.PRIVATE_USE,
                Character.SURROGATE,
                Character.UNASSIGNED,
                Character.LINE_SEPARATOR,
                Character.PARAGRAPH_SEPARATOR
        );
        if ((cp >= asciiPrintableStart && cp <= asciiPrintableEnd) || cp == '\t' || cp == '\n' || cp == '\r') {
            return true;
        }
        var category = (byte)Character.getType(cp);
        if (nonPrintableCategories.contains(category)) {
            return false;
        }
        var isNonPrintable = (cp >= noncharStart && cp <= noncharEnd) || (cp & noncharMask) == noncharMask;
        return !isNonPrintable;
    }
    public static String dashed(String separated) {
        return separatedWith(separated, "-");
    }

    public static String dotted(String separated) {
        return separatedWith(separated, ".");
    }

    public static String slashed(String separated) {
        return separatedWith(separated, "/");
    }

    public static String separatedWith(String separated, String separator) {
        return separated
                .replaceAll("[^a-zA-Z0-9]+", "#")
                .replaceAll("(^#+|#+$)", "")
                .replaceAll("#", separator);
    }

    public static String filter(String input, String allowedRegexpRanges) {
        return input.replaceAll("[^" + allowedRegexpRanges + "]+", "");
    }
}
