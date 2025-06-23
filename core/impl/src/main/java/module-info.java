module nl.adamg.baizel.core.impl {
    exports nl.adamg.baizel.core.impl;
    exports nl.adamg.baizel.core.impl.targets;

    uses nl.adamg.baizel.core.api.Task;
    uses nl.adamg.baizel.core.api.TargetType;

    provides nl.adamg.baizel.core.api.TargetType with
            nl.adamg.baizel.core.impl.targets.Main,
            nl.adamg.baizel.core.impl.targets.Test,
            nl.adamg.baizel.core.impl.targets.TestFixtures,
            nl.adamg.baizel.core.impl.targets.MainResources,
            nl.adamg.baizel.core.impl.targets.TestResources,
            nl.adamg.baizel.core.impl.targets.TestFixturesResources;

    provides nl.adamg.baizel.core.api.VersionTracker with
            nl.adamg.baizel.core.impl.GitVersionTracker;

    requires nl.adamg.baizel.internal.common.annotations;
    requires nl.adamg.baizel.internal.common.javadsl;
    requires nl.adamg.baizel.internal.common.util;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.internal.common.serialization;
    requires nl.adamg.baizel.core.entities;
    requires nl.adamg.baizel.core.api;
    requires nl.adamg.baizel.internal.common.java;
}