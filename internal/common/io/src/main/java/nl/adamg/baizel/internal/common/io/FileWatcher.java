package nl.adamg.baizel.internal.common.io;

import nl.adamg.baizel.internal.common.util.Exceptions;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Can be used to subscribe to changes in files or directories.
 * Must be closed when no longer used to prevent memory leaks.
 */
public class FileWatcher implements AutoCloseable {
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final Map<WatchKey, Set<Subscription>> subscriptions = new ConcurrentHashMap<>();
    private final Map<Path, Subscription> pathToSubscription = new ConcurrentHashMap<>();
    private final WatchService watchService;
    private final Thread thread;

    public static FileWatcher start() throws IOException {
        var fileWatcher = new AtomicReference<FileWatcher>();
        var thread = new Thread(() -> fileWatcher.get().runWatcherThread());
        fileWatcher.set(new FileWatcher(FileSystems.getDefault().newWatchService(), thread));
        thread.setName(FileWatcher.class.getSimpleName() + "#" + System.identityHashCode(thread));
        thread.start();
        return fileWatcher.get();
    }

    private FileWatcher(WatchService watchService, Thread thread) {
        this.watchService = watchService;
        this.thread = thread;
    }

    public Subscription subscribe(Path path, Consumer<Path> handler) throws IOException {
        var registrationPoint = Files.isDirectory(path) ? path : path.getParent();
        var key = registrationPoint.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        var subscription = new Subscription(path, key, handler);
        subscriptions.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(subscription);
        pathToSubscription.put(path.toAbsolutePath().normalize(), subscription);
        return subscription;
    }

    @Override
    public void close() throws IOException, InterruptedException {
        alive.set(false);
        watchService.close();
        thread.interrupt();
        thread.join();
        subscriptions.clear();
        pathToSubscription.clear();
    }

    public class Subscription implements Closeable {
        private final AtomicBoolean alive = new AtomicBoolean(true);
        private final AtomicReference<Throwable> exception = new AtomicReference<>();
        private final Path path;
        private final WatchKey key;
        private final Consumer<Path> handler;

        public Subscription(Path path, WatchKey key, Consumer<Path> handler) {
            this.path = path;
            this.key = key;
            this.handler = handler;
        }

        @Override
        public void close() throws IOException {
            alive.set(false);
            var normalizedPath = path.toAbsolutePath().normalize();
            pathToSubscription.remove(normalizedPath);
            var subs = subscriptions.get(key);
            if (subs != null) {
                subs.remove(this);
                if (subs.isEmpty()) {
                    key.cancel();
                    subscriptions.remove(key);
                }
            }
            if (exception.get() != null) {
                throw Exceptions.rethrow(exception.get(), IOException.class);
            }
        }
    }

    private void runWatcherThread() {
        while (alive.get() && !Thread.currentThread().isInterrupted()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }

            var events = key.pollEvents();
            var subs = subscriptions.getOrDefault(key, Collections.emptySet());

            for (var event : events) {
                if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) continue;

                for (var subscription : subs) {
                    if (!subscription.alive.get()) continue;

                    var context = (Path) event.context();
                    var base = Files.isDirectory(subscription.path) ? subscription.path : subscription.path.getParent();
                    var changed = base.resolve(context).toAbsolutePath().normalize();
                    var target = subscription.path.toAbsolutePath().normalize();

                    if (!Files.isDirectory(subscription.path) && !changed.equals(target)) continue;

                    try {
                        subscription.handler.accept(changed);
                    } catch (RuntimeException e) {
                        subscription.exception.set(e);
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                subscriptions.remove(key);
            }
        }
    }
}
