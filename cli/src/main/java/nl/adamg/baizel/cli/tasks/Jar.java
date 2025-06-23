package nl.adamg.baizel.cli.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.TargetCoordinates;
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
    public boolean isApplicable(TargetCoordinates target, TargetCoordinates.CoordinateKind targetType, Baizel baizel) {
        return targetType == TargetCoordinates.CoordinateKind.MODULE;
    }

    @Override
    public Set<Path> run(TargetCoordinates target, List<String> args, List<Input<TaskRequest>> inputs, TargetCoordinates.CoordinateKind targetType, Baizel baizel) throws IOException {
        var module = target.getModule(baizel.project());
        var manifest = ManifestUtil.createManifest(module, target.type());
        var jarCreator = new JarCreator(baizel.fileSystem(), false, false);
        var inputRoots = new TreeSet<Path>();
        var targetRoot = module.getTarget(target.type());
        if (targetRoot == null) {
            return Set.of();
        }
        var outputPathSuffix = Path.of(".build/classes/java/" + target.type().getTargetTypeId());
        for(var input : inputs) {
            for(var inputPath : input.paths()) {
                var suffixOffset = inputPath.toString().indexOf(outputPathSuffix.toString());
                if (suffixOffset > 0) {
                    inputRoots.add(Path.of(inputPath.toString().substring(0, suffixOffset + outputPathSuffix.toString().length())));
                }
            }
        }
        var resourceTarget = targetRoot.resources();
        if (resourceTarget != null) {
            inputRoots.add(resourceTarget.fullPath());
        }
        var jarFileName = module.artifactId() + "-" + baizel.project().version() + ".jar";
        var outputJarPath = module.buildDir().resolve("dist").resolve(jarFileName);
        jarCreator.createJar(inputRoots, manifest, outputJarPath);
        return Set.of(outputJarPath);
    }

    @Override
    public Set<TaskRequest> findDependencies(TargetCoordinates target, TargetCoordinates.CoordinateKind targetType, Baizel baizel) {
        return Set.of(TaskRequestImpl.of(target, Compile.TASK_ID));
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
