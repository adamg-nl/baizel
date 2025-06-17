package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Task implements Serializable {
    public String type;
    public List<String> inputPaths;

    //region generated code
    public Task(String type, List<String> inputPaths) {
        this.type = type;
        this.inputPaths = inputPaths;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return Objects.equals(type, task.type) && Objects.equals(inputPaths, task.inputPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, inputPaths);
    }
    //endregion
}
