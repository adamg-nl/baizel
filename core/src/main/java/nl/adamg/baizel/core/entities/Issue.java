package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class Issue implements Serializable {
    public String id;
    public Map<String, String> details;

    //region generated code
    public Issue(String id, Map<String, String> details) {
        this.id = id;
        this.details = details;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Issue issue = (Issue) object;
        return Objects.equals(id, issue.id) && Objects.equals(details, issue.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, details);
    }
    //endregion
}
