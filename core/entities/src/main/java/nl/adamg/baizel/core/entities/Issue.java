package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class Issue implements Serializable {
    public String id;
    public int errorCode;
    public Map<String, String> details;
    public String messageTemplate;

    //region generated code
    public Issue(String id, int errorCode, Map<String, String> details, String messageTemplate) {
        this.id = id;
        this.errorCode = errorCode;
        this.details = details;
        this.messageTemplate = messageTemplate;
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
