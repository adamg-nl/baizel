package nl.adamg.baizel.cli;

import java.io.Serializable;

public final class Options implements Serializable {
    public int workerCount = Runtime.getRuntime().availableProcessors();

    public Options() {}

    //region generated code
    public Options(int workerCount) {
        this.workerCount = workerCount;
    }
    //endregion
}
