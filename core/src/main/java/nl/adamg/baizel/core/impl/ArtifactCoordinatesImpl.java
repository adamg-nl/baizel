package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.internal.common.util.EntityModel;
import nl.adamg.baizel.core.api.ArtifactCoordinates;

import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.ArtifactCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.ArtifactCoordinates]
/// - Model:  [nl.adamg.baizel.core.impl.ArtifactCoordinatesImpl]
public class ArtifactCoordinatesImpl
        extends EntityModel<ArtifactCoordinates, nl.adamg.baizel.core.entities.ArtifactCoordinates, ArtifactCoordinatesImpl>
        implements ArtifactCoordinates {
    //region factory
    public static ArtifactCoordinates of(
            String organization,
            String artifact,
            String version,
            String moduleId
    ) {
        return new ArtifactCoordinatesImpl(
                new nl.adamg.baizel.core.entities.ArtifactCoordinates(
                        organization,
                        artifact,
                        version,
                        moduleId
                )
        );
    }

    public static ArtifactCoordinates parse(String coordinatesString) {
        var split = coordinatesString.split(":", 3);
        return of(
                split.length > 0 ? split[0] : "",
                split.length > 1 ? split[1] : "",
                split.length > 2 ? split[2] : "",
                ""
        );
    }
    //endregion

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
    public String version() {
        return entity.version;
    }

    @Override
    public String moduleId() {
        return entity.moduleId;
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
                e -> e.version,
                e -> e.moduleId
        );
    }
    //endregion

    //region generated code
    public ArtifactCoordinatesImpl(nl.adamg.baizel.core.entities.ArtifactCoordinates entity) {
        super(entity);
    }
    //endregion
}
