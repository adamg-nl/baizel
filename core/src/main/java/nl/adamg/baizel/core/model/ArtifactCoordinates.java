package nl.adamg.baizel.core.model;

import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.ArtifactCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.ArtifactCoordinates]
/// - Model:  [nl.adamg.baizel.core.model.ArtifactCoordinates]
public class ArtifactCoordinates extends EntityModel<nl.adamg.baizel.core.entities.ArtifactCoordinates, ArtifactCoordinates> implements nl.adamg.baizel.core.api.ArtifactCoordinates {
    public static nl.adamg.baizel.core.entities.ArtifactCoordinates parse(String coordinatesString) {
        var split = coordinatesString.split(":", 3);
        return new nl.adamg.baizel.core.entities.ArtifactCoordinates(
                split.length > 0 ? split[0] : "",
                split.length > 1 ? split[1] : "",
                split.length > 2 ? split[2] : "",
                ""
        );
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
    public ArtifactCoordinates(nl.adamg.baizel.core.entities.ArtifactCoordinates entity) {
        super(entity);
    }
    //endregion
}
