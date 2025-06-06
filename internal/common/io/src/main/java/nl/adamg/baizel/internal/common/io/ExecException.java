package nl.adamg.baizel.internal.common.io;

import java.io.IOException;

public final class ExecException extends IOException {
    private final int exitCode;

    public ExecException(Exception cause) {
        super(cause);
        this.exitCode = -1;
    }

    public ExecException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public int exitCode() {
        return exitCode;
    }
}
