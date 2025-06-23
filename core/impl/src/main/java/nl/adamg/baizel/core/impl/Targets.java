package nl.adamg.baizel.core.impl;

import java.util.Collection;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.impl.targets.Main;
import nl.adamg.baizel.core.impl.targets.MainResources;
import nl.adamg.baizel.core.impl.targets.Test;
import nl.adamg.baizel.core.impl.targets.TestFixtures;
import nl.adamg.baizel.core.impl.targets.TestFixturesResources;
import nl.adamg.baizel.core.impl.targets.TestResources;
import nl.adamg.baizel.internal.common.java.Services;
import nl.adamg.baizel.internal.common.util.Lazy;

import java.util.Map;
import nl.adamg.baizel.internal.common.util.collections.Items;

/// Provides access to source set configurations: the default ones like [#main()],
/// and to any custom ones, like this: `getByPath("src/testFixtures/java")`.
public final class Targets {
    private Targets(){}

    private static final Lazy.NonNull.Safe<Map<String, Target>> TARGETS_BY_ID = Lazy.safeNonNull(() ->
            Items.mapToMap(Services.get(Target.class), Target::targetId, s -> s)
    );
    private static final Lazy.NonNull.Safe<Map<String, Target>> TARGETS_BY_CONTENT_ROOT = Lazy.safeNonNull(() ->
            Items.mapToMap(TARGETS_BY_ID.get().values(), Target::contentRoot, s -> s)
    );

    public static Target byId(String targetId) {
        var target = TARGETS_BY_ID.get().get(targetId);
        if (target == null) {
            throw new IllegalArgumentException("source set not found: " + targetId);
        }
        return target;
    }

    /// @param contentRoot relative to module root
    public static Target byContentRoot(String contentRoot) {
        var target = TARGETS_BY_CONTENT_ROOT.get().get(contentRoot);
        if (target == null) {
            throw new IllegalArgumentException("source set not found: " + contentRoot);
        }
        return target;
    }

    public static Collection<Target> values() {
        return TARGETS_BY_ID.get().values();
    }

    @SuppressWarnings("ConfusingMainMethod")
    public static Target main() {
        return byId(Main.TARGET_ID);
    }

    public static Target mainResources() {
        return byId(MainResources.TARGET_ID);
    }

    public static Target test() {
        return byId(Test.TARGET_ID);
    }

    public static Target testResources() {
        return byId(TestResources.TARGET_ID);
    }

    public static Target testFixtures() {
        return byId(TestFixtures.TARGET_ID);
    }

    public static Target testFixturesResources() {
        return byId(TestFixturesResources.TARGET_ID);
    }

    @CheckForNull
    public static Target getTargetType(String sourceFilePath) {
        for(var target : values()) {
            if (sourceFilePath.startsWith(target.contentRoot())) {
                return target;
            }
        }
        return null;
    }
}
