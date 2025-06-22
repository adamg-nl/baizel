package nl.adamg.baizel.cli.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.api.TaskScheduler.Input;
import nl.adamg.baizel.core.impl.TaskRequestImpl;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;
import nl.adamg.baizel.internal.jar.JarCreator;
import nl.adamg.baizel.internal.jar.ManifestUtil;

public class Jar implements Task {
    public static final String TASK_ID = "jar";

    @ServiceProvider(Task.class)
    public Jar() {}

    @Override
    public boolean isApplicable(Target target, Target.Type targetType, Baizel baizel) {
        return targetType == Target.Type.MODULE;
    }

    @Override
    public Set<Path> run(Target target, List<String> args, List<Input<TaskRequest>> inputs, Target.Type targetType, Baizel baizel) throws IOException {
        var module = target.getModule(baizel.project());
        var manifest = ManifestUtil.createManifest(module, target.sourceSet());
        var jarCreator = new JarCreator(baizel.fileSystem(), false, false);
        var inputRoots = new ArrayList<Path>();
        inputRoots.add(module.sourceRoot(target.sourceSet()));
        var resourceSet = target.sourceSet().resourceSet();
        if (resourceSet != null) {
            inputRoots.add(module.sourceRoot(resourceSet));
        }
        var jarFileName = module.artifactId() + "-" + baizel.project().version();
        var outputJarPath = module.buildDir().resolve("dist").resolve(jarFileName);
        jarCreator.createJar(inputRoots, manifest, outputJarPath);
        return Set.of(outputJarPath);
    }

    @Override
    public Set<TaskRequest> findDependencies(Target target, Target.Type targetType, Baizel baizel) {
        return Set.of(TaskRequestImpl.of(target, Compile.TASK_ID));
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
