package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;

import java.io.IOException;
import java.util.function.Consumer;

/// - API:    [nl.adamg.baizel.core.api.Baizel]
/// - Model:  [nl.adamg.baizel.core.model.Baizel]
/// - CLI:    `nl.adamg.baizel.cli.Baizel`
public interface Baizel {
    void run(Invocation invocation) throws IOException, InterruptedException;
    void report(String issueId, String... details);
    Consumer<Issue> reporter();
    FileSystem fileSystem();
    Shell shell();
    Project project();
    BaizelOptions options();
    Target.Type getTargetType(Target target);
}
