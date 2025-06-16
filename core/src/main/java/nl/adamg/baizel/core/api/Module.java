package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.model.Requirement;
import nl.adamg.baizel.core.model.Class;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/// - API:    [nl.adamg.baizel.core.api.Module]
/// - Entity: [nl.adamg.baizel.core.entities.Module]
/// - Model:  [nl.adamg.baizel.core.model.Module]
public interface Module {
    Path fullPath();
    @CheckForNull Path getSourceRoot(SourceSet sourceSet);
    String path();
    Map<String, Class> classes();
    List<Requirement> requirements() throws IOException;
    List<String> exports() throws IOException;
}
