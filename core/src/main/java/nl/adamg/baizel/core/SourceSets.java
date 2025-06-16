package nl.adamg.baizel.core;

import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.core.sourcesets.Main;
import nl.adamg.baizel.internal.common.util.Lazy;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;

public enum SourceSets {
    ;
    
    private static final Lazy.NonNull.Safe<Map<String, SourceSet>> SOURCE_SETS_BY_ID = Lazy.safeNonNull(() -> {
        var map = new TreeMap<String, SourceSet>();
        for(var sourceSet : ServiceLoader.load(SourceSet.class)) {
            map.put(sourceSet.getSourceSetId(), sourceSet);
        }
        return map;
    });

    public static SourceSet get(String sourceSetId) {
        var sourceSet = SOURCE_SETS_BY_ID.get().get(sourceSetId);
        if (sourceSet == null) {
            throw new IllegalArgumentException("source set not found: " + sourceSetId);
        }
        return sourceSet;
    }

    public static Set<String> getSourceSets() {
        return SOURCE_SETS_BY_ID.get().keySet();
    }

    @SuppressWarnings("ConfusingMainMethod")
    public static SourceSet main() {
        return Objects.requireNonNull(get(Main.SOURCE_SET_ID));
    }
}
