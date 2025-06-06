package nl.adamg.baizel.internal.bootstrap;

import nl.adamg.baizel.internal.bootstrap.javadsl.JavaDsl;
import nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mapToList;
import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mergeSet;
import static nl.adamg.baizel.internal.bootstrap.util.logging.Timer.timed;

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
        return mapToList(Arrays.asList(Files.readString(mavenClasspathFile).split(File.pathSeparator)), Path::of);
    }

    void writeCurrentBaizelChecksum(String currentBaizelChecksum) throws IOException {
        Files.writeString(baizelChecksumFile, currentBaizelChecksum);
    }

    String readLastBaizelChecksum() throws IOException {
        return Files.exists(baizelChecksumFile) ? Files.readString(baizelChecksumFile).trim() : "";
    }

    boolean build() throws IOException {
        var modulePaths = timed(LOG, () -> ModuleFinder.findModules(baizelRoot), "finding modules");
        var projectInfo = timed(LOG, () -> loadProjectInfo(baizelRoot.resolve("project-info.java")), "reading project-info.java");
        var mavenDependencyCoordinates = timed(LOG, () -> loadMavenDependencyCoordinates(projectInfo), "reading bootstra/module-info.java");
        var mavenClient = timed(LOG, () -> MavenClient.loadClient(getRepositories(projectInfo), getModuleCoordinates(projectInfo), baizelRoot), "loading Maven client");
        timed(LOG, () -> libraries.addAll(mapToList(mavenDependencyCoordinates, mavenClient::resolve)), "resolving Maven dependencies", true);
        Files.writeString(mavenClasspathFile, String.join(File.pathSeparator, mapToList(libraries, Path::toString)));
        var sourceRoots = new TreeSet<>(mapToList(modulePaths, p -> p.resolve("src/main/java")));
        Files.writeString(sourcePathFile, String.join(File.pathSeparator, mapToList(sourceRoots, Path::toString)));
        return timed(LOG, () -> compile(sourceRoots, mavenClasspathFile, sourcePathFile, compiledClasspathRoot), "compiling", true);
    }

    private List<String> loadMavenDependencyCoordinates(ObjectTree projectInfo) {
        return projectInfo.body().get("dependsOn").list();
    }

    private static List<String> getRepositories(ObjectTree projectInfo) {
        return projectInfo.body().get("repository").list();
    }

    private static Map<String, Set<String>> getModuleCoordinates(ObjectTree projectInfo) {
        List<List<?>> definitions = projectInfo.body().get("dependencies").body().list();
        var moduleNameToCoordinateMap = new TreeMap<String, Set<String>>();
        // definitions is a list of entries, each entry is a two element list. [0] is coordinate, [1] is a sub-list of all module names included
        for(var definition : definitions) {
            var coordinate = (String)definition.get(0);
            @SuppressWarnings("unchecked")
            var moduleNames = (List<List<String>>) definition.get(1);
            for(var moduleName : moduleNames) {
                moduleNameToCoordinateMap.computeIfAbsent(moduleName.get(0), k -> new TreeSet<>()).add(coordinate);
            }
        }
        return moduleNameToCoordinateMap;
    }

    private static ObjectTree loadProjectInfo(Path path) throws IOException {
        return ObjectTree.of(new JavaDsl().read(Files.readString(path)));
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
        var javacArgs = List.of(
                "--class-path", "@" + relativize(mavenClasspathFile),
                "--source-path", "@" + relativize(sourcePathFile),
                "-d", relativize(outputDir)
        );
        var task = compiler.getTask(
                new PrintWriter(logStream),
                fileManager,
                diagnosticListener,
                javacArgs,
                null,
                compilationUnits
        );
        if (LOG.isLoggable(Level.INFO)) {
            // TODO make sure this doesn't run without --verbose
            System.err.println("XXX LOG.isLoggable(Level.INFO)");
            var mavenClasspath = Files.readString(mavenClasspathFile);
            var sourcePath = Files.readString(sourcePathFile);
            LOG.info("$ javac " + String.join(" ", javacArgs) + " " + String.join(" ", mapToList(sourceFiles, BootstrapBuilder::relativize)));
            LOG.info("class-path -- { \"path\": \"" + mavenClasspath + "\" }");
            LOG.info("source-path -- { \"path\": \"" + sourcePath + "\" }");
            System.out.println("ZZZ LOG.isLoggable(Level.INFO)");
        }
        return task.call();
    }

    private static String relativize(Path path) {
        return Path.of(".").toAbsolutePath().relativize(path).toString();
    }
    
    //region generated code
    Path baizelRoot() {
        return baizelRoot;
    }

    Path compiledClasspathRoot() {
        return compiledClasspathRoot;
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
