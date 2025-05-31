package nl.adamg.baizel.internal.bootstrap;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

public class Timer implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(Timer.class.getName());
    private final Logger log;
    private final String description;
    private final Instant start;

    public Timer(Logger log, String description) {
        this.log = log;
        this.description = description;
        this.start = Instant.now();
    }

    public Timer(String description) {
        this(LOG, description);
    }

    public interface ThrowingSupplier<T, E extends Exception> {
        T get() throws E;
    }

    public static <T, E extends Exception> T timed(Logger log, ThrowingSupplier<T, E> timed, String description) throws E {
        try (var ignored = new Timer(log, description)) {
            return timed.get();
        }
    }

    @Override
    public void close() {
        var duration = Duration.between(start, Instant.now());
        log.info(description + " finished -- { \"durationMs\": " + duration.toMillis() + " }");
    }
}
