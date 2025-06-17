package nl.adamg.baizel.cli;

import nl.adamg.baizel.core.api.BaizelArguments;
import nl.adamg.baizel.core.BaizelException;
import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.core.impl.BaizelArgumentsImpl;
import nl.adamg.baizel.core.impl.BaizelImpl;
import nl.adamg.baizel.core.impl.ProjectImpl;
import nl.adamg.baizel.internal.bootstrap.Bootstrap;
import nl.adamg.baizel.internal.common.io.LocalFileSystem;
import nl.adamg.baizel.internal.common.io.SystemShell;
import nl.adamg.baizel.internal.common.util.LoggerUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Logger;

/// CLI entry point to the Baizel build system for Java™
///
/// - API:    [nl.adamg.baizel.core.api.Baizel]
/// - Impl:   [nl.adamg.baizel.core.impl.BaizelImpl]
/// - CLI:    [nl.adamg.baizel.cli.Baizel]
public class Baizel {
    private static final Logger LOG = Logger.getLogger(Baizel.class.getName());

    public static void main(String... rawArgs) throws Exception {
        LOG.info("main() started" + LoggerUtil.with("rawArgs", String.join(", ", rawArgs)));
        if (rawArgs.length == 1 && "--help".equals(rawArgs[0])) {
            System.err.println(Files.readString(Bootstrap.findBaizelDir().resolve("README")));
            return;
        }
        var args = BaizelArgumentsImpl.parse(rawArgs);
        var projectRoot = getProjectRoot(args);
        var reporter = (Consumer<Issue>) i -> LOG.warning(i.id + LoggerUtil.with(i.details));
        var fileSystem = new LocalFileSystem();
        try(var shell = SystemShell.load(projectRoot);
            var baizel = BaizelImpl.start(args.options(), projectRoot, shell, fileSystem, reporter)) {
            baizel.run(args.invocation());
        } catch (BaizelException e) {
            throw e.exit();
        }
        LOG.info("main() finished");
    }

    private static Path getProjectRoot(BaizelArguments args) {
        var projectRoot = args.options().projectRoot();
        if (projectRoot.toString().isEmpty()) {
            return ProjectImpl.findProjectRoot(Path.of("."));
        }
        return projectRoot;
    }
}
