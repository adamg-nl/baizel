package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.TargetType;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class MainResources implements TargetType {
    public static final String TARGET_TYPE_ID = "main-resources";

    @ServiceProvider(TargetType.class)
    public MainResources() {}

    @Override
    public String getTargetTypeId() {
        return TARGET_TYPE_ID;
    }

    @Override
    public String getPath() {
        return "src/main/resources";
    }
}
