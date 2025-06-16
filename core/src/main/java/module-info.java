module nl.adamg.baizel.core {
    exports nl.adamg.baizel.core;
    exports nl.adamg.baizel.core.entities;
    exports nl.adamg.baizel.core.api;
    exports nl.adamg.baizel.core.model;

    uses nl.adamg.baizel.core.api.Task;

    provides nl.adamg.baizel.core.api.SourceSet with
            nl.adamg.baizel.core.sourcesets.Main,
            nl.adamg.baizel.core.sourcesets.Test;

    requires nl.adamg.baizel.internal.common.annotations;
    requires nl.adamg.baizel.internal.common.javadsl;
    requires nl.adamg.baizel.internal.common.util;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.internal.common.serialization;
}