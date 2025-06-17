module nl.adamg.baizel.core.impl {
    exports nl.adamg.baizel.core.impl;
    exports nl.adamg.baizel.core.impl.sourcesets;

    uses nl.adamg.baizel.core.api.Task;
    uses nl.adamg.baizel.core.api.SourceSet;

    provides nl.adamg.baizel.core.api.SourceSet with
            nl.adamg.baizel.core.impl.sourcesets.Main,
            nl.adamg.baizel.core.impl.sourcesets.Test;

    requires nl.adamg.baizel.internal.common.annotations;
    requires nl.adamg.baizel.internal.common.javadsl;
    requires nl.adamg.baizel.internal.common.util;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.internal.common.serialization;
    requires nl.adamg.baizel.core.entities;
    requires nl.adamg.baizel.core.api;
}