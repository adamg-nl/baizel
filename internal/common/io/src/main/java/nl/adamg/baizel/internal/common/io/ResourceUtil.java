package nl.adamg.baizel.internal.common.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import javax.annotation.CheckForNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilities for reading Java application and test resource files.
 */
public final class ResourceUtil {
    /**
     * Returns path to a resource, which may be a file inside a dist jar or inside project sources.
     * In case of jar, returned path is ZipFileSystem path, with root at the root of the jar
     * content. Such paths work with most of read-only Java NIO APIs, but cannot be manipulated as
     * strings. Path from project sources is only returned in dev mode, to enable hot reloading
     * without rebuild.
     */
    @CheckForNull
    public static Path getResourcePath(@CheckForNull Class<?> owner, String relativePath) {
        var url = getResourceUrl(owner, relativePath);
        if (url == null) {
            return findInSrcDir(owner, relativePath);
        }
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            return null;
        }
        if (uri.getScheme().equals("jar")) {
            var jarFsPath = uri.toString().replaceFirst(".*!/", "");
            try {
                return getJavaFileSystem(uri).getPath(jarFsPath);
            } catch (IOException e) {
                return null;
            }
        }
        var buildCachePath = Paths.get(uri);
        if (Files.exists(buildCachePath)) {
            return buildCachePath;
        }
        return null;
    }

    /**
     * If resource is not found on classpath, but we are running from a build output dir within a
     * source repository, check under src/main/resources.
     */
    @CheckForNull
    private static Path findInSrcDir(@CheckForNull Class<?> owner, String relativePath) {
        var resourcesParent = ResourceUtil.getResourcePath(owner, ".");
        if (resourcesParent == null) {
            return null;
        }
        var relativeToParent = resourcesParent.resolve(relativePath);
        if (Files.exists(relativeToParent)) {
            return relativeToParent;
        }
        var resourcesRoot = ResourceUtil.getResourcePath(owner, "/");
        if (resourcesRoot == null || owner == null) {
            return null;
        }
        var relativeToRoot = resourcesRoot.resolve(owner.getPackageName().replace(".", "/") + "/" + relativePath);
        if (Files.exists(relativeToRoot)) {
            return relativeToRoot;
        }
        return null;
    }

    public static List<String> readTextResource(@CheckForNull Class<?> owner, String relativePath) throws IOException {
        try (var stream = openStream(owner, relativePath)) {
            if (stream == null) {
                throw new FileNotFoundException(relativePath);
            }
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.toList());
            }
        }
    }

    public static String readStringResource(@CheckForNull Class<?> owner, String relativePath) throws IOException {
        try (var stream = openStream(owner, relativePath)) {
            if (stream == null) {
                throw new FileNotFoundException(relativePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static byte[] readBinaryResource(@CheckForNull Class<?> owner, String relativePath) throws IOException {
        try (var stream = openStream(owner, relativePath)) {
            if (stream == null) {
                throw new FileNotFoundException(relativePath);
            }
            return stream.readAllBytes();
        }
    }

    // region internal utils
    @CheckForNull
    private static URL getResourceUrl(@CheckForNull Class<?> owner, String path) {
        if (owner == null) {
            var url = ResourceUtil.class.getClassLoader().getResource(path);
            if (url != null) {
                return url;
            }
            return ClassLoader.getSystemResource(path);
        }
        var url = owner.getResource(path);
        if (url != null) {
            return url;
        }
        return owner.getClassLoader().getResource(path);
    }

    private static FileSystem getJavaFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            try {
                return FileSystems.newFileSystem(uri, Map.of("create", "true"));
            } catch (FileSystemAlreadyExistsException e2) {
                // this will rarely happen when the first access happens from two threads at once
                return FileSystems.getFileSystem(uri);
            }
        }
    }

    @CheckForNull
    private static InputStream openStream(@CheckForNull Class<?> owner, String relativePath) throws IOException {
        var url = getResourceUrl(owner, relativePath);
        if (url == null) {
            return null;
        }
        return url.openStream();
    }
    // endregion

    private ResourceUtil() {}
}
