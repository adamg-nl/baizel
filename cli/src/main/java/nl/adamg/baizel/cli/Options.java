package nl.adamg.baizel.cli;

import nl.adamg.baizel.core.Project;

import java.io.Serializable;
import java.nio.file.Path;

public final class Options implements Serializable {
    public int workerCount = Runtime.getRuntime().availableProcessors();
    public Path projectRoot = Project.findProjectRoot(Path.of("."));

    public Options() {}

    //region generated code
    public Options(int workerCount, Path projectRoot) {
        this.workerCount = workerCount;
        this.projectRoot = projectRoot;
    }
    //endregion
}
