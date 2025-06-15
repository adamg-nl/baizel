package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Map;

public class Issue implements Serializable {
    public String id;
    public Map<String, String> details;

    //region generated code
    public Issue(String id, Map<String, String> details) {
        this.id = id;
        this.details = details;
    }
    //endregion
}
