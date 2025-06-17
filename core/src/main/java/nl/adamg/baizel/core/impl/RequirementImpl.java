package nl.adamg.baizel.core.impl;

import java.util.Set;
import nl.adamg.baizel.core.api.Requirement;
import nl.adamg.baizel.internal.common.util.EntityModel;

import nl.adamg.baizel.internal.common.util.collections.Items;

/// - API:    [nl.adamg.baizel.core.api.Requirement]
/// - Entity: [nl.adamg.baizel.core.entities.Requirement]
/// - Model:  [nl.adamg.baizel.core.impl.RequirementImpl]
public class RequirementImpl
        extends EntityModel<nl.adamg.baizel.core.entities.Requirement>
        implements Requirement {
    private static final Set<String> SDK_PACKAGES = Set.of("java", "jdk");

    //region factory
    public static Requirement of(
            String moduleId,
            boolean isTransitive
    ) {
        return new RequirementImpl(
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
        return Items.anyMatch(SDK_PACKAGES, p -> entity.moduleId.startsWith(p + "."));
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
    //endregion

    //region generated code
    public RequirementImpl(nl.adamg.baizel.core.entities.Requirement entity) {
        super(entity);
    }
    //endregion
}
