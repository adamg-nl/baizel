package nl.adamg.baizel.internal.bootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * It's a Maven Client that can be compiled before Maven is available.
 * At runtime, it retrieves just the Maven client libraries (from Eclipse project)
 * from the repository configured at the root {@code build.gradle}.
 * Then, it loads these libraries into a child classloader, and reflectively calls into them.
 */
public final class MavenClient {
    private final Logger LOG = Logger.getLogger(MavenClient.class.getName());
    private final AfterClientLib libClient;
    private final Map<String, Path> cache = new ConcurrentHashMap<>();

    public static MavenClient loadClient(List<String> repositories, Path baizelRoot) throws IOException {
        var mavenClientLibDependencies = BeforeClientLib.resolveMavenClientLibDependencies(baizelRoot, repositories.get(0));
        var dynamicLibs = DynamicClassLoader.forPaths(mavenClientLibDependencies, MavenClient.class);
        var libClient = AfterClientLib.load(repositories.stream().map(BeforeClientLib::url).toList(), dynamicLibs);
        return new MavenClient(libClient);
    }

    public Path resolve(String coordinates) {
        var jarPath = cache.get(coordinates);
        if (jarPath == null) {
            jarPath = libClient.resolveCoords(coordinates);
            cache.put(coordinates, jarPath);
            LOG.info("resolved library -- { \"coordinates\": \"" + coordinates + "\", \"jarPath\": \"" + jarPath + "\" }");
        }
        return jarPath;
    }

    //region internal utils
    private MavenClient(AfterClientLib libClient) {
        this.libClient = libClient;
    }

    private static class BeforeClientLib {
        private static final Pattern COORDINATES_PATTERN = Pattern.compile("(?<org>[^:]+):(?<artifact>[^:]+):(?<version>.+)");
        private final String remoteRepositoryUrl;
        private final Path localRepository;

        public BeforeClientLib(String remoteRepositoryUrl) {
            this(remoteRepositoryUrl, Path.of(System.getProperty("user.home"), ".m2/repository"));
        }

        public BeforeClientLib(String remoteRepositoryUrl, Path localRepository) {
            this.remoteRepositoryUrl = remoteRepositoryUrl;
            this.localRepository = localRepository;
        }

        @SuppressWarnings("unused")
        public static List<Path> resolveMavenClientLibDependencies(Path baizelRoot, String remoteRepositoryUrl) throws IOException {
            var moduleInfoFile = baizelRoot.resolve("internal/bootstrap/src/main/java/module-info.java");
            var moduleInfoText = Files.readString(moduleInfoFile).replaceAll("//baizel//", "");
            var libraryModuleIds = new TreeSet<String>();
            try {
                var moduleInfo = new ConfigParser().read(moduleInfoText);
            } catch (ParseException e) {
                throw new RuntimeException("broken " + moduleInfoFile, e);
            }
            var respository = new BeforeClientLib(remoteRepositoryUrl);
            var classpath = new ArrayList<Path>();
//            var moduleConfig = BootstrapBuilder.readGradleConfig(moduleInfoFile);
//            for (var configuration : List.of("implementation", "runtimeOnly")) {
//                var dependencies = moduleConfig.get(configuration);
//                if (dependencies == null) {
//                    continue;
//                }
//                for (var dependency : dependencies) {
//                    var localPath = respository.resolve(dependency);
//                    if (localPath != null) {
//                        classpath.add(localPath);
//                    }
//                }
//            }
            return classpath;
        }

        public static List<String> getRepositories(Path baizelRoot) throws IOException {

            throw null;
//            return BootstrapBuilder.readGradleConfig(baizelRoot.resolve("build.gradle")).get("url");
        }

        /*@CheckForNull*/
        public Path resolve(String coordinates) throws IOException {
            var matcher = COORDINATES_PATTERN.matcher(coordinates);
            if (!matcher.matches()) {
                return null;
            }
            var org = matcher.group("org");
            var artifact = matcher.group("artifact");
            var version = matcher.group("version");
            var path = org.replace(".", "/") + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ".jar";
            var localFile = localRepository.resolve(path);
            if (Files.exists(localFile)) {
                return localFile;
            }
            return downloadFile(remoteRepositoryUrl + path, localFile);
        }

