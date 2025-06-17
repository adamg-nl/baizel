package nl.adamg.baizel.core.impl.sourcesets;

import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Test implements SourceSet {

    @ServiceProvider(SourceSet.class)
    public Test() {}

    @Override
    public String getSourceSetId() {
        return "test";
    }

    @Override
    public String getPath() {
        return "src/test/java";
    }
}
