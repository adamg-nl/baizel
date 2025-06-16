package nl.adamg.baizel.core.model;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.TaskInput]
/// - Entity: [nl.adamg.baizel.core.entities.TaskInput]
/// - Model:  [nl.adamg.baizel.core.model.TaskInput]
public class TaskInput extends EntityModel<nl.adamg.baizel.core.entities.TaskInput, TaskInput> implements nl.adamg.baizel.core.api.TaskInput {
    //region factory
    public static TaskInput of(
            nl.adamg.baizel.core.entities.Target origin,
            String originTaskId,
            Set<Path> paths
    ) {
        return new TaskInput(
                new nl.adamg.baizel.core.entities.TaskInput(
                        origin,
                        originTaskId,
                        paths
                )
        );
    }
    //endregion

    //region getters
    @Override
    public Target origin() {
        return new nl.adamg.baizel.core.model.Target(entity.origin);
    }

    @Override
    public String originTaskId() {
        return entity.originTaskId;
    }

    @Override
    public Set<Path> paths() {
        return entity.paths;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return entity.origin + "." + entity.originTaskId;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.TaskInput, ?>> fields() {
        return List.of(
                ti -> ti.origin,
                ti -> ti.originTaskId,
                ti -> ti.paths
        );
    }
    //endregion

    //region generated code
    public TaskInput(nl.adamg.baizel.core.entities.TaskInput entity) {
        super(entity);
    }
    //endregion
}
