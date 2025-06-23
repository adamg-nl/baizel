package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class MainResources implements Target {
    public static final String TARGET_ID = "main-resources";

    @ServiceProvider(Target.class)
    public MainResources() {}

    @Override
    public String targetId() {
        return TARGET_ID;
    }

    @Override
    public String contentRoot() {
        return "src/main/resources";
    }
}
