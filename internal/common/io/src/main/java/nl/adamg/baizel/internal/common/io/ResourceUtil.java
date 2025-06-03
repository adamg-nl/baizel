package nl.adamg.baizel.internal.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ResourceUtil {
    public static String readString(Class<?> owner, String packageRelativePath) throws IOException {
        try(var stream = owner.getResourceAsStream(packageRelativePath)) {
            if (stream == null) {
                throw new FileNotFoundException(packageRelativePath);
            }
            var scanner = new Scanner(stream, StandardCharsets.UTF_8);
            return scanner.next("\\A");
        }
    }
}
