package nl.adamg.baizel.core.impl.targets;

import java.util.List;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.impl.Targets;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class TestFixtures implements Target {
    public static final String TARGET_ID = "test-fixtures";

    @ServiceProvider(Target.class)
    public TestFixtures() {}

    @Override
    public String targetId() {
        return TARGET_ID;
    }

    @Override
    public String contentRoot() {
        return "src/testFixtures/java";
    }

    @Override
    public List<Target> resources() {
        return List.of(Targets.byId(TestFixturesResources.TARGET_ID));
    }
}
