package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;
import java.util.function.Consumer;

public interface Baizel {
    void report(String issueId, String... details);
    Consumer<Issue> reporter();
    Project project();
    FileSystem fileSystem();
    Shell shell();
}
