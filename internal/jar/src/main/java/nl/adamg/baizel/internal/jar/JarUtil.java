package nl.adamg.baizel.internal.jar;

import java.util.logging.Logger;
import net.java.truevfs.access.TArchiveDetector;
import net.java.truevfs.access.TConfig;
import net.java.truevfs.access.TFile;
import net.java.truevfs.access.TVFS;
import net.java.truevfs.comp.zipdriver.JarDriver;
import net.java.truevfs.kernel.spec.FsAccessOption;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class JarUtil {
    private static final Logger LOG = Logger.getLogger(JarUtil.class.getName());

    /** Updates the jar in-place without rewriting */
    public static void update(Path jarPath, Map<String, Path> replacements) throws IOException {
        TFile jarFile = null;
        try(var config = TConfig.open()) {
            config.setAccessPreference(FsAccessOption.APPEND, false);
            config.setAccessPreference(FsAccessOption.CREATE_PARENTS, true);
            config.setAccessPreference(FsAccessOption.GROW, false);
            var archiveDetector = new TArchiveDetector("jar", new JarDriver());
            jarFile = new TFile(jarPath.toString(), archiveDetector);
            for (var replacement : replacements.entrySet()) {
                var source = replacement.getValue();
                var relativePath = replacement.getKey();
                var target = new TFile(jarFile, relativePath, archiveDetector);
                if (source == null ) {
                    target.rm();
                    continue;
                }
                TFile.cp(source.toFile(), target);
            }
            TVFS.sync(jarFile);
        } finally {
            if (jarFile != null) {
                TVFS.umount(jarFile);
            }
        }
    }

}
