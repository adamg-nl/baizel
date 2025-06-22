package nl.adamg.baizel.core.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.Class;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.core.api.SourceRoot;
import nl.adamg.baizel.internal.common.util.EntityModel;

public class SourceRootImpl extends EntityModel<nl.adamg.baizel.core.entities.SourceRoot> implements SourceRoot {
    private final Module module;
    private final Map<String, Class> classes = new TreeMap<>();

    //region factory

    //endregion

    @Override
    public String path() {
        return entity.path;
    }

    @CheckForNull
    @Override
    public Class getClass(String qualifiedName) {
        return null;
    }

    @Override
    public Collection<Class> getAllClasses() {
        return List.of();
    }

    @Override
    public Module module() {
        return null;
    }

    @Override
    public String toString() {
        return entity.path;
    }

    //region generated code
    public SourceRootImpl(nl.adamg.baizel.core.entities.SourceRoot entity, Module module){
        super(entity);
        this.module = module;
    }
    //endregion
}
