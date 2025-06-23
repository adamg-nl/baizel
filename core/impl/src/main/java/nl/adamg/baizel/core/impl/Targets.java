package nl.adamg.baizel.core.impl;

import java.util.Collection;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.TargetType;
import nl.adamg.baizel.core.impl.targets.Main;
import nl.adamg.baizel.core.impl.targets.MainResources;
import nl.adamg.baizel.core.impl.targets.Test;
import nl.adamg.baizel.core.impl.targets.TestResources;
import nl.adamg.baizel.internal.common.java.Services;
import nl.adamg.baizel.internal.common.util.Lazy;

import java.util.Map;
import nl.adamg.baizel.internal.common.util.collections.Items;

/// Provides access to source set configurations: the default ones like [#main()],
/// and to any custom ones, like this: `getByPath("src/testFixtures/java")`.
public final class Targets {
    private Targets(){}

    private static final Lazy.NonNull.Safe<Map<String, TargetType>> SOURCE_SETS_BY_ID = Lazy.safeNonNull(() ->
            Items.mapToMap(Services.get(TargetType.class), TargetType::getTargetTypeId, s -> s)
    );
    private static final Lazy.NonNull.Safe<Map<String, TargetType>> SOURCE_SETS_BY_PATH = Lazy.safeNonNull(() ->
            Items.mapToMap(SOURCE_SETS_BY_ID.get().values(), TargetType::getPath, s -> s)
    );

    public static TargetType byId(String sourceSetId) {
        var sourceSet = SOURCE_SETS_BY_ID.get().get(sourceSetId);
        if (sourceSet == null) {
            throw new IllegalArgumentException("source set not found: " + sourceSetId);
        }
        return sourceSet;
    }

    /// @param sourceSetPath relative to module root
    public static TargetType byPath(String sourceSetPath) {
        var sourceSet = SOURCE_SETS_BY_PATH.get().get(sourceSetPath);
        if (sourceSet == null) {
            throw new IllegalArgumentException("source set not found: " + sourceSetPath);
        }
        return sourceSet;
    }

    public static Collection<TargetType> values() {
        return SOURCE_SETS_BY_ID.get().values();
    }

    @SuppressWarnings("ConfusingMainMethod")
    public static TargetType main() {
        return byId(Main.TARGET_TYPE_ID);
    }

    public static TargetType test() {
        return byId(Test.TARGET_TYPE_ID);
    }

    public static TargetType mainResources() {
        return byId(MainResources.TARGET_TYPE_ID);
    }

    public static TargetType testResources() {
        return byId(TestResources.TARGET_TYPE_ID);
    }

    @CheckForNull
    public static TargetType getTargetType(String sourceFilePath) {
        for(var target : values()) {
            if (sourceFilePath.startsWith(target.getPath())) {
                return target;
            }
        }
        return null;
    }
}
