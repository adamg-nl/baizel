package nl.adamg.baizel.core.impl;

import java.io.IOException;
import nl.adamg.baizel.core.api.Project;
import nl.adamg.baizel.core.api.SemanticVersion;
import nl.adamg.baizel.core.api.VersionTracker;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;
import nl.adamg.baizel.internal.common.io.ShellConfig;
import nl.adamg.baizel.internal.common.util.Text;

public class GitVersionTracker implements VersionTracker {
    @ServiceProvider(VersionTracker.class)
    public GitVersionTracker() {}

    @Override
    public SemanticVersion getVersion(Project project, Shell shell, FileSystem fileSystem) throws IOException {
        var shellConfig = new ShellConfig();
        shellConfig.pwd = project.root();
        var findLastTagCommand = "git describe --tags --abbrev=0";
        var gitTag = shell.exec(findLastTagCommand, shellConfig).stdOut();
        String countCommitsCommand;
        if (! gitTag.isEmpty()) {
            if (gitTag.matches("^[vV].*")) {
                gitTag = gitTag.substring(1);
            }
            countCommitsCommand = "git rev-list '" + Text.filter(gitTag, "a-zA-Z0-9_-") + "..HEAD' --count";
        } else {
            countCommitsCommand = "git rev-list --count HEAD";
        }
        var semver = SemanticVersionImpl.parse(gitTag);
        if (semver == null) {
            semver = SemanticVersionImpl.of(0, 0, 0);
        }
        var patchCommitCount = Integer.parseInt(shell.exec(countCommitsCommand, shellConfig).assertSuccess().stdErr());
        return SemanticVersionImpl.of(
                semver.major(),
                semver.minor(),
                semver.patch() + patchCommitCount
        );
    }
}
