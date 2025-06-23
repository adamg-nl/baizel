package nl.adamg.baizel.core.impl;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.Class;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.core.api.SourceRoot;
import nl.adamg.baizel.core.api.TargetType;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.util.EntityModel;

public class SourceRootImpl extends EntityModel<nl.adamg.baizel.core.entities.SourceRoot> implements SourceRoot {
    private final Module module;
    /// key: qualified class name
    private final Map<String, Class> classes = new TreeMap<>();
    private final TargetType type;
    private final FileSystem fileSystem;

    //region factory
    public static SourceRoot of(
            Module module,
            TargetType type,
            FileSystem fileSystem
    ) {
        return new SourceRootImpl(
                module,
                type,
                fileSystem,
                new nl.adamg.baizel.core.entities.SourceRoot(
                        new TreeMap<>()
                )
        );
    }

    //endregion

    @CheckForNull
    @Override
    public Class getClass(String qualifiedName) {
        return classes.computeIfAbsent(qualifiedName, n -> ClassImpl.load(n, this, fileSystem));
    }

    @Override
    public Collection<Class> getAllClasses() {
        return List.of();
    }

    @Override
    public Module module() {
        return module;
    }

    @Override
    public TargetType type() {
        return type;
    }

    @Override
    public Path fullPath() {
        return module.fullPath().resolve(type.getPath());
    }

    @CheckForNull
    @Override
    public SourceRoot resources() {
        var resourceTargetType = type.resourceTarget();
        if (resourceTargetType == null) {
            return null;
        }
        return module.getTarget(resourceTargetType);
    }

    @Override
    public String toString() {
        return type.getPath();
    }

    //region implementation internals
    //endregion

    //region generated code
    public SourceRootImpl(Module module, TargetType type, FileSystem fileSystem, nl.adamg.baizel.core.entities.SourceRoot entity){
        super(entity);
        this.module = module;
        this.type = type;
        this.fileSystem = fileSystem;
    }
    //endregion
}
