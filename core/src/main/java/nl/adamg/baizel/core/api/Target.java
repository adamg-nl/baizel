package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.model.Module;
import nl.adamg.baizel.core.model.Project;

import javax.annotation.CheckForNull;

/// Format: `[@[<ORG>/]<ARTIFACT>][//<PATH>][:<TARGET_NAME>]`
/// Example: `@foo/bar//baz/qux:main`
/// Example: `//baz/qux`
///
/// - API:    [nl.adamg.baizel.core.api.Target]
/// - Entity: [nl.adamg.baizel.core.entities.Target]
/// - Model:  [nl.adamg.baizel.core.model.Target]
public interface Target {
    @CheckForNull Module getModule(Project project);
    SourceSet getSourceSet();
    String organization();
    String artifact();
    String path();
    String targetName();
}
