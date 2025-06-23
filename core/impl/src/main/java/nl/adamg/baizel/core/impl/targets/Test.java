package nl.adamg.baizel.core.impl.targets;

import java.util.List;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.impl.Targets;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Test implements Target {
    public static final String TARGET_ID = "test";

    @ServiceProvider(Target.class)
    public Test() {}

    @Override
    public String targetId() {
        return TARGET_ID;
    }

    @Override
    public String contentRoot() {
        return "src/test/java";
    }

    @Override
    public List<Target> resources() {
        return List.of(Targets.byId(TestResources.TARGET_ID));
    }
}
