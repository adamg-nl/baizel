package nl.adamg.baizel.core.impl;

import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.SemanticVersion;
import nl.adamg.baizel.internal.common.util.EntityModel;

public final class SemanticVersionImpl extends EntityModel<nl.adamg.baizel.core.entities.SemanticVersion> implements SemanticVersion {
    private static final String NUMERIC_IDENTIFIER = "0|[1-9]\\d*";
    private static final String IDENTIFIER = "[0-9A-Za-z-]+";
    private static final String DOT_SEPARATED_IDENTIFIERS = IDENTIFIER + "(\\." + IDENTIFIER + ")*";
    private static final String MAJOR = "(?<major>" + NUMERIC_IDENTIFIER + ")";
    private static final String MINOR = "(?<minor>" + NUMERIC_IDENTIFIER + ")";
    private static final String PATCH = "(?<patch>" + NUMERIC_IDENTIFIER + ")";
    private static final String PRERELEASE = "(?<prerelease>" + DOT_SEPARATED_IDENTIFIERS + ")";
    private static final String BUILD = "(?<build>" + DOT_SEPARATED_IDENTIFIERS + ")";
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^"
            + MAJOR + "\\."
            + MINOR + "\\."
            + PATCH
            + "(-" + PRERELEASE + ")?"
            + "(\\+" + BUILD + ")?"
            + "$");

    //region factory
    public static SemanticVersion of(
            int majorVersion,
            int minorVersion,
            int patchVersion,
            String prereleasePart,
            String buildPart
    ) {
        return new SemanticVersionImpl(
                new nl.adamg.baizel.core.entities.SemanticVersion(
                        majorVersion,
                        minorVersion,
                        patchVersion,
                        prereleasePart,
                        buildPart
                )
        );
    }

    public static SemanticVersion of(
            int majorVersion,
            int minorVersion,
            int patchVersion
    ) {
        return of(
                majorVersion,
                minorVersion,
                patchVersion,
                "",
                ""
        );
    }

    /// @return `null` if input is not a valid semver string
    @CheckForNull
    public static SemanticVersion parse(String input) {
        var matcher = SEMVER_PATTERN.matcher(input);
        if (! matcher.matches()) {
            return null;
        }
        try {
            return of(
                    Integer.parseInt(matcher.group("major")),
                    Integer.parseInt(matcher.group("minor")),
                    Integer.parseInt(matcher.group("patch")),
                    Objects.requireNonNullElse(matcher.group("prerelease"), ""),
                    Objects.requireNonNullElse(matcher.group("build"), "")
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
    //endregion

    @Override
    public String toString() {
        String versionString = major() + "." + minor() + "." + patch();
        if (! prerelease().isEmpty()) {
            versionString += "-" + prerelease();
        }
        if (! build().isEmpty()) {
            versionString += "+" + build();
        }
        return versionString;
    }

    //region getters
    @Override
    public int major() {
        return entity.major;
    }

    @Override
    public int minor() {
        return entity.minor;
    }

    @Override
    public int patch() {
        return entity.patch;
    }

    @Override
    public String prerelease() {
        return entity.prerelease;
    }

    @Override
    public String build() {
        return entity.build;
    }
    //endregion

    //region generated code
    public SemanticVersionImpl(nl.adamg.baizel.core.entities.SemanticVersion entity) {
        super(entity);
    }
    //endregion
}
