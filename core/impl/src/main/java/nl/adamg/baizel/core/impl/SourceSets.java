package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.core.impl.sourcesets.Main;
import nl.adamg.baizel.core.impl.sourcesets.MainResources;
import nl.adamg.baizel.core.impl.sourcesets.Test;
import nl.adamg.baizel.core.impl.sourcesets.TestResources;
import nl.adamg.baizel.internal.common.java.Services;
import nl.adamg.baizel.internal.common.util.Lazy;

import java.util.Map;
import java.util.Set;
import nl.adamg.baizel.internal.common.util.collections.Items;

/// Provides access to source set configurations: the default ones like [#main()],
/// and to any custom ones, like this: `getByPath("src/testFixtures/java")`.
public enum SourceSets {
    ;
    private static final Lazy.NonNull.Safe<Map<String, SourceSet>> SOURCE_SETS_BY_ID = Lazy.safeNonNull(() ->
            Items.mapToMap(Services.get(SourceSet.class), SourceSet::getSourceSetId, s -> s)
    );
    private static final Lazy.NonNull.Safe<Map<String, SourceSet>> SOURCE_SETS_BY_PATH = Lazy.safeNonNull(() ->
            Items.mapToMap(SOURCE_SETS_BY_ID.get().values(), SourceSet::getPath, s -> s)
    );

    public static SourceSet byId(String sourceSetId) {
        var sourceSet = SOURCE_SETS_BY_ID.get().get(sourceSetId);
        if (sourceSet == null) {
            throw new IllegalArgumentException("source set not found: " + sourceSetId);
        }
        return sourceSet;
    }

    /// @param sourceSetPath relative to module root
    public static SourceSet byPath(String sourceSetPath) {
        var sourceSet = SOURCE_SETS_BY_PATH.get().get(sourceSetPath);
        if (sourceSet == null) {
            throw new IllegalArgumentException("source set not found: " + sourceSetPath);
        }
        return sourceSet;
    }

    public static Set<String> sourceSets() {
        return SOURCE_SETS_BY_ID.get().keySet();
    }

    @SuppressWarnings("ConfusingMainMethod")
    public static SourceSet main() {
        return byId(Main.SOURCE_SET_ID);
    }

    public static SourceSet test() {
        return byId(Test.SOURCE_SET_ID);
    }

    public static SourceSet mainResources() {
        return byId(MainResources.SOURCE_SET_ID);
    }

    public static SourceSet testResources() {
        return byId(TestResources.SOURCE_SET_ID);
    }
}
