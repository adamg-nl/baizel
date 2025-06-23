package nl.adamg.baizel.core.impl.targets;

import nl.adamg.baizel.core.api.TargetType;
import nl.adamg.baizel.core.impl.Targets;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Main implements TargetType {
    public static final String TARGET_TYPE_ID = "main";

    @ServiceProvider(TargetType.class)
    public Main() {}

    @Override
    public String getTargetTypeId() {
        return TARGET_TYPE_ID;
    }

    @Override
    public String getPath() {
        return "src/main/java";
    }

    @Override
    public TargetType resourceTarget() {
        return Targets.byId(MainResources.TARGET_TYPE_ID);
    }
}
