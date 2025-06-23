package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class TestFixturesResources implements Target {
    public static final String TARGET_ID = "test-fixtures-resources";

    @ServiceProvider(Target.class)
    public TestFixturesResources() {}

    @Override
    public String targetId() {
        return TARGET_ID;
    }

    @Override
    public String contentRoot() {
        return "src/testFixtures/resources";
    }
}
