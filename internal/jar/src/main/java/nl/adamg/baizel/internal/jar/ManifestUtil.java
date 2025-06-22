package nl.adamg.baizel.internal.jar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.core.api.SourceSet;

@SuppressWarnings("JavadocLinkAsPlainText")
public class ManifestUtil {
    /// Examples: `com.amazon.ion`, `com.google.guice`, `org.mockito`
    public static final Attributes.Name AUTOMATIC_MODULE_NAME = new Attributes.Name("Automatic-Module-Name");
    /// Examples: `2021-09-22`, `2025-04-11`
    public static final Attributes.Name BUILD_DATE = new Attributes.Name("Build-Date");
    /// Examples: `0a446598f2b90bb0f52f1945f08755b430ec474b`, `163b25a85093f094606c40864835f0fb60404133`
    public static final Attributes.Name BUILD_REVISION = new Attributes.Name("Build-Revision");
    /// Examples: `11`, `17`, `21`
    public static final Attributes.Name BUILD_JDK_SPEC = new Attributes.Name("Build-Jdk-Spec");
    /// Examples: `A high performance caching library`
    public static final Attributes.Name BUNDLE_DESCRIPTION = new Attributes.Name("Bundle-Description");
    /// Examples: `https://github.com/google/guava/`, `https://netty.io/`, `http://x-stream.github.io`
    public static final Attributes.Name BUNDLE_DOC_URL = new Attributes.Name("Bundle-DocURL");
    /// Examples: `"Apache-2.0";link="https://www.apache.org/licenses/LICENSE-2.0.txt"`, `Apache-2.0`, `https://jsoup.org/license`
    public static final Attributes.Name BUNDLE_LICENSE = new Attributes.Name("Bundle-License");
    /// Always "2"
    public static final Attributes.Name BUNDLE_MANIFEST_VERSION = new Attributes.Name("Bundle-ManifestVersion");
    /// Either
    /// Examples: `Apache Commons Logging`, `com.amazon.ion:ion-java`, `com.squareup.okhttp3`, `ec4j-core`, `jcl-over-slf4j`, `oshi-core`, `spock-core`, `zstd-jni`, `guice`
    public static final Attributes.Name BUNDLE_NAME = new Attributes.Name("Bundle-Name");
    /// Includes sub-fields:
    /// - `url`: web repository browser url (example: `https://github.com/google/gson/`)
    /// - `connection`: public read-only git url, usually on `scm:git:https://` (example: `scm:git:https://github.com/assertj/assertj.git/assertj-parent/assertj-core`)
    /// - `developer-connection`: restricted pushable git url, usually on `scm:git:ssh://` (example: `scm:git:ssh://git@github.com:google/gson.git`)
    /// - `tag` (examples: `maven-resolver-1.9.20`, `assertj-build-3.27.3`, `gson-parent-2.13.1`, `oshi-parent-6.6.0`)
    public static final Attributes.Name BUNDLE_SCM = new Attributes.Name("Bundle-SCM");
    /// Examples:
    /// - `com.amazon.ion.java`
    /// - `com.google.gson`
    /// - `com.sun.jna`
    /// - `io.github.classgraph.classgraph`
    /// - `org.eclipse.jgit.ssh.apache`
    /// - `org.apache.maven.resolver.api`
    public static final Attributes.Name BUNDLE_SYMBOLIC_NAME = new Attributes.Name("Bundle-SymbolicName");
    public static final Attributes.Name BUNDLE_VENDOR = new Attributes.Name("Bundle-Vendor");
    public static final Attributes.Name BUNDLE_VERSION = new Attributes.Name("Bundle-Version");
    public static final Attributes.Name CREATED_BY = new Attributes.Name("Created-By");
    private static final DateTimeFormatter BUILD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Manifest createManifest(Module module, SourceSet sourceSet) throws IOException {
        var explicitManifestFile = getExplicitManifestFile(module, sourceSet);
        var version = module.project().version();
        var vendor = module.project().groupId();
        if (vendor.isEmpty()) {
            vendor = module.project().projectId();
        }
        Manifest manifest;
        if (explicitManifestFile != null) {
            manifest = loadManifest(explicitManifestFile);
        } else {
            manifest = new Manifest();
        }
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(BUNDLE_MANIFEST_VERSION, "2");
        var mainClass = module.mainClass();
        if (mainClass != null) {
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass.canonicalName());
        }
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, module.artifactId());
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, version);
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, vendor);
        manifest.getMainAttributes().put(AUTOMATIC_MODULE_NAME, module.moduleId());
        manifest.getMainAttributes().put(BUILD_DATE, LocalDate.now().format(BUILD_DATE_FORMAT));
        if (! version.build().isEmpty()) {
            manifest.getMainAttributes().put(BUILD_REVISION, version.build());
        }
        manifest.getMainAttributes().put(BUILD_JDK_SPEC, Runtime.version().feature());
        manifest.getMainAttributes().put(CREATED_BY, getJvmInfo());
        var description = module.shortDescription();
        if (! description.isEmpty()) {
            manifest.getMainAttributes().put(BUNDLE_DESCRIPTION, description);
        }
        var website = module.project().metadata().get("www").string();
        if (! website.isEmpty()) {
            manifest.getMainAttributes().put(BUNDLE_DOC_URL, website);
        }
        var license = module.project().metadata().get("license").string();
        if (! license.isEmpty()) {
            manifest.getMainAttributes().put(BUNDLE_LICENSE, license);
        }
        var title = module.title();
        if (title.isEmpty()) {
            title = module.artifactId();
        }
        manifest.getMainAttributes().put(BUNDLE_NAME, title);
        var gitUrl = module.project().metadata().get("git").string();
        if (! gitUrl.isEmpty()) {
            var tag = version.major() + "." + version.minor() + "." + version.patch();
            manifest.getMainAttributes().put(BUNDLE_SCM, "connection=\"scm:git:" + gitUrl + "\",tag=\"" + tag + "\"");
        }
        manifest.getMainAttributes().put(BUNDLE_SYMBOLIC_NAME, module.moduleId());
        manifest.getMainAttributes().put(BUNDLE_VENDOR, vendor);
        manifest.getMainAttributes().put(BUNDLE_VERSION, version.toString());

        return manifest;
    }

    private static String getJvmInfo() {
        var version = System.getProperty("java.version");
        var runtimeVersion = System.getProperty("java.runtime.version");
        var vendor = System.getProperty("java.vendor");

        // Some JDKs (like Eclipse Adoptium) set runtime version differently
        if (runtimeVersion != null && !runtimeVersion.equals(version)) {
            version = runtimeVersion;
        }

        return version + " (" + vendor + ")";
    }

    //region implementation internals
    private static Manifest loadManifest(Path path) throws IOException {
        try(var stream = Files.newInputStream(path)) {
            return new Manifest(stream);
        }
    }

    @CheckForNull
    private static Path getExplicitManifestFile(Module module, SourceSet sourceSet) {
        var manifestPath = "META-INF/MANIFEST.MF";
        var resourceSet = sourceSet.resourceSet();
        if (resourceSet != null) {
            var resourceSetPath = module.sourceRoot(resourceSet);
            if (resourceSetPath != null) {
                var file = resourceSetPath.resolve(manifestPath);
                if (Files.exists(file)) {
                    return file;
                }
            }
        }
        var sourceSetPath = module.sourceRoot(sourceSet);
        if (sourceSetPath != null) {
            var file = sourceSetPath.resolve(manifestPath);
            if (Files.exists(file)) {
                return file;
            }
        }
        return null;
    }
    //endregion
}
