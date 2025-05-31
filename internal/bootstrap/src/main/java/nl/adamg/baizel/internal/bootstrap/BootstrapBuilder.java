package nl.adamg.baizel.internal.bootstrap;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static nl.adamg.baizel.internal.bootstrap.Collections.mapToList;
import static nl.adamg.baizel.internal.bootstrap.Collections.mergeSet;
import static nl.adamg.baizel.internal.bootstrap.Timer.timed;

class BootstrapBuilder {
    private static final Logger LOG = Logger.getLogger(BootstrapBuilder.class.getName());
    private final Path baizelRoot;
    private final Path bootstrapDir;
    private final Path bootstrapBuildDir;
    private final Path mavenClasspathFile;
    private final Path sourcePathFile;
    private final Path compiledClasspathRoot;
    private final Path baizelChecksumFile;
    private final Set<Path> libraries = new TreeSet<>();

    static BootstrapBuilder makeBuilder(Path baizelRoot) throws IOException {
        var bootstrapDir = baizelRoot.resolve("internal/bootstrap");
        var bootstrapBuildDir = bootstrapDir.resolve(".build");
        var bootstrapCompileRoot = bootstrapBuildDir.resolve("classes/java/main");
        var mavenClasspathFile = bootstrapBuildDir.resolve("maven.classpath");
        var sourcePathFile = bootstrapBuildDir.resolve("src.classpath");
        var baizelChecksumFile = bootstrapBuildDir.resolve("baizel.checksum");
        Files.createDirectories(bootstrapBuildDir);
        Files.createDirectories(bootstrapCompileRoot);
        return new BootstrapBuilder(
                baizelRoot,
                bootstrapDir,
                bootstrapBuildDir,
                mavenClasspathFile,
                sourcePathFile,
                bootstrapCompileRoot,
                baizelChecksumFile);
    }

    List<Path> readCachedMavenClasspath() throws IOException {
        return mapToList(Arrays.asList(Files.readString(mavenClasspathFile).split(":")), Path::of);
    }

    void writeCurrentBaizelChecksum(String currentBaizelChecksum) throws IOException {
        Files.writeString(baizelChecksumFile, currentBaizelChecksum);
    }

    String readLastBaizelChecksum() throws IOException {
        return Files.exists(baizelChecksumFile) ? Files.readString(baizelChecksumFile()).trim() : "";
    }

    boolean build() throws IOException {
        var modulePaths = timed(LOG, () -> ModuleFinder.findModules(baizelRoot), "finding modules");
        var mavenDependencyCoordinates = timed(LOG, () -> readMavenDependencies(modulePaths), "reading module definitions");
        var mavenClient = timed(LOG, () -> MavenClient.loadClient(baizelRoot), "loading Maven client");
        timed(LOG, () -> libraries.addAll(mapToList(mavenDependencyCoordinates, mavenClient::resolve)), "resolving Maven dependencies");
        Files.writeString(mavenClasspathFile, String.join(":", mapToList(libraries, Path::toString)));
        var sourceRoots = new TreeSet<>(mapToList(modulePaths, p -> p.resolve("src/main/java")));
        Files.writeString(sourcePathFile, String.join(":", mapToList(sourceRoots, Path::toString)));
        return timed(LOG, () -> compile(sourceRoots, mavenClasspathFile, sourcePathFile, compiledClasspathRoot), "compiling");
    }

    private static Set<String> readMavenDependencies(Set<Path> modulePaths) throws IOException {
        return mergeSet(Collections.mapToList(modulePaths, BootstrapBuilder::readMavenDependencies));
    }

    private static Set<String> readMavenDependencies(Path module) throws IOException {
        var dependencies = new TreeSet<String>();
        var moduleConfig = readGradleConfig(module.resolve("build.gradle"));
        for (var configuration : List.of("implementation", "runtimeOnly", "api", "compileOnly", "compileOnlyApi")) {
            var configurationDependencies = moduleConfig.get(configuration);
            if (configurationDependencies != null) {
                dependencies.addAll(configurationDependencies);
            }
        }
        return dependencies;
    }

    private static List<Path> findJavaSources(Path directory) throws IOException {
        try(var stream = Files.walk(directory)) {
            return stream.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static boolean compile(Set<Path> sourceRoots, Path mavenClasspathFile, Path sourcePathFile, Path outputDir) throws IOException {
        var logStream = System.err;
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnosticListener = (DiagnosticListener<JavaFileObject>)logStream::println;
        var fileManager = compiler.getStandardFileManager(diagnosticListener, null, null);
        var sourceFiles = mergeSet(mapToList(sourceRoots, BootstrapBuilder::findJavaSources));
        var compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles.stream().map(Path::toFile).toList());
        var task = compiler.getTask(
                new PrintWriter(logStream),
                fileManager,
                diagnosticListener,
                List.of(
                        "--class-path", "@" + mavenClasspathFile + ":" + outputDir,
                        "--source-path", "@" + sourcePathFile,
                        "-d", outputDir.toString()
                ),
                null,
                compilationUnits
        );
        return task.call();
    }

    static Map<String, List<String>> readGradleConfig(Path gradleGroovyFile) throws IOException {
        // this only has to be good enough to extract minimum of information from specific bootstrap gradle file
        var data = new LinkedHashMap<String, List<String>>();
        var assignmentPattern = Pattern.compile("^ *(?<key>[^(]+)\\(\"(?<value>[^\"]+)\"\\) *(//.*)?$");
        for (var line : Files.readAllLines(gradleGroovyFile)) {
            var matcher = assignmentPattern.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            var key = matcher.group("key");
            var value = matcher.group("value");
            data.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return data;
    }
    
    //region generated code
    Path baizelRoot() {
        return baizelRoot;
    }

    Path bootstrapDir() {
        return bootstrapDir;
    }

    Path bootstrapBuildDir() {
        return bootstrapBuildDir;
    }

    Path mavenClasspathFile() {
        return mavenClasspathFile;
    }

    Path sourcePathFile() {
        return sourcePathFile;
    }

    Path compiledClasspathRoot() {
        return compiledClasspathRoot;
    }

    Path baizelChecksumFile() {
        return baizelChecksumFile;
    }

    Set<Path> libraries() {
        return libraries;
    }

    BootstrapBuilder(Path baizelRoot, Path bootstrapDir, Path bootstrapBuildDir, Path mavenClasspathFile, Path sourcePathFile, Path compiledClasspathRoot, Path baizelChecksumFile) {
        this.baizelRoot = baizelRoot;
        this.bootstrapDir = bootstrapDir;
        this.bootstrapBuildDir = bootstrapBuildDir;
        this.mavenClasspathFile = mavenClasspathFile;
        this.sourcePathFile = sourcePathFile;
        this.compiledClasspathRoot = compiledClasspathRoot;
        this.baizelChecksumFile = baizelChecksumFile;
    }
    //endregion
}
