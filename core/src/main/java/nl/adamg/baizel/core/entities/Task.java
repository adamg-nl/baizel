package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {
    public String type;
    public List<String> inputPaths;

    //region generated code
    public Task(String type, List<String> inputPaths) {
        this.type = type;
        this.inputPaths = inputPaths;
    }
    //endregion
}
