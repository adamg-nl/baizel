package nl.adamg.baizel.core.model;

import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.Class]
/// - Entity: [nl.adamg.baizel.core.entities.Class]
/// - Model:  [nl.adamg.baizel.core.model.Class]
public class Class extends EntityModel<nl.adamg.baizel.core.entities.Class, Class> implements nl.adamg.baizel.core.api.Class {
    private final Module module;

    //region getters
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

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.Class, ?>> fields() {
        return List.of(
                c -> c.canonicalName,
                c -> c.imports
        );
    }
    //endregion

    //region generated code
    public Class(Module module, nl.adamg.baizel.core.entities.Class entity) {
        super(entity);
        this.module = module;
    }
    //endregion
}
