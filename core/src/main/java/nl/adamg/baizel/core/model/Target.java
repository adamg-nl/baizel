package nl.adamg.baizel.core.model;

import nl.adamg.baizel.core.SourceSets;
import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.internal.common.util.EntityModel;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.Target]
/// - Entity: [nl.adamg.baizel.core.entities.Target]
/// - Model:  [nl.adamg.baizel.core.model.Target]
public class Target extends EntityModel<nl.adamg.baizel.core.entities.Target, Target> implements nl.adamg.baizel.core.api.Target {
    public static Target module(String path) {
        return new Target(new nl.adamg.baizel.core.entities.Target("", "", path, ""));
    }

    public static Target artifact(String organization, String artifact) {
        return new Target(new nl.adamg.baizel.core.entities.Target(organization, artifact, "", ""));
    }

    /// @return null if this target is not a project module (e.g. it's an artifact)
    @CheckForNull
    public Module getModule(Project project) {
        if (!artifact().isEmpty() || entity.path.isEmpty()) {
            return null;
        }
        return project.getModuleOf(project.root().resolve(entity.path));
    }

    public SourceSet getSourceSet() {
        return ! entity.targetName.isEmpty() ? SourceSets.get(entity.targetName) : SourceSets.main();
    }

    //region get
    public String organization() {
        return entity.organization;
    }

    public String artifact() {
        return entity.artifact;
    }

    public String path() {
        return entity.path;
    }

    public String targetName() {
        return entity.targetName;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (!entity.organization.isEmpty()) {
            sb.append('@').append(entity.organization);
            if (!entity.artifact.isEmpty()) {
                sb.append('/').append(entity.artifact);
            }
        }
        if (! entity.path.isEmpty()) {
            sb.append("//").append(entity.path);
        }
        if (!entity.targetName.isEmpty()) {
            sb.append(':').append(entity.targetName);
        }
        return sb.toString();
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.Target, ?>> fields() {
        return List.of(
                t -> t.organization,
                t -> t.artifact,
                t -> t.path,
                t -> t.targetName
        );
    }
    //endregion

    //region generated code
    public Target(nl.adamg.baizel.core.entities.Target entity) {
        super(entity);
    }
    //endregion
}
