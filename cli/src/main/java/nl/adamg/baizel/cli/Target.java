package nl.adamg.baizel.cli;

import javax.annotation.CheckForNull;
import java.io.Serializable;

/// Format: `[@[<ORG>/][<MODULE>]]//<PATH>[:<TARGET_NAME>]`
/// Example: `@foo/bar//baz/qux:qux`
/// Example: `//baz/qux`
public class Target implements Serializable {
    @CheckForNull public final String organization;
    @CheckForNull public final String module;
    @CheckForNull public final String path;
    @CheckForNull public final String targetName;

    public static Target parse(String input) {
        var org = (String)null;
        var mod = (String)null;
        String path;
        var name = (String)null;

        var at = input.indexOf('@');
        var slashSlash = input.indexOf("//");
        var colon = input.indexOf(':');

        if (at != -1 && at < slashSlash) {
            var orgMod = input.substring(at + 1, slashSlash).split("/", 2);
            org = orgMod[0];
            if (orgMod.length > 1) {
                mod = orgMod[1];
            }
        }

        var pathStart = slashSlash + 2;
        var pathEnd = colon != -1 ? colon : input.length();
        path = input.substring(pathStart, pathEnd);

        if (colon != -1) {
            name = input.substring(colon + 1);
        } else if (!path.isEmpty()) {
            name = path.substring(path.lastIndexOf('/') + 1);
        }

        return new Target(org, mod, path, name);
    }

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
