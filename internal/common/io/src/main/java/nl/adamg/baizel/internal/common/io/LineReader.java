package nl.adamg.baizel.internal.common.io;

import javax.annotation.CheckForNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstraction that fits both in-memory collections and I/O InputStreams. In the factory methods,
 * null and non-existing inputs result in empty readers without throwing.
 */
public interface LineReader<E extends Exception> extends AutoCloseable {
    @CheckForNull
    String previewNext() throws E;

    void advance() throws E;

    default boolean hasNext() throws E {
        return previewNext() != null;
    }

    @CheckForNull
    default String takeNext() throws E {
        var next = previewNext();
        advance();
        return next;
    }

    @Override
    default void close() throws E {
        // nothing
    }

    static LineReader<RuntimeException> fromLinesString(@CheckForNull String input) {
        if (input == null) {
            return empty();
        }
        return fromStringList(Arrays.asList(input.split("\n")));
    }

    static LineReader<RuntimeException> fromStringList(@CheckForNull Collection<String> input) {
        if (input == null) {
            return empty();
        }
        var queue = new LinkedList<>(input);
        return new LineReader<>() {
            @CheckForNull
            @Override
            public String previewNext() throws RuntimeException {
                return queue.peek();
            }

            @Override
            public void advance() throws RuntimeException {
                queue.poll();
            }
        };
    }

    static LineReader<IOException> fromFile(@CheckForNull Path input) throws IOException {
        if (input == null || !Files.exists(input)) {
            return empty();
        }
        return fromInputStream(Files.newInputStream(input));
    }

    static LineReader<IOException> fromInputStream(@CheckForNull InputStream input) {
        if (input == null) {
            return empty();
        }
        var reader = new BufferedReader(new InputStreamReader(input));
        var nextLine = new AtomicReference<String>();
        return new LineReader<>() {
            @CheckForNull
            @Override
            public String previewNext() throws IOException {
                if (nextLine.get() == null) {
                    nextLine.set(reader.readLine());
                }
                return nextLine.get();
            }

            @Override
            public void advance() throws IOException {
                nextLine.set(reader.readLine());
            }

            @Override
            public void close() throws IOException {
                reader.close();
                input.close();
            }
        };
    }

    static <E extends Exception> LineReader<E> empty() {
        return new LineReader<>() {
            @CheckForNull
            @Override
            public String previewNext() {
                return null;
            }

            @Override
            public void advance() {
                // nothing
            }
        };
    }
}
