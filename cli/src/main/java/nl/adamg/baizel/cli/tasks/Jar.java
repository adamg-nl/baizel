package nl.adamg.baizel.cli.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.TargetCoordinates;
import nl.adamg.baizel.core.api.TargetCoordinates.CoordinateKind;
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
    public boolean isApplicable(TargetCoordinates target, CoordinateKind coordinateKind, Baizel baizel) {
        return coordinateKind == CoordinateKind.MODULE;
    }

    @Override
    public Set<Path> run(TargetCoordinates target, List<String> args, List<Input<TaskRequest>> inputs, CoordinateKind targetType, Baizel baizel) throws IOException {
        var module = baizel.project().getModule(target);
        var manifest = ManifestUtil.createManifest(module, target.targetType());
        var jarCreator = new JarCreator(baizel.fileSystem(), false, false);
        var inputRoots = new TreeSet<Path>();
        var targetRoot = module.getContentRoot(target.targetType());
        if (targetRoot == null) {
            return Set.of();
        }
        var outputPathSuffix = Path.of(".build/classes/java/" + target.targetType().targetId());
        for(var input : inputs) {
            for(var inputPath : input.paths()) {
                var suffixOffset = inputPath.toString().indexOf(outputPathSuffix.toString());
                if (suffixOffset > 0) {
                    inputRoots.add(Path.of(inputPath.toString().substring(0, suffixOffset + outputPathSuffix.toString().length())));
                }
            }
        }
        for(var resourceRoot : targetRoot.resources()) {
            inputRoots.add(resourceRoot.fullPath());
        }
        var jarFileName = module.artifactId() + "-" + baizel.project().version() + ".jar";
        var outputJarPath = module.buildDir().resolve("dist").resolve(jarFileName);
        jarCreator.createJar(inputRoots, manifest, outputJarPath);
        return Set.of(outputJarPath);
    }

    @Override
    public Set<TaskRequest> findDependencies(TargetCoordinates target, CoordinateKind targetType, Baizel baizel) {
        return Set.of(TaskRequestImpl.of(target, Compile.TASK_ID));
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
