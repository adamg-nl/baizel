package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.TargetType;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class TestFixturesResources implements TargetType {
    public static final String TARGET_TYPE_ID = "test-fixtures-resources";

    @ServiceProvider(TargetType.class)
    public TestFixturesResources() {}

    @Override
    public String getTargetTypeId() {
        return TARGET_TYPE_ID;
    }

    @Override
    public String getPath() {
        return "src/testFixtures/resources";
    }
}
