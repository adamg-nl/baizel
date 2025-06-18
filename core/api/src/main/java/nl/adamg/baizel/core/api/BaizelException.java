package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.entities.Issue;

public class BaizelException extends RuntimeException {
    private final Issue issue;

    //region generated code
    public BaizelException(Issue issue) {
        super(issue.id + ": " + issue.messageTemplate + "\n" + issue.details);
        this.issue = issue;
    }

    public Issue issue() {
        return issue;
    }
    //endregion
}
