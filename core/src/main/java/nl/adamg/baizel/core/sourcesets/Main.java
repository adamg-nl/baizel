package nl.adamg.baizel.core.sourcesets;

import nl.adamg.baizel.core.api.SourceSet;

public class Main implements SourceSet {
    public static final String SOURCE_SET_ID = "main";

    @Override
    public String getSourceSetId() {
        return SOURCE_SET_ID;
    }

    @Override
    public String getPath() {
        return "src/main/java";
    }
}
