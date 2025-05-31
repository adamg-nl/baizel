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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * It's a Maven Client that can be compiled before Maven is available.
 * At runtime it retrieves just the Maven client libraries (from Eclipse project)
 * from the repository configured at the root {@code build.gradle}.
 * Then, it loads these libraries into a child classloader, and reflectively calls into them.
 */
public final class MavenClient {
    private final AfterClientLib libClient;
    private final Map<String, Path> cache = new ConcurrentHashMap<>();

    public static MavenClient loadClient(Path baizelRoot) throws IOException {
        var repositories = BeforeClientLib.getRepositories(baizelRoot);
        var mavenClientLibDependencies = BeforeClientLib.resolveMavenClientLibDependencies(baizelRoot, repositories.getFirst());
        var dynamicLibs = DynamicClassLoader.forPaths(mavenClientLibDependencies, MavenClient.class);
        var libClient = AfterClientLib.load(repositories.stream().map(BeforeClientLib::url).toList(), dynamicLibs);
        return new MavenClient(libClient);
    }

    public Path resolve(String coordinates) {
        var jarPath = cache.get(coordinates);
        if (jarPath == null) {
            jarPath = libClient.resolveCoords(coordinates);
            cache.put(coordinates, jarPath);
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
            var gradleFile = baizelRoot.resolve("internal/bootstrap/build.gradle");
            var respository = new BeforeClientLib(remoteRepositoryUrl);
            var classpath = new ArrayList<Path>();
            var moduleConfig = BootstrapBuilder.readGradleConfig(gradleFile);
            for (var configuration : List.of("implementation", "runtimeOnly")) {
                var dependencies = moduleConfig.get(configuration);
                if (dependencies == null) {
                    continue;
                }
                for (var dependency : dependencies) {
                    var localPath = respository.resolve(dependency);
                    if (localPath != null) {
                        classpath.add(localPath);
                    }
                }
            }
            return classpath;
        }

        public static List<String> getRepositories(Path baizelRoot) throws IOException {
            return BootstrapBuilder.readGradleConfig(baizelRoot.resolve("build.gradle")).get("url");
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
        private static Object getSession(Object repositorySystem, DynamicClassLoader libs) {
            var session = libs.invoke("org.apache.maven.repository.internal.MavenRepositorySystemUtils", "newSession");
            var localRepositoryPath = Path.of(System.getProperty("user.home"), ".m2/repository");
            var localRepository = libs.requireConstruct("org.eclipse.aether.repository.LocalRepository", localRepositoryPath.toFile());
            var repositoryManager = libs.requireInvoke(repositorySystem, "newLocalRepositoryManager", session, localRepository);
            libs.invoke(session, "setLocalRepositoryManager", repositoryManager);
            return session;
        }

        private static Object getRepository(URL url, DynamicClassLoader libs) { // RemoteRepository
            return libs.requireInvoke(libs.requireConstruct("org.eclipse.aether.repository.RemoteRepository$Builder",
                    null,
                    "default",
                    url.toString()
            ), "build");
        }

        private static Object getRepositorySystem(DynamicClassLoader libs) {
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
            return serviceRegistry;
        }
    }
    //endregion
}
