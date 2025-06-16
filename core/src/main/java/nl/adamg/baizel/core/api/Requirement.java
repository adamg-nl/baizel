package nl.adamg.baizel.core.api;

/// - API:    [nl.adamg.baizel.core.api.Requirement]
/// - Entity: [nl.adamg.baizel.core.entities.Requirement]
/// - Model:  [nl.adamg.baizel.core.model.Requirement]
public interface Requirement {
    boolean isSdkRequirement();
    boolean isTransitive();
    String moduleId();
}
