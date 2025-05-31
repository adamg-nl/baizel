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
    private final TreeSet<Path> moduleDefFiles;

    private ModuleFinder(TreeSet<Path> moduleDefFiles) {
        this.moduleDefFiles = moduleDefFiles;
    }

    static Set<Path> findModules(Path baizelRoot) throws IOException {
        var moduleDefFiles = new TreeSet<Path>();
        Files.walkFileTree(baizelRoot, new ModuleFinder(moduleDefFiles));
        return moduleDefFiles;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return dir.getFileName().toString().startsWith(".") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (file.getFileName().toString().equals("build.gradle") && Files.isDirectory(file.getParent().resolve("src/main/java"))) {
            moduleDefFiles.add(file.getParent());
        }
        return FileVisitResult.CONTINUE;
    }
}
