package nl.adamg.baizel.core;

import nl.adamg.baizel.internal.common.util.collections.EntityComparator;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.function.Function;

/// Format: `[@[<ORG>/]<ARTIFACT>][//<PATH>][:<TARGET_NAME>]`
/// Example: `@foo/bar//baz/qux:main`
/// Example: `//baz/qux`
public class Target implements Comparable<Target> {
    private final nl.adamg.baizel.core.entities.Target entity;

    //region value-like type
    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (entity.organization != null) {
            sb.append('@').append(entity.organization);
            if (entity.artifact != null) {
                sb.append('/').append(entity.artifact);
            }
        }
        sb.append("//").append(entity.path);
        if (entity.targetName != null) {
            sb.append(':').append(entity.targetName);
        }
        return sb.toString();
    }

    private List<Function<Target, ?>> fields() {
        return List.of(
                Target::organization,
                Target::artifact,
                Target::path,
                Target::targetName
        );
    }

    @Override
    public int compareTo(Target that) {
        return EntityComparator.compareBy(this, that, fields());
    }

    @Override
    public int hashCode() {
        return EntityComparator.hashCode(this, fields());
    }

    @Override
    public boolean equals(Object obj) {
        return EntityComparator.equals(this, obj, fields());
    }

    //endregion

    //region getters
    @CheckForNull
    public String organization() {
        return entity.organization;
    }

    @CheckForNull
    public String artifact() {
        return entity.artifact;
    }

    @CheckForNull
    public String path() {
        return entity.path;
    }

    @CheckForNull
    public String targetName() {
        return entity.targetName;
    }
    //endregion

    //region generated code
    public Target(nl.adamg.baizel.core.entities.Target entity) {
        this.entity = entity;
    }
    //endregion
}
