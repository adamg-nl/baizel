package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class TestResources implements Target {
    public static final String TARGET_ID = "test-resources";

    @ServiceProvider(Target.class)
    public TestResources() {}

    @Override
    public String targetId() {
        return TARGET_ID;
    }

    @Override
    public String contentRoot() {
        return "src/test/resources";
    }
}
