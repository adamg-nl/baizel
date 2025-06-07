package nl.adamg.baizel.cli;

import javax.annotation.CheckForNull;
import java.io.Serializable;

/// Format: `[@[<ORG>/]<MODULE>][//<PATH>][:<TARGET_NAME>]`
/// Example: `@foo/bar//baz/qux:qux`
/// Example: `//baz/qux`
/// Either path or target name is mandatory, and both can be present.
public class Target implements Serializable {
    @CheckForNull public final String organization;
    @CheckForNull public final String module;
    @CheckForNull public final String path;
    @CheckForNull public final String targetName;

    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (organization != null) {
            sb.append('@').append(organization);
            if (module != null) {
                sb.append('/').append(module);
            }
        }
        sb.append("//").append(path);
        if (targetName != null) {
            sb.append(':').append(targetName);
        }
        return sb.toString();
    }

    //region generated code
    public Target(@CheckForNull String organization, @CheckForNull String module, String path, @CheckForNull String targetName) {
        this.organization = organization;
        this.module = module;
        this.path = path;
        this.targetName = targetName;
    }
    //endregion
}
