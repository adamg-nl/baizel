package nl.adamg.baizel.core.sourcesets;

import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Main implements SourceSet {
    public static final String SOURCE_SET_ID = "main";

    @ServiceProvider(SourceSet.class)
    public Main() {}

    @Override
    public String getSourceSetId() {
        return SOURCE_SET_ID;
    }

    @Override
    public String getPath() {
        return "src/main/java";
    }
}
