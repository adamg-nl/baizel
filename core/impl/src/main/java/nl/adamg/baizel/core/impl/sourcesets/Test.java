package nl.adamg.baizel.core.impl.sourcesets;

import nl.adamg.baizel.core.api.SourceSet;
import nl.adamg.baizel.core.impl.SourceSets;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Test implements SourceSet {
    public static final String SOURCE_SET_ID = "test";

    @ServiceProvider(SourceSet.class)
    public Test() {}

    @Override
    public String getSourceSetId() {
        return SOURCE_SET_ID;
    }

    @Override
    public String getPath() {
        return "src/test/java";
    }

    @Override
    public SourceSet resourceSet() {
        return SourceSets.byId(TestResources.SOURCE_SET_ID);
    }
}
