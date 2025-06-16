package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.core.BaizelException;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskInput;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;
import nl.adamg.baizel.internal.maven.MavenClient;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class Resolve implements Task {
    public static final String TASK_ID = "resolve";
    /// key: list of repositories; value: repository client
    private static final Map<List<String>, MavenClient> clientCache = new ConcurrentHashMap<>();

    @ServiceProvider(Task.class)
    public Resolve() {
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }

    @Override
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Target.Type targetType, Baizel baizel) {
        var client = clientCache.computeIfAbsent(baizel.project().artifactRepositories(), MavenClient::load);
        var coordinates = baizel.project().getArtifactCoordinates(target.artifact());
        if (coordinates == null) {
            throw new BaizelException(BaizelErrors.ARTIFACT_NOT_FOUND, target.artifact());
        }
        var output = new TreeSet<Path>();
        var path = client.resolveCoords(coordinates.toString());
        if (path == null) {
            throw new BaizelException(BaizelErrors.ARTIFACT_NOT_FOUND, coordinates.toString());
        }
        output.add(path);
        return output;
    }

    @Override
    public boolean isApplicable(Target target, Target.Type targetType, Baizel baizel) {
        return targetType == Target.Type.ARTIFACT;
    }
}
