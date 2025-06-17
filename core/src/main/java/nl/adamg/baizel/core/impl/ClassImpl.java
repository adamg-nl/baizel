package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Class;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Class]
/// - Entity: [nl.adamg.baizel.core.entities.Class]
/// - Model:  [nl.adamg.baizel.core.impl.ClassImpl]
public class ClassImpl
        extends EntityModel<nl.adamg.baizel.core.entities.Class>
        implements Class {
    private final Module module;

    //region factory
    public static Class of(
            Module module,
            String canonicalName,
            List<String> imports
    ) {
        return new ClassImpl(
                module,
                new nl.adamg.baizel.core.entities.Class(
                        canonicalName,
                        imports
                )
        );
    }
    //endregion

    //region getters
    @Override
    public Module module() {
        return module;
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

    //region entity model
    @Override
    public String toString() {
        return entity.canonicalName;
    }
    //endregion

    //region generated code
    public ClassImpl(Module module, nl.adamg.baizel.core.entities.Class entity) {
        super(entity);
        this.module = module;
    }
    //endregion
}
