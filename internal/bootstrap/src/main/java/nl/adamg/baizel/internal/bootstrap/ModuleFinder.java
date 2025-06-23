package nl.adamg.baizel.internal.bootstrap;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;

class ModuleFinder extends SimpleFileVisitor<Path> {
    private static final int DEPTH_LIMIT = 10;
    private final TreeSet<Path> moduleDefFiles;
    private final Path projectRoot;

    private ModuleFinder(TreeSet<Path> moduleDefFiles, Path projectRoot) {
        this.moduleDefFiles = moduleDefFiles;
        this.projectRoot = projectRoot;
    }

    static Set<Path> findModules(Path baizelRoot) throws IOException {
        var moduleDefFiles = new TreeSet<Path>();
        Files.walkFileTree(baizelRoot, new ModuleFinder(moduleDefFiles, baizelRoot));
        return moduleDefFiles;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (projectRoot.relativize(dir).getNameCount() > DEPTH_LIMIT) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        var name = dir.getFileName().toString();
        return (name.startsWith(".") && ! name.equals(".baizel")) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.endsWith(Path.of("src/main/java/module-info.java"))) {
            moduleDefFiles.add(getParent(file, 4));
        }
        return FileVisitResult.CONTINUE;
    }

    private Path getParent(Path path, int level) {
        for(int i=0; i<level; i++) {
            path = path.getParent();
        }
        return path;
    }
}
