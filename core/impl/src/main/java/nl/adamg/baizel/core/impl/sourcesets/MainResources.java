package nl.adamg.baizel.core.impl.sourcesets;

import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class MainResources implements SourceSet {
    public static final String SOURCE_SET_ID = "main-resources";

    @ServiceProvider(SourceSet.class)
    public MainResources() {}

    @Override
    public String getSourceSetId() {
        return SOURCE_SET_ID;
    }

    @Override
    public String getPath() {
        return "src/main/resources";
    }
}
