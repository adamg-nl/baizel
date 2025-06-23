package nl.adamg.baizel.core.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.Class;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.core.api.ContentRoot;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.util.EntityModel;

/// - API:    [nl.adamg.baizel.core.api.ContentRoot]
/// - Entity: [nl.adamg.baizel.core.entities.ContentRoot]
/// - Impl:   [nl.adamg.baizel.core.impl.ContentRootImpl]
public class ContentRootImpl extends EntityModel<nl.adamg.baizel.core.entities.ContentRoot> implements ContentRoot {
    private final Module module;
    /// key: qualified class name
    private final Map<String, Class> classes = new TreeMap<>();
    private final Target type;
    private final FileSystem fileSystem;

    //region factory
    public static ContentRoot of(
            Module module,
            Target type,
            FileSystem fileSystem
    ) {
        return new ContentRootImpl(
                module,
                type,
                fileSystem,
                new nl.adamg.baizel.core.entities.ContentRoot(
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
    public Target target() {
        return type;
    }

    @Override
    public Path fullPath() {
        return module.fullPath().resolve(type.contentRoot());
    }

    @Override
    public List<ContentRoot> resources() {
        var output = new ArrayList<ContentRoot>();
        for (var target : type.resources()) {
            var contentRoot = module.getContentRoot(target);
            if (contentRoot != null) {
                output.add(contentRoot);
            }
        }
        return output;
    }

    @Override
    public String toString() {
        return type.contentRoot();
    }

    //region implementation internals
    //endregion

    //region generated code
    public ContentRootImpl(Module module, Target type, FileSystem fileSystem, nl.adamg.baizel.core.entities.ContentRoot entity){
        super(entity);
        this.module = module;
        this.type = type;
        this.fileSystem = fileSystem;
    }
    //endregion
}
