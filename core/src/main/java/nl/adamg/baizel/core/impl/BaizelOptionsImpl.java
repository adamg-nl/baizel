package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.BaizelOptions;
import nl.adamg.baizel.core.BaizelException;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/// Format:
/// ```
/// [ --<NAME>=<VALUE> ]...
/// ```
///
/// - API:    [nl.adamg.baizel.core.api.BaizelOptions]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelOptions]
/// - Model:  [nl.adamg.baizel.core.impl.BaizelOptionsImpl]
public class BaizelOptionsImpl
        extends EntityModel<BaizelOptions, nl.adamg.baizel.core.entities.BaizelOptions, BaizelOptionsImpl>
        implements BaizelOptions {
    private static final String WORKER_COUNT = "--worker-count=";
    private static final String PROJECT_ROOT = "--project-root=";

    //region factory
    public static BaizelOptions of(
            int workerCount,
            String projectRoot
    ) {
        return new BaizelOptionsImpl(
                new nl.adamg.baizel.core.entities.BaizelOptions(
                        workerCount,
                        projectRoot
                )
        );
    }

    public static nl.adamg.baizel.core.entities.BaizelOptions parse(Queue<String> remainingArgs) {
        var options = new nl.adamg.baizel.core.entities.BaizelOptions(
                Runtime.getRuntime().availableProcessors(),
                ""
        );
        while (! remainingArgs.isEmpty() && remainingArgs.peek().startsWith("-")) {
            parseOption(options, remainingArgs.poll());
        }
        return options;
    }

    public static void parseOption(nl.adamg.baizel.core.entities.BaizelOptions options, String option) {
        if (option.startsWith(WORKER_COUNT)) {
            options.workerCount = Integer.parseInt(option.substring(WORKER_COUNT.length()));
            return;
        }
        if (option.startsWith(PROJECT_ROOT)) {
            options.projectRoot = option.substring(PROJECT_ROOT.length());
            return;
        }
        throw new BaizelException(BaizelErrors.INVALID_OPTION, option);
    }
    //endregion


    //region getters
    @Override
    public int workerCount() {
        return entity.workerCount;
    }

    @Override
    public Path projectRoot() {
        return Path.of(entity.projectRoot);
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return WORKER_COUNT + entity.workerCount + " " + PROJECT_ROOT + entity.projectRoot;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.BaizelOptions, ?>> fields() {
        return List.of(
                o -> o.workerCount,
                o -> o.projectRoot
        );
    }
    //endregion

    //region generated code
    public BaizelOptionsImpl(nl.adamg.baizel.core.entities.BaizelOptions entity) {
        super(entity);
    }
    //endregion
}
