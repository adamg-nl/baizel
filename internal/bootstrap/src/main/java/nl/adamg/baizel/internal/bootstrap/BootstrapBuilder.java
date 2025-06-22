package nl.adamg.baizel.internal.bootstrap;

import com.sun.source.util.JavacTask;
import nl.adamg.baizel.internal.bootstrap.javadsl.JavaDsl;
import nl.adamg.baizel.internal.bootstrap.util.collections.Items;
import nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mapToList;
import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mergeSet;
import static nl.adamg.baizel.internal.bootstrap.util.logging.Timer.timed;

class BootstrapBuilder {
    private static final Logger LOG = Logger.getLogger(BootstrapBuilder.class.getName());
    private final Path baizelRoot;
    private final Path mavenClasspathFile;
    private final Path resourceRootsFile;
    private final Path compiledClasspathRoot;
    private final Path baizelChecksumFile;
    private final Set<Path> libraries = new TreeSet<>();
    private final Set<Path> resourceRoots = new TreeSet<>();

    static BootstrapBuilder makeBuilder(Path baizelRoot) throws IOException {
        var bootstrapDir = baizelRoot.resolve("internal/bootstrap");
        var bootstrapBuildDir = bootstrapDir.resolve(".build");
        var bootstrapCompileRoot = bootstrapBuildDir.resolve("classes/java/main");
        var mavenClasspathFile = bootstrapBuildDir.resolve("maven.classpath");
        var resourceRootsFile = bootstrapBuildDir.resolve("resources.classpath");
        var baizelChecksumFile = bootstrapBuildDir.resolve("baizel.checksum");
        Files.createDirectories(bootstrapBuildDir);
        Files.createDirectories(bootstrapCompileRoot);
        return new BootstrapBuilder(
                baizelRoot,
                bootstrapDir,
                bootstrapBuildDir,
                mavenClasspathFile,
                resourceRootsFile,
                bootstrapCompileRoot,
                baizelChecksumFile);
    }

    List<Path> readCachedMavenClasspath() throws IOException {
        return mapToList(Arrays.asList(Files.readString(mavenClasspathFile).split(File.pathSeparator)), Path::of);
    }

    List<Path> readCachedResourceRoots() throws IOException {
        return mapToList(Arrays.asList(Files.readString(resourceRootsFile).split(File.pathSeparator)), Path::of);
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
        resourceRoots.addAll(mapToList(modulePaths, p -> p.resolve("src/main/resources")));
        Files.writeString(resourceRootsFile, String.join(File.pathSeparator, mapToList(resourceRoots, Path::toString)));
        return timed(LOG, () -> compile(sourceRoots, libraries, compiledClasspathRoot), "compiling", true);
    }

    Set<Path> getResourceRootsOfBuiltModules() {
        return resourceRoots;
    }

    private List<String> loadMavenDependencyCoordinates(ObjectTree projectInfo) {
        return projectInfo.body().get("dependencies").body().keys();
    }

    private static List<String> getRepositories(ObjectTree projectInfo) {
        return projectInfo.body().get("repository").get("maven").list();
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
            return stream.filter(p -> p.toString().endsWith(".java") && ! p.getFileName().toString().equals("module-info.java")).toList();
        }
    }

    private static boolean compile(Set<Path> sourceRoots, Set<Path> artifactRoots, Path outputDir) throws IOException {
        var logStream = System.err;
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnosticListener = (DiagnosticListener<JavaFileObject>)logStream::println;
        var fileManager = compiler.getStandardFileManager(diagnosticListener, null, null);
        var sourceFiles = mergeSet(mapToList(sourceRoots, BootstrapBuilder::findJavaSources));
        var compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles.stream().map(Path::toFile).toList());
        var javacArgs = new ArrayList<>(List.of(
                "-d", relativize(outputDir), // output dir
                "-g", "-parameters", // extended metadata
                "-implicit:none"
        ));
        fileManager.setLocation(StandardLocation.CLASS_PATH, Items.mapToList(artifactRoots, Path::toFile));
        if (System.getenv("BAIZEL_DEBUG") != null) {
            LOG.info("$ javac " + String.join(" ", javacArgs) + " ...");
        }
        var task = (JavacTask) compiler.getTask(
                new PrintWriter(logStream),
                fileManager,
                diagnosticListener,
                javacArgs,
                null,
                compilationUnits
        );
        return task.call();
    }

    private static String relativize(Path path) {
        return Path.of(".").toAbsolutePath().relativize(path.toAbsolutePath()).toString();
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

    BootstrapBuilder(Path baizelRoot, Path bootstrapDir, Path bootstrapBuildDir, Path mavenClasspathFile, Path resourceRootsFile, Path compiledClasspathRoot, Path baizelChecksumFile) {
        this.baizelRoot = baizelRoot;
        this.mavenClasspathFile = mavenClasspathFile;
        this.resourceRootsFile = resourceRootsFile;
        this.compiledClasspathRoot = compiledClasspathRoot;
        this.baizelChecksumFile = baizelChecksumFile;
    }
    //endregion
}
