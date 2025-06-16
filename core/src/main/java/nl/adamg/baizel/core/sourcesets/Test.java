package nl.adamg.baizel.core.sourcesets;

import nl.adamg.baizel.core.api.SourceSet;

public class Test implements SourceSet {
    @Override
    public String getSourceSetId() {
        return "test";
    }

    @Override
    public String getPath() {
        return "src/test/java";
    }
}
