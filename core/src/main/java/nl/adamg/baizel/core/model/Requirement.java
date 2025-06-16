package nl.adamg.baizel.core.model;

import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.Requirement]
/// - Entity: [nl.adamg.baizel.core.entities.Requirement]
/// - Model:  [nl.adamg.baizel.core.model.Requirement]
public class Requirement extends EntityModel<nl.adamg.baizel.core.entities.Requirement, Requirement> implements nl.adamg.baizel.core.api.Requirement {
    //region factory
    public static nl.adamg.baizel.core.api.Requirement of(
            String moduleId,
            boolean isTransitive
    ) {
        return new Requirement(
                new nl.adamg.baizel.core.entities.Requirement(
                        moduleId,
                        isTransitive
                )
        );
    }
    //endregion

    /// @return true if this module is part of SDK, thus doesn't resolve to source module nor Maven artifact
    @Override
    public boolean isSdkRequirement() {
        return entity.moduleId.startsWith("java.");
    }

    //region getters
    @Override
    public boolean isTransitive() {
        return entity.isTransitive;
    }

    @Override
    public String moduleId() {
        return entity.moduleId;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return (entity.isTransitive ? "transitive " : "") + entity.moduleId;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.Requirement, ?>> fields() {
        return List.of(
                e -> e.isTransitive,
                e -> e.moduleId
        );
    }
    //endregion

    //region generated code
    public Requirement(nl.adamg.baizel.core.entities.Requirement entity) {
        super(entity);
    }
    //endregion
}
