module nl.adamg.baizel.core {
    exports nl.adamg.baizel.core;
    exports nl.adamg.baizel.core.entities;
    exports nl.adamg.baizel.core.tasks;

    uses nl.adamg.baizel.core.tasks.Task;

    requires nl.adamg.baizel.internal.common.annotations;
    requires nl.adamg.baizel.internal.common.javadsl;
    requires nl.adamg.baizel.internal.common.util;
}