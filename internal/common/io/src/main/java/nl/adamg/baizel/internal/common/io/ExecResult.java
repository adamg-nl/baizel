package nl.adamg.baizel.internal.common.io;

import java.io.IOException;

public record ExecResult(int exitCode, String stdErr, String stdOut) {
    public void throwIfFailed() throws ExecException {
        if (exitCode == 0) {
            return;
        }
        if (!stdErr.isBlank()) {
            throw new ExecException(exitCode, stdErr);
        }
        if (!stdOut.isBlank()) {
            throw new ExecException(exitCode, stdOut);
        }
        throw new ExecException(exitCode, "exit code: " + exitCode);
    }

    public ExecResult assertSuccess() throws IOException {
        throwIfFailed();
        return this;
    }
}
