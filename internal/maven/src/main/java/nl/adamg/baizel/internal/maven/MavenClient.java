package nl.adamg.baizel.internal.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import javax.annotation.CheckForNull;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MavenClient {
    private final List<RemoteRepository> remoteRepositories;
    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession session;

    private MavenClient(
            List<RemoteRepository> remoteRepositories,
            RepositorySystem repositorySystem,
            RepositorySystemSession session
    ) {
        this.remoteRepositories = remoteRepositories;
        this.repositorySystem = repositorySystem;
        this.session = session;
    }

    public static MavenClient load(List<String> repositories) {
        var repositorySystem = getRepositorySystem();
        return new MavenClient(
                repositories.stream().map(MavenClient::getRepository).toList(),
                repositorySystem,
                getSession(repositorySystem)
        );
    }

    @CheckForNull
    public Path resolveCoords(String coords) {
        return resolve(new DefaultArtifact(coords));
    }

    @CheckForNull
    private Path resolve(Artifact coords) {
        var request = new ArtifactRequest(coords, remoteRepositories, null);
        try {
            var result = repositorySystem.resolveArtifact(session, request);
            var artifact = result.getArtifact();
            return artifact.getFile().toPath();
        } catch (ArtifactResolutionException e) {
            // code using MavenClient is responsible for reporting unresolved libraries, in a structured way
            return null;
        }
    }

    private static DefaultRepositorySystemSession getSession(RepositorySystem repositorySystem) {
        var session = MavenRepositorySystemUtils.newSession();
        var localRepositoryPath = Path.of(System.getProperty("user.home"), ".m2/repository");
        var localRepository = new LocalRepository(localRepositoryPath.toFile());
        var repositoryManager = repositorySystem.newLocalRepositoryManager(session, localRepository);
        session.setLocalRepositoryManager(repositoryManager);
        return session;
    }

    private static RemoteRepository getRepository(String url) {
        return new RemoteRepository.Builder(null, "default", url).build();
    }

    private static RepositorySystem getRepositorySystem() {
        var locator = MavenRepositorySystemUtils.newServiceLocator();
        var registry = getServiceRegistry();
        for (var entry : registry.entrySet()) {
            @SuppressWarnings("unchecked")
            var interfaceCast = (Class<Object>) entry.getKey();
            for (var impl : entry.getValue()) {
                locator.addService(interfaceCast, impl);
            }
        }
        return locator.getService(org.eclipse.aether.RepositorySystem.class);
    }

    private static Map<Class<?>, Set<Class<?>>> getServiceRegistry() {
        var serviceRegistry = new TreeMap<Class<?>, Set<Class<?>>>(Comparator.comparing(Class::getCanonicalName));
        serviceRegistry.put(org.eclipse.aether.spi.connector.RepositoryConnectorFactory.class, Set.of(org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory.class));
        serviceRegistry.put(org.eclipse.aether.spi.connector.transport.TransporterFactory.class, Set.of(org.eclipse.aether.transport.file.FileTransporterFactory.class, org.eclipse.aether.transport.http.HttpTransporterFactory.class));

        // defensive (TODO clean up)
        serviceRegistry.put(org.apache.maven.repository.internal.ModelCacheFactory.class, Set.of(org.apache.maven.repository.internal.DefaultModelCacheFactory.class));
        serviceRegistry.put(org.eclipse.aether.RepositorySystem.class, Set.of(org.eclipse.aether.internal.impl.DefaultRepositorySystem.class));
        serviceRegistry.put(org.eclipse.aether.impl.ArtifactDescriptorReader.class, Set.of(org.apache.maven.repository.internal.DefaultArtifactDescriptorReader.class));
        serviceRegistry.put(org.eclipse.aether.impl.ArtifactResolver.class, Set.of(org.eclipse.aether.internal.impl.DefaultArtifactResolver.class));
        serviceRegistry.put(org.eclipse.aether.impl.DependencyCollector.class, Set.of(org.eclipse.aether.internal.impl.collect.DefaultDependencyCollector.class));
        serviceRegistry.put(org.eclipse.aether.impl.Deployer.class, Set.of(org.eclipse.aether.internal.impl.DefaultDeployer.class));
        serviceRegistry.put(org.eclipse.aether.impl.Installer.class, Set.of(org.eclipse.aether.internal.impl.DefaultInstaller.class));
        serviceRegistry.put(org.eclipse.aether.impl.LocalRepositoryProvider.class, Set.of(org.eclipse.aether.internal.impl.DefaultLocalRepositoryProvider.class));
        serviceRegistry.put(org.eclipse.aether.impl.MetadataGeneratorFactory.class, Set.of(org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory.class, org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory.class));
        serviceRegistry.put(org.eclipse.aether.impl.MetadataResolver.class, Set.of(org.eclipse.aether.internal.impl.DefaultMetadataResolver.class));
        serviceRegistry.put(org.eclipse.aether.impl.OfflineController.class, Set.of(org.eclipse.aether.internal.impl.DefaultOfflineController.class));
        serviceRegistry.put(org.eclipse.aether.impl.RemoteRepositoryFilterManager.class, Set.of(org.eclipse.aether.internal.impl.filter.DefaultRemoteRepositoryFilterManager.class));
        serviceRegistry.put(org.eclipse.aether.impl.RemoteRepositoryManager.class, Set.of(org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager.class));
        serviceRegistry.put(org.eclipse.aether.impl.RepositoryConnectorProvider.class, Set.of(org.eclipse.aether.internal.impl.DefaultRepositoryConnectorProvider.class));
        serviceRegistry.put(org.eclipse.aether.impl.RepositoryEventDispatcher.class, Set.of(org.eclipse.aether.internal.impl.DefaultRepositoryEventDispatcher.class));
        serviceRegistry.put(org.eclipse.aether.impl.RepositorySystemLifecycle.class, Set.of(org.eclipse.aether.internal.impl.DefaultRepositorySystemLifecycle.class));
        serviceRegistry.put(org.eclipse.aether.impl.SyncContextFactory.class, Set.of(org.eclipse.aether.internal.impl.synccontext.legacy.DefaultSyncContextFactory.class));
        serviceRegistry.put(org.eclipse.aether.impl.UpdateCheckManager.class, Set.of(org.eclipse.aether.internal.impl.DefaultUpdateCheckManager.class));
        serviceRegistry.put(org.eclipse.aether.impl.UpdatePolicyAnalyzer.class, Set.of(org.eclipse.aether.internal.impl.DefaultUpdatePolicyAnalyzer.class));
        serviceRegistry.put(org.eclipse.aether.impl.VersionRangeResolver.class, Set.of(org.apache.maven.repository.internal.DefaultVersionRangeResolver.class));
        serviceRegistry.put(org.eclipse.aether.impl.VersionResolver.class, Set.of(org.apache.maven.repository.internal.DefaultVersionResolver.class));
        serviceRegistry.put(org.eclipse.aether.internal.impl.LocalPathComposer.class, Set.of(org.eclipse.aether.internal.impl.DefaultLocalPathComposer.class));
        serviceRegistry.put(org.eclipse.aether.internal.impl.TrackingFileManager.class, Set.of(org.eclipse.aether.internal.impl.DefaultTrackingFileManager.class));
        serviceRegistry.put(org.eclipse.aether.internal.impl.synccontext.named.NamedLockFactoryAdapterFactory.class, Set.of(org.eclipse.aether.internal.impl.synccontext.named.NamedLockFactoryAdapterFactoryImpl.class));
        serviceRegistry.put(org.eclipse.aether.spi.connector.checksum.ChecksumAlgorithmFactorySelector.class, Set.of(org.eclipse.aether.internal.impl.checksum.DefaultChecksumAlgorithmFactorySelector.class));
        serviceRegistry.put(org.eclipse.aether.spi.connector.checksum.ChecksumPolicyProvider.class, Set.of(org.eclipse.aether.internal.impl.DefaultChecksumPolicyProvider.class));
        serviceRegistry.put(org.eclipse.aether.spi.connector.layout.RepositoryLayoutFactory.class, Set.of(org.eclipse.aether.internal.impl.Maven2RepositoryLayoutFactory.class));
        serviceRegistry.put(org.eclipse.aether.spi.connector.layout.RepositoryLayoutProvider.class, Set.of(org.eclipse.aether.internal.impl.DefaultRepositoryLayoutProvider.class));
        serviceRegistry.put(org.eclipse.aether.spi.connector.transport.TransporterProvider.class, Set.of(org.eclipse.aether.internal.impl.DefaultTransporterProvider.class));
        serviceRegistry.put(org.eclipse.aether.spi.io.FileProcessor.class, Set.of(org.eclipse.aether.internal.impl.DefaultFileProcessor.class));
        serviceRegistry.put(org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory.class, Set.of(org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory.class, org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory.class));
        serviceRegistry.put(org.eclipse.aether.spi.log.LoggerFactory.class, Set.of(org.eclipse.aether.internal.impl.slf4j.Slf4jLoggerFactory.class));
        serviceRegistry.put(org.eclipse.aether.spi.synccontext.SyncContextFactory.class, Set.of(org.eclipse.aether.internal.impl.synccontext.DefaultSyncContextFactory.class));

        return serviceRegistry;
    }
}