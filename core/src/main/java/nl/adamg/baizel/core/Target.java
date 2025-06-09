package nl.adamg.baizel.core;

import nl.adamg.baizel.internal.common.util.collections.EntityComparator;

import javax.annotation.CheckForNull;

/// Format: `[@[<ORG>/]<MODULE>][//<PATH>][:<TARGET_NAME>]`
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
            if (entity.module != null) {
                sb.append('/').append(entity.module);
            }
        }
        sb.append("//").append(entity.path);
        if (entity.targetName != null) {
            sb.append(':').append(entity.targetName);
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Target that) {
        return EntityComparator.compareBy(
                this, that,
                Target::organization,
                Target::module,
                Target::path,
                Target::targetName
        );
    }
    //endregion

    //region getters
    @CheckForNull
    public String organization() {
        return entity.organization;
    }

    @CheckForNull
    public String module() {
        return entity.module;
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
