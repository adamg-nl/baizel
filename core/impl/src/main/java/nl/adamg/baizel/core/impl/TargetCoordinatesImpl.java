package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.TargetCoordinates;
import nl.adamg.baizel.internal.common.util.EntityModel;

/// - API:    [nl.adamg.baizel.core.api.TargetCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.TargetCoordinates]
/// - Impl:   [nl.adamg.baizel.core.impl.TargetCoordinatesImpl]
public class TargetCoordinatesImpl
        extends EntityModel<nl.adamg.baizel.core.entities.TargetCoordinates>
        implements TargetCoordinates {
    //region factory
    public static TargetCoordinates of(
            String org, 
            String artifact, 
            String path, 
            String name
    ) {
        return new TargetCoordinatesImpl(
                new nl.adamg.baizel.core.entities.TargetCoordinates(
                        org,
                        artifact,
                        path,
                        name
                )
        );
    }

    public static TargetCoordinates module(String path) {
        return of("", "", path, "");
    }

    /// @param artifact qualified Java module id (that will be mapped to Maven coordinates)
    public static TargetCoordinates artifact(String organization, String artifact) {
        return of(organization, artifact, "", "");
    }

    public static TargetCoordinates parseTarget(String input) {
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

    public Target targetType() {
        return ! entity.targetName.isEmpty() ? Targets.byId(entity.targetName) : Targets.main();
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
    //endregion

    //region generated code
    public TargetCoordinatesImpl(nl.adamg.baizel.core.entities.TargetCoordinates entity) {
        super(entity);
    }
    //endregion
}
