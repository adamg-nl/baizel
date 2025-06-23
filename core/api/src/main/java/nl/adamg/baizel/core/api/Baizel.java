package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;

import java.io.IOException;
import java.util.function.Consumer;

/// - API:    [nl.adamg.baizel.core.api.Baizel]
/// - Impl:   [nl.adamg.baizel.core.impl.BaizelImpl]
/// - CLI:    [nl.adamg.baizel.cli.Baizel]
@SuppressWarnings("JavadocReference")
public interface Baizel extends AutoCloseable {
    void run(Invocation invocation) throws IOException, InterruptedException;
    void report(String issueId, String messageTemplate, String... details);
    Consumer<Issue> reporter();
    FileSystem fileSystem();
    Shell shell();
    Project project();
    BaizelOptions options();
    TargetCoordinates.CoordinateKind getTargetType(TargetCoordinates target);
    @Override
    void close() throws IOException, InterruptedException;
}