        private static URL url(String p) {
            try {
                return new URI(p).toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        private static Path downloadFile(String remoteUrl, Path localPath) throws IOException {
            Files.createDirectories(localPath.getParent());
            try (var stream = new FileOutputStream(localPath.toFile())) {
                var channel = Channels.newChannel(new URI(remoteUrl).toURL().openStream());
                stream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
                return localPath;
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
    }

    private static class AfterClientLib {
        private final List<Object> remoteRepositories; // List<RemoteRepository>
        private final Object repositorySystem; // RepositorySystem
        private final Object session; // RepositorySystemSession
        private final DynamicClassLoader<URLClassLoader> libs;

        private AfterClientLib(
                List<Object> remoteRepositories,
                Object repositorySystem,
                Object session,
                DynamicClassLoader<URLClassLoader> libs
        ) {
            this.remoteRepositories = remoteRepositories;
            this.repositorySystem = repositorySystem;
            this.session = session;
            this.libs = libs;
        }

        public static AfterClientLib load(List<URL> repositories, DynamicClassLoader<URLClassLoader> libs) {
            var repositorySystem = getRepositorySystem(libs);
            return new AfterClientLib(
                    repositories.stream().map(u -> getRepository(u, libs)).toList(),
                    repositorySystem,
                    getSession(repositorySystem, libs),
                    libs
            );
        }

        /*@CheckForNull*/
        public Path resolveCoords(String coords) {
            return resolve(libs.requireConstruct("org.eclipse.aether.artifact.DefaultArtifact", coords));
        }

        /*@CheckForNull*/
        private Path resolve(Object coords) { // Artifact
            var request = libs.requireConstruct("org.eclipse.aether.resolution.ArtifactRequest", coords, remoteRepositories, null);
            try {
                var result = libs.requireInvoke(repositorySystem, "resolveArtifact", session, request);
                var artifact = libs.requireInvoke(result, "getArtifact");
                return ((File) libs.requireInvoke(artifact, "getFile")).toPath();
            } catch (RuntimeException e) {
                // code using MavenClient is responsible for reporting unresolved libraries, in a structured way
                if (e.getCause().getClass().getName().equals("org.eclipse.aether.resolution.ArtifactResolutionException")) {
                    return null;
                }
                throw e;
            }
        }

        // DefaultRepositorySystemSession, RepositorySystem
        private static Object getSession(Object repositorySystem, DynamicClassLoader<?> libs) {
            var session = libs.invoke("org.apache.maven.repository.internal.MavenRepositorySystemUtils", "newSession");
            var localRepositoryPath = Path.of(System.getProperty("user.home"), ".m2/repository");
            var localRepository = libs.requireConstruct("org.eclipse.aether.repository.LocalRepository", localRepositoryPath.toFile());
            var repositoryManager = libs.requireInvoke(repositorySystem, "newLocalRepositoryManager", session, localRepository);
            libs.invoke(session, "setLocalRepositoryManager", repositoryManager);
            return session;
        }

        private static Object getRepository(URL url, DynamicClassLoader<?> libs) { // RemoteRepository
            return libs.requireInvoke(libs.requireConstruct("org.eclipse.aether.repository.RemoteRepository$Builder",
                    null,
                    "default",
                    url.toString()
            ), "build");
        }

        private static Object getRepositorySystem(DynamicClassLoader<?> libs) {
            var locator = libs.requireInvoke("org.apache.maven.repository.internal.MavenRepositorySystemUtils", "newServiceLocator");
            var registry = getServiceRegistry(libs);
            for (var entry : registry.entrySet()) {
                @SuppressWarnings("unchecked")
                var interfaceCast = (Class<Object>) entry.getKey();
                for (var impl : entry.getValue()) {
                    libs.invoke(locator, "addService", interfaceCast, impl);
                }
            }
            return libs.requireInvoke(locator, "getService", libs.forName("org.eclipse.aether.RepositorySystem"));
        }

        private static Map<Class<?>, Set<Class<?>>> getServiceRegistry(DynamicClassLoader<?> libs) {
            var serviceRegistry = new TreeMap<Class<?>, Set<Class<?>>>(Comparator.comparing(Class::getCanonicalName));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.connector.RepositoryConnectorFactory"), Set.of(libs.forName("org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.connector.transport.TransporterFactory"), Set.of(libs.forName("org.eclipse.aether.transport.file.FileTransporterFactory"), libs.forName("org.eclipse.aether.transport.http.HttpTransporterFactory")));
            
            
            // defensive
            serviceRegistry.put(libs.forName("org.apache.maven.repository.internal.ModelCacheFactory"), Set.of(libs.forName("org.apache.maven.repository.internal.DefaultModelCacheFactory")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.RepositorySystem"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultRepositorySystem")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.ArtifactDescriptorReader"), Set.of(libs.forName("org.apache.maven.repository.internal.DefaultArtifactDescriptorReader")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.ArtifactResolver"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultArtifactResolver")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.DependencyCollector"), Set.of(libs.forName("org.eclipse.aether.internal.impl.collect.DefaultDependencyCollector")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.Deployer"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultDeployer")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.Installer"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultInstaller")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.LocalRepositoryProvider"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultLocalRepositoryProvider")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.MetadataGeneratorFactory"), Set.of(libs.forName("org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory"), libs.forName("org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.MetadataResolver"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultMetadataResolver")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.OfflineController"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultOfflineController")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.RemoteRepositoryFilterManager"), Set.of(libs.forName("org.eclipse.aether.internal.impl.filter.DefaultRemoteRepositoryFilterManager")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.RemoteRepositoryManager"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.RepositoryConnectorProvider"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultRepositoryConnectorProvider")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.RepositoryEventDispatcher"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultRepositoryEventDispatcher")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.RepositorySystemLifecycle"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultRepositorySystemLifecycle")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.SyncContextFactory"), Set.of(libs.forName("org.eclipse.aether.internal.impl.synccontext.legacy.DefaultSyncContextFactory")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.UpdateCheckManager"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultUpdateCheckManager")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.UpdatePolicyAnalyzer"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultUpdatePolicyAnalyzer")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.VersionRangeResolver"), Set.of(libs.forName("org.apache.maven.repository.internal.DefaultVersionRangeResolver")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.impl.VersionResolver"), Set.of(libs.forName("org.apache.maven.repository.internal.DefaultVersionResolver")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.internal.impl.LocalPathComposer"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultLocalPathComposer")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.internal.impl.TrackingFileManager"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultTrackingFileManager")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.internal.impl.synccontext.named.NamedLockFactoryAdapterFactory"), Set.of(libs.forName("org.eclipse.aether.internal.impl.synccontext.named.NamedLockFactoryAdapterFactoryImpl")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector"), Set.of(libs.forName("org.eclipse.aether.internal.impl.checksum.DefaultChecksumAlgorithmFactorySelector")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.connector.checksum.ChecksumPolicyProvider"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultChecksumPolicyProvider")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.connector.layout.RepositoryLayoutFactory"), Set.of(libs.forName("org.eclipse.aether.internal.impl.Maven2RepositoryLayoutFactory")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.connector.layout.RepositoryLayoutProvider"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultRepositoryLayoutProvider")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.connector.transport.TransporterProvider"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultTransporterProvider")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.io.FileProcessor"), Set.of(libs.forName("org.eclipse.aether.internal.impl.DefaultFileProcessor")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory"), Set.of(libs.forName("org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory"), libs.forName("org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.log.LoggerFactory"), Set.of(libs.forName("org.eclipse.aether.internal.impl.slf4j.Slf4jLoggerFactory")));
            serviceRegistry.put(libs.forName("org.eclipse.aether.spi.synccontext.SyncContextFactory"), Set.of(libs.forName("org.eclipse.aether.internal.impl.synccontext.DefaultSyncContextFactory")));


            return serviceRegistry;
        }
    }
    //endregion
}
