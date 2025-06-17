package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.BaizelOptions;
import nl.adamg.baizel.core.api.BaizelException;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.nio.file.Path;
import java.util.Queue;

/// Format:
/// ```
/// [ --<NAME>=<VALUE> ]...
/// ```
///
/// - API:    [nl.adamg.baizel.core.api.BaizelOptions]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelOptions]
/// - Impl:   [nl.adamg.baizel.core.impl.BaizelOptionsImpl]
public class BaizelOptionsImpl
        extends EntityModel<nl.adamg.baizel.core.entities.BaizelOptions>
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

    public static BaizelOptions parse(Queue<String> remainingArgs) {
        var options = new nl.adamg.baizel.core.entities.BaizelOptions(
                Runtime.getRuntime().availableProcessors(),
                ""
        );
        while (! remainingArgs.isEmpty() && remainingArgs.peek().startsWith("-")) {
            parseOption(options, remainingArgs.poll());
        }
        return new BaizelOptionsImpl(options);
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
        throw Issue.critical(BaizelErrors.INVALID_OPTION, "option", option);
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
    //endregion

    //region generated code
    public BaizelOptionsImpl(nl.adamg.baizel.core.entities.BaizelOptions entity) {
        super(entity);
    }
    //endregion
}
