package nl.adamg.baizel.core.model;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Model:  [nl.adamg.baizel.core.model.TaskRequest]
public class TaskRequest
        extends EntityModel<nl.adamg.baizel.core.api.TaskRequest, nl.adamg.baizel.core.entities.TaskRequest, TaskRequest>
        implements nl.adamg.baizel.core.api.TaskRequest {
    //region factory
    public static nl.adamg.baizel.core.api.TaskRequest of(
            Target target,
            String task
    ) {
        return new TaskRequest(
                new nl.adamg.baizel.core.entities.TaskRequest(
                        ((nl.adamg.baizel.core.model.Target)target).entity(),
                        task
                )
        );
    }
    //endregion

    //region getters
    @Override
    public Target target() {
        return new nl.adamg.baizel.core.model.Target(entity.target);
    }

    @Override
    public String taskId() {
        return entity.taskId;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return entity.target + ":" + entity.taskId;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.TaskRequest, ?>> fields() {
        return List.of(
                tr -> tr.target,
                tr -> tr.taskId
        );
    }
    //endregion

    //region generated code
    public TaskRequest(nl.adamg.baizel.core.entities.TaskRequest entity) {
        super(entity);
    }
    //endregion
}
