package nl.adamg.baizel.internal.bootstrap;

import nl.adamg.baizel.internal.bootstrap.io.Checksum;
import nl.adamg.baizel.internal.bootstrap.java.DynamicClassLoader;
import nl.adamg.baizel.internal.bootstrap.util.logging.LoggerUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.logging.Logger;

import static nl.adamg.baizel.internal.bootstrap.util.logging.Timer.timed;

/**
 * Bootstrap Stage 2:
 * This class is compiled with cache, but can't import anything yet except core Java.
 * It checks checksum of the entire Baizel, if needed resolves libraries and rebuilds,
 * creates the full Baizel classloader with access to libraries and all the modules.
 */
public final class Bootstrap {
    private static final Instant START_TIMESTAMP = Instant.now();
    private static final Logger LOG = Logger.getLogger(Bootstrap.class.getName());

    /** Called as JVM entrypoint from 'bin/baizel' (Stage 1) */
    public static void main(String... args) throws Exception {
        LoggerUtil.configureLogger();
        LOG.info("Bootstrap Stage 2");

        // Step 2.1: we compute some paths, relative to the path of this .java file as detected via reflection
        var builder = BootstrapBuilder.makeBuilder(findBaizelDir());

        // Step 2.2: we check if the Baizel checksum matches
        var lastBaizelChecksum = builder.readLastBaizelChecksum();
        var currentBaizelChecksum = timed(LOG, () -> Checksum.directory(builder.baizelRoot()), "computing Baizel checksum");
        var isBaizelUpToDate = lastBaizelChecksum.equals(currentBaizelChecksum);

        // Step 2.3: if changes detected, rebuild everything
        var bootstrapClasspath = new TreeSet<Path>();
        if (! isBaizelUpToDate) {
            var compilationSucceeded = builder.build();
            if (!compilationSucceeded) {
                System.exit(202); // CliErrors.BOOTSTRAP_COMPILATION_FAILED
                return;
            }
            builder.writeCurrentBaizelChecksum(currentBaizelChecksum);
            bootstrapClasspath.addAll(builder.libraries());
            bootstrapClasspath.addAll(builder.getResourceRootsOfBuiltModules());
        } else {
            bootstrapClasspath.addAll(builder.readCachedMavenClasspath());
            bootstrapClasspath.addAll(builder.readCachedResourceRoots());
        }

        // Step 2.4: load the compiled Baizel into a child classloader in the current JVM and call baizel.cli main
        bootstrapClasspath.add(builder.compiledClasspathRoot());
        try(var bootstrapClassLoader = DynamicClassLoader.forPaths(bootstrapClasspath, Bootstrap.class)) {
            LOG.info("Stage 2 finished -- { \"durationMs\": " + Duration.between(START_TIMESTAMP, Instant.now()).toMillis() + " }");
            bootstrapClassLoader.activate(Thread.currentThread());
            bootstrapClassLoader.invoke("nl.adamg.baizel.cli.Baizel", "main", true, new Object[]{args});
        }
    }

    public static Path findBaizelDir() throws URISyntaxException, IOException {
        var baizelDir = Optional.of(System.getenv("BAIZEL_DIR")).map(Path::of).orElse(null);
        if (baizelDir != null && Files.isDirectory(baizelDir)) {
            return baizelDir.toRealPath();
        }
        var currentSourceUrl = Bootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        var currentSourcePath = Path.of(Objects.requireNonNull(currentSourceUrl).toURI()).toRealPath();
        var buildClasspathRelativePath = "internal/bootstrap/.build/classes/java/main";
        var currentClassPathString = currentSourcePath.toString().replaceAll("\\\\", "/");
        if (currentClassPathString.endsWith(buildClasspathRelativePath)) {
            var pathLength = currentClassPathString.length() - buildClasspathRelativePath.length();
            return Path.of(currentClassPathString.substring(0, pathLength)).toRealPath();
        }
        throw new BootstrapMethodError("unexpected location of Bootstrap.class)");
    }

    private Bootstrap() {}
}
