package nl.adamg.baizel.core.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.Class;
import nl.adamg.baizel.core.api.ContentRoot;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Class]
/// - Entity: [nl.adamg.baizel.core.entities.Class]
/// - Impl:   [nl.adamg.baizel.core.impl.ClassImpl]
public class ClassImpl
        extends EntityModel<nl.adamg.baizel.core.entities.Class>
        implements Class {
    private final ContentRoot sourceRoot;
    private final FileSystem fileSystem;

    //region factory
    public static Class of(
            String canonicalName,
            ContentRoot contentRoot,
            FileSystem fileSystem
    ) {
        return new ClassImpl(
                contentRoot,
                fileSystem,
                new nl.adamg.baizel.core.entities.Class(
                        canonicalName,
                        new ArrayList<>()
                )
        );
    }

    @CheckForNull
    public static Class load(String className, ContentRoot contentRoot, FileSystem fileSystem) {
        var path = contentRoot.fullPath().resolve(classNameToSourcePath(className));
        if (! fileSystem.exists(path)) {
            return null;
        }
        return of(className, contentRoot, fileSystem);
    }
    //endregion

    /// @param pathRelativeToSourceRoot path relative to the source root
    public static String sourcePathToClassName(String pathRelativeToSourceRoot) {
        return pathRelativeToSourceRoot
                .replaceFirst("\\.java$", "")
                .replaceAll("/", ".");
    }

    /// @return path relative to the source root
    public static String classNameToSourcePath(String className) {
        return className.replaceAll("\\.", "/") + ".java";
    }

    @Override
    public Path fullPath() {
        return sourceRoot.fullPath().resolve(classNameToSourcePath(entity.canonicalName));
    }

    //region getters
    @Override
    public ContentRoot sourceRoot() {
        return sourceRoot;
    }

    @Override
    public String canonicalName() {
        return entity.canonicalName;
    }

    @Override
    public List<String> imports() {
        return entity.imports;
    }
    //endregion

    @Override
    public String toString() {
        return entity.canonicalName;
    }

    //region implementation internals
    //endregion

    //region generated code
    public ClassImpl(ContentRoot sourceRoot, FileSystem fileSystem, nl.adamg.baizel.core.entities.Class entity) {
        super(entity);
        this.sourceRoot = sourceRoot;
        this.fileSystem = fileSystem;
    }
    //endregion
}
