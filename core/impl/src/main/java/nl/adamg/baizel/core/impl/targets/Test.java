package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.TargetType;
import nl.adamg.baizel.core.impl.Targets;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Test implements TargetType {
    public static final String TARGET_TYPE_ID = "test";

    @ServiceProvider(TargetType.class)
    public Test() {}

    @Override
    public String getTargetTypeId() {
        return TARGET_TYPE_ID;
    }

    @Override
    public String getPath() {
        return "src/test/java";
    }

    @Override
    public TargetType resourceTarget() {
        return Targets.byId(TestResources.TARGET_TYPE_ID);
    }
}
