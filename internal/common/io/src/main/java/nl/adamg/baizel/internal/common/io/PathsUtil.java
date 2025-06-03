package nl.adamg.baizel.internal.common.io;

import java.nio.file.Path;

public class PathsUtil {
    public static Path getParent(Path path, int level) {
        for(int i=0; i<level; i++) {
            path = path.getParent();
        }
        return path;
    }
}
