package nl.adamg.baizel.core.api;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/// - API:    [nl.adamg.baizel.core.api.Module]
/// - Entity: [nl.adamg.baizel.core.entities.Module]
/// - Impl:   [nl.adamg.baizel.core.impl.ModuleImpl]
@SuppressWarnings("JavadocReference")
public interface Module {
    /// Absolute path of the module root.
    /// Example: `/home/me/projects/nuclear-kitchen/toaster/api`.
    Path fullPath();

    @CheckForNull
    Path sourceRoot(SourceSet sourceSet);

    /// Relative to `project.root`. Example: `toaster/api`.
    String path();

    Map<String, Class> classes();

    List<Requirement> requirements() throws IOException;

    List<String> exports() throws IOException;

    Project project();

    /// Equal to the `projectId`.
    /// Example: `com.acme.nuclear.kitchen`
    String groupId();

    /// Equal to the relative path with slashes substituted with dashes.
    /// Also used as the `bundleName`.
    /// Example: `toaster-api`
    String artifactId();

    /// Made of the `projectId` and module path, with slashes substituted with dots.
    /// Also used as the `bundleSymbolicName` and `automaticModuleName`.
    /// Example: `com.acme.nuclear.kitchen.toaster.api`
    String moduleId();

    /// If the module contains a file named 'main', which contains a line in format:
    /// ```
    /// .*/baizel run //path/to/a/java/source/file/relative/to/the/project/Root.java
    /// ```
    /// then this method will return object representing that class.
    @CheckForNull
    Class mainClass() throws IOException;

    Path buildDir();

    /// @return empty string if there's no short description in README.md
    String shortDescription() throws IOException;

    /// @return empty string if there's no title in README.md
    String title() throws IOException;
}
