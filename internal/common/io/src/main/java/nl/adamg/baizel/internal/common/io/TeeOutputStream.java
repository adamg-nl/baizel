package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {
    private final OutputStream branch1;
    private final OutputStream branch2;

    public TeeOutputStream(OutputStream branch1, OutputStream branch2) {
        this.branch1 = branch1;
        this.branch2 = branch2;
    }

    @Override
    public void write(int b) throws IOException {
        branch1.write(b);
        branch2.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        branch1.write(b);
        branch2.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        branch1.write(b, off, len);
        branch2.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        branch1.flush();
        branch2.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            branch1.close();
        } finally {
            branch2.close();
        }
    }
}
