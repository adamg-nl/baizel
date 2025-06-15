package nl.adamg.baizel.core;

import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

public class Requirement extends EntityModel<nl.adamg.baizel.core.entities.Requirement, Requirement> {
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
