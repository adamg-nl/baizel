package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.BaizelException;
import nl.adamg.baizel.core.SourceSets;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.core.api.Project;
import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.Target]
/// - Entity: [nl.adamg.baizel.core.entities.Target]
/// - Model: [nl.adamg.baizel.core.impl.TargetImpl]
public class TargetImpl
        extends EntityModel<Target, nl.adamg.baizel.core.entities.Target, TargetImpl>
        implements Target {
    //region factory
    public static Target of(
            String org, 
            String artifact, 
            String path, 
            String name
    ) {
        return new TargetImpl(
                new nl.adamg.baizel.core.entities.Target(
                        org,
                        artifact,
                        path,
                        name
                )
        );
    }

    public static Target module(String path) {
        return of("", "", path, "");
    }

    /// @param artifact qualified Java module id (that will be mapped to Maven coordinates)
    public static Target artifact(String organization, String artifact) {
        return of(organization, artifact, "", "");
    }

    public static Target parseTarget(String input) {
        var org = "";
        var mod = "";
        var name = "";

        var at = input.indexOf('@');
        var slashSlash = input.indexOf("//");
        var colon = input.indexOf(':');

        if (at != -1 && at < slashSlash) {
            var orgMod = input.substring(at + 1, slashSlash).split("/", 2);
            org = orgMod[0];
            if (orgMod.length > 1) {
                mod = orgMod[1];
            }
        }

        var pathStart = slashSlash + 2;
        var pathEnd = colon != -1 ? colon : input.length();
        var path = input.substring(pathStart, pathEnd);

        if (colon != -1) {
            name = input.substring(colon + 1);
        }

        return of(org, mod, path, name);
    }
    //endregion

    /// @throws BaizelException if module is not found or this target is not a module-type target
    public Module getModule(Project project) {
        if (!artifact().isEmpty() || entity.path.isEmpty()) {
            throw new BaizelException(BaizelErrors.MODULE_NOT_FOUND, this.toString());
        }
        var module = project.getModuleOf(project.root().resolve(entity.path));
        if (module == null) {
            throw new BaizelException(BaizelErrors.MODULE_NOT_FOUND, entity.path);
        }
        return module;
    }

    public SourceSet sourceSet() {
        return ! entity.targetName.isEmpty() ? SourceSets.get(entity.targetName) : SourceSets.main();
    }

    //region getters
    @Override
    public String organization() {
        return entity.organization;
    }

    @Override
    public String artifact() {
        return entity.artifact;
    }

    @Override
    public String path() {
        return entity.path;
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
    public TargetImpl(nl.adamg.baizel.core.entities.Target entity) {
        super(entity);
    }
    //endregion
}
