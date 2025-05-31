package nl.adamg.baizel.internal.bootstrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.adamg.baizel.internal.bootstrap.Timer.timed;

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
        configureLogger();
        LOG.info("Bootstrap Stage 2");

        // Step 2.1: we compute some paths, relative to the path of this .java file as detected via reflection
        var builder = BootstrapBuilder.makeBuilder(findBaizelRoot());

        // Step 2.2: we check if the Baizel checksum matches
        var lastBaizelChecksum = builder.readLastBaizelChecksum();
        var currentBaizelChecksum = timed(LOG, () -> Checksum.directory(builder.baizelRoot()), "computing Baizel checksum");
        var isBaizelUpToDate = lastBaizelChecksum.equals(currentBaizelChecksum);

        // Step 2.3: if changes detected, rebuild everything
        var libraries = new TreeSet<Path>();
        if (! isBaizelUpToDate) {
            var compilationSucceeded = builder.build();
            if (!compilationSucceeded) {
                System.exit(201);
                return;
            }
            builder.writeCurrentBaizelChecksum(currentBaizelChecksum);
            libraries.addAll(builder.libraries());
        } else {
            libraries.addAll(builder.readCachedMavenClasspath());
        }

        // Step 2.4: load the compiled Baizel into a child classloader in the current JVM and call baizel.cli main
        var bootstrapClasspath = new ArrayList<>(libraries);
        bootstrapClasspath.add(builder.compiledClasspathRoot());
        try(var bootstrapClassLoader = DynamicClassLoader.forPaths(bootstrapClasspath, Bootstrap.class)) {
            LOG.info("Stage 2 finished -- { \"durationMs\": " + Duration.between(START_TIMESTAMP, Instant.now()).toMillis() + " }");
            bootstrapClassLoader.invoke("nl.adamg.baizel.cli.Baizel", "main", (Object)args);
        }
    }

    private static void configureLogger() {
        if ("true".equals(System.getenv("BAIZEL_VERBOSE"))) {
            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tH:%1$tM:%1$tS.%1$tL %4$-4s [%3$s] %5$s%n");
            System.setProperty("java.util.logging.ConsoleHandler.level", Level.FINEST.getName());
            var formatter = new SourceInfoEnhancer();
            for (var handler : Logger.getLogger("").getHandlers()) {
                handler.setFormatter(formatter);
            }
        } else {
            System.setProperty("java.util.logging.SimpleFormatter.format", "");
            System.setProperty("java.util.logging.ConsoleHandler.level", Level.OFF.getName());
        }
    }

    public static Path findBaizelRoot() throws URISyntaxException, IOException {
        var currentSourceUrl = Bootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        var currentSourcePath = Path.of(Objects.requireNonNull(currentSourceUrl).toURI()).toRealPath();
        var buildClasspathRelativePath = "internal/bootstrap/.build/classes/java/main";
        var currentClassPathString = currentSourcePath.toString();
        if (currentClassPathString.endsWith(buildClasspathRelativePath)) {
            var pathLength = currentClassPathString.length() - buildClasspathRelativePath.length();
            return Path.of(currentClassPathString.substring(0, pathLength));
        }
        throw new BootstrapMethodError("unexpected location of Bootstrap.class)");
    }

    private Bootstrap() {}
}
