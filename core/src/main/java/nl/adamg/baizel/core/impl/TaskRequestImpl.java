package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.util.List;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Model:  [nl.adamg.baizel.core.impl.TaskRequestImpl]
public class TaskRequestImpl
        extends EntityModel<TaskRequest, nl.adamg.baizel.core.entities.TaskRequest, TaskRequestImpl>
        implements TaskRequest {
    //region factory
    public static TaskRequest of(
            Target target,
            String task
    ) {
        return new TaskRequestImpl(
                new nl.adamg.baizel.core.entities.TaskRequest(
                        ((TargetImpl)target).entity(),
                        task
                )
        );
    }
    //endregion

    //region getters
    @Override
    public Target target() {
        return new TargetImpl(entity.target);
    }

    @Override
    public String taskId() {
        return entity.taskId;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return target() + ":" + entity.taskId;
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
    public TaskRequestImpl(nl.adamg.baizel.core.entities.TaskRequest entity) {
        super(entity);
    }
    //endregion
}
