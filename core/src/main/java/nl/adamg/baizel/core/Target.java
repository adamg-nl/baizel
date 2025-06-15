package nl.adamg.baizel.core;

import nl.adamg.baizel.internal.common.util.EntityModel;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.function.Function;

/// Format: `[@[<ORG>/]<ARTIFACT>][//<PATH>][:<TARGET_NAME>]`
/// Example: `@foo/bar//baz/qux:main`
/// Example: `//baz/qux`
public class Target extends EntityModel<nl.adamg.baizel.core.entities.Target, Target> {
    /**
     * @return null if this target is not a project module (e.g. it's an artifact)
     */
    @CheckForNull
    public Module getModule(Project project) {
        if (artifact() != null || entity.path == null) {
            return null;
        }
        return project.getModuleOf(project.root().resolve(entity.path));
    }

    //region getters
    @CheckForNull
    public String organization() {
        return entity.organization;
    }

    @CheckForNull
    public String artifact() {
        return entity.artifact;
    }

    @CheckForNull
    public String path() {
        return entity.path;
    }

    @CheckForNull
    public String targetName() {
        return entity.targetName;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (entity.organization != null) {
            sb.append('@').append(entity.organization);
            if (entity.artifact != null) {
                sb.append('/').append(entity.artifact);
            }
        }
        sb.append("//").append(entity.path);
        if (entity.targetName != null) {
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
