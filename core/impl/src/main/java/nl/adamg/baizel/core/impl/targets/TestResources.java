package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.TargetType;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class TestResources implements TargetType {
    public static final String TARGET_TYPE_ID = "test-resources";

    @ServiceProvider(TargetType.class)
    public TestResources() {}

    @Override
    public String getTargetTypeId() {
        return TARGET_TYPE_ID;
    }

    @Override
    public String getPath() {
        return "src/test/resources";
    }
}
