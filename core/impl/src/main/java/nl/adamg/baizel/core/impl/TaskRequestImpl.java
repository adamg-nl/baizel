package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.internal.common.util.EntityModel;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Impl:   [nl.adamg.baizel.core.impl.TaskRequestImpl]
public class TaskRequestImpl
        extends EntityModel<nl.adamg.baizel.core.entities.TaskRequest>
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

    @Override
    public int compareTo(TaskRequest taskRequest) {
        return toString().compareTo(taskRequest.toString());
    }

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
    //endregion

    //region generated code
    public TaskRequestImpl(nl.adamg.baizel.core.entities.TaskRequest entity) {
        super(entity);
    }
    //endregion
}
