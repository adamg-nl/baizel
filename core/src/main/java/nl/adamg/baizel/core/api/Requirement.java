package nl.adamg.baizel.core.api;

/// - API:    [nl.adamg.baizel.core.api.Requirement]
/// - Entity: [nl.adamg.baizel.core.entities.Requirement]
/// - Impl:   [nl.adamg.baizel.core.impl.RequirementImpl]
public interface Requirement {
    boolean isSdkRequirement();
    boolean isTransitive();
    String moduleId();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
