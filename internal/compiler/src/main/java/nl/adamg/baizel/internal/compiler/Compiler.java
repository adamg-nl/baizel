package nl.adamg.baizel.internal.compiler;

import com.sun.source.util.JavacTask;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import nl.adamg.baizel.internal.bootstrap.util.collections.Items;
import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mapToList;
import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mergeSet;

@ThreadSafe // synchronized
public class Compiler {
    private static final Logger LOG = Logger.getLogger(Compiler.class.getName());
    private final List<Diagnostic<? extends JavaFileObject>> issues;
    private final JavaCompiler compiler;
    private final StandardJavaFileManager fileManager;
    private final PrintStream logStream;

    public static Compiler create(PrintStream logStream) {
        var issues = new ArrayList<Diagnostic<? extends JavaFileObject>>();
        var compiler = ToolProvider.getSystemJavaCompiler();
        return new Compiler(
                issues,
                compiler,
                compiler.getStandardFileManager(issues::add, null, null),
                logStream
        );
    }

    public synchronized List<Diagnostic<? extends JavaFileObject>> compile(
            Set<Path> sourceRoots,
            Set<Path> artifactRoots,
            Path outputDir) throws IOException {
        issues.clear();
        var sourceFiles = mergeSet(mapToList(sourceRoots, Compiler::findJavaSources));
        var compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles.stream().map(Path::toFile).toList());
        var javacArgs = new ArrayList<>(List.of(
                "-d", outputDir.toString(),
                "-g", "-parameters", // extended metadata
                "-implicit:none"
        ));
        fileManager.setLocation(StandardLocation.CLASS_PATH, Items.mapToList(artifactRoots, Path::toFile));
        LOG.info("$ javac " + String.join(" ", javacArgs) + " ...");
        var task = (JavacTask) compiler.getTask(
                new PrintWriter(logStream),
                fileManager,
                issues::add,
                javacArgs,
                null,
                compilationUnits
        );
        var success = task.call();
        if (! success && issues.isEmpty()) {
            LOG.warning("javac reported failure but no diagnostics were added");
        }
        var issues = new ArrayList<>(this.issues);
        this.issues.clear();
        return issues;
    }

    private static List<Path> findJavaSources(Path directory) throws IOException {
        try(var stream = Files.walk(directory)) {
            return stream.filter(p -> p.toString().endsWith(".java") && ! p.getFileName().toString().equals("module-info.java")).toList();
        }
    }

    //region generated code
    public Compiler(List<Diagnostic<? extends JavaFileObject>> issues, JavaCompiler compiler, StandardJavaFileManager fileManager, PrintStream logStream) {
        this.issues = issues;
        this.compiler = compiler;
        this.fileManager = fileManager;
        this.logStream = logStream;
    }
    //endregion
}
