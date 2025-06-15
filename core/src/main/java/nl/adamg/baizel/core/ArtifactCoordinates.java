package nl.adamg.baizel.core;

import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

/// Maven artifact coordinates, in format
/// ```
/// <ORGANIZATION>:<ARTIFACT>:<VERSION>
/// ```
public class ArtifactCoordinates extends EntityModel<nl.adamg.baizel.core.entities.ArtifactCoordinates, ArtifactCoordinates> {
    public static nl.adamg.baizel.core.entities.ArtifactCoordinates parse(String coordinatesString) {
        var split = coordinatesString.split(":", 3);
        return new nl.adamg.baizel.core.entities.ArtifactCoordinates(
                split.length > 0 ? split[0] : "",
                split.length > 1 ? split[1] : "",
                split.length > 2 ? split[2] : "",
                null
        );
    }

    //region getters
    public String organization() {
        return entity.organization;
    }

    public String artifact() {
        return entity.artifact;
    }

    public String version() {
        return entity.version;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return entity.organization + ":" + entity.artifact + ":" + entity.version;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.ArtifactCoordinates, ?>> fields() {
        return List.of(
                e -> e.organization,
                e -> e.artifact,
                e -> e.version
        );
    }
    //endregion

    //region generated code
    public ArtifactCoordinates(nl.adamg.baizel.core.entities.ArtifactCoordinates entity) {
        super(entity);
    }
    //endregion
}
