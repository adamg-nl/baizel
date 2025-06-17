package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.TaskInput;
import nl.adamg.baizel.internal.common.util.EntityModel;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/// - API:    [nl.adamg.baizel.core.api.TaskInput]
/// - Entity: [nl.adamg.baizel.core.entities.TaskInput]
/// - Model:  [nl.adamg.baizel.core.impl.TaskInputImpl]
public class TaskInputImpl
        extends EntityModel<TaskInput, nl.adamg.baizel.core.entities.TaskInput, TaskInputImpl>
        implements TaskInput {
    //region factory
    public static TaskInputImpl of(
            Target origin,
            String originTaskId,
            Set<Path> paths
    ) {
        return new TaskInputImpl(
                new nl.adamg.baizel.core.entities.TaskInput(
                        ((TargetImpl)origin).entity(),
                        originTaskId,
                        Items.mapToList(paths, Path::toString)
                )
        );
    }
    //endregion

    //region getters
    @Override
    public Target origin() {
        return new TargetImpl(entity.origin);
    }

    @Override
    public String originTaskId() {
        return entity.originTaskId;
    }

    @Override
    public Set<Path> paths() {
        return Items.mapToSortedSet(entity.paths, Path::of);
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return origin() + "." + entity.originTaskId;
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
    public TaskInputImpl(nl.adamg.baizel.core.entities.TaskInput entity) {
        super(entity);
    }
    //endregion
}
