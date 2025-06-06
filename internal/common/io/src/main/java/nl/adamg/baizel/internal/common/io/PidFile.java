package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PidFile implements AutoCloseable {
    private final Path path;
    private final RandomAccessFile file;
    private final FileLock lock;
    private final FileChannel channel;
    private final boolean hadToWait;
    private final boolean previousOwnerCrashed;

    private PidFile(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        var fileExisted = Files.exists(path);
        var processWasRunning = fileExisted && isProcessRunning(path);
        this.previousOwnerCrashed = fileExisted && !processWasRunning;
        this.path = path;
        this.file = new RandomAccessFile(path.toFile(), "rw");
        this.channel = this.file.getChannel();
        var lock = channel.tryLock();
        this.hadToWait = (lock == null);
        this.lock = (lock == null) ? channel.lock() : lock;
        if (isProcessRunning(path)) {
            throw new IOException("Java lock was released but file still contains PID of a running process");
        }
        Files.writeString(path, String.valueOf(ProcessHandle.current().pid()));
    }

    public static boolean isProcessRunning(long pid) {
        return ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
    }

    public static boolean isProcessRunning(Path pidFile) throws IOException {
        if (!Files.exists(pidFile)) {
            return false;
        }
        var content = Files.readString(pidFile).trim();
        return !content.isEmpty() && isProcessRunning(Long.parseLong(content));
    }
    private static final Map<String, PidFile> OPEN_FILES = new ConcurrentHashMap<>();

    /**
     * @param exclusive not shared with other threads within this JVM
     */
    public static PidFile acquire(Path path, boolean exclusive) throws IOException {
        String key = path.toString();

        if (!exclusive) {
            // Shared mode — use computeIfAbsent
            try {
                return OPEN_FILES.computeIfAbsent(key, k -> {
                    try {
                        return new PidFile(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (RuntimeException e) {
                if (e.getCause() instanceof IOException io) {
                    throw io;
                }
                throw e;
            }
        }

        // Exclusive mode — only one thread at a time
        while (true) {
            PidFile existing = OPEN_FILES.get(key);
            if (existing == null) {
                PidFile newFile = new PidFile(path);
                if (OPEN_FILES.putIfAbsent(key, newFile) == null) {
                    return newFile;
                } else {
                    // Someone else beat us — close and retry
                    newFile.close();
                }
            } else {
                synchronized (existing) {
                    while (OPEN_FILES.get(key) != null) {
                        try {
                            existing.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IOException(e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            Files.write(path, new byte[0]);
            lock.release();
            channel.close();
            file.close();
        } finally {
            OPEN_FILES.remove(path.toString(), this);
            synchronized (this) {
                this.notifyAll(); // Wake up any waiting threads
            }
            Files.deleteIfExists(path);
        }
    }

    public boolean hadToWait() {
        return hadToWait;
    }

    public boolean previousOwnerCrashed() {
        return previousOwnerCrashed;
    }
}
