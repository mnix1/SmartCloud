package com.smartcloud.web;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class PartFile implements NanoHTTPD.TempFile {

    private final File file;

    private final OutputStream fstream;

    public PartFile(File tempdir) throws IOException {
        this.file = tempdir;
        this.file.createNewFile();
        this.fstream = new FileOutputStream(this.file);
    }

    private static final void safeClose(Object closeable) {
        try {
            if (closeable != null) {
                if (closeable instanceof Closeable) {
                    ((Closeable) closeable).close();
                } else if (closeable instanceof Socket) {
                    ((Socket) closeable).close();
                } else if (closeable instanceof ServerSocket) {
                    ((ServerSocket) closeable).close();
                } else {
                    throw new IllegalArgumentException("Unknown object to close");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not close");
        }
    }

    @Override
    public void delete() throws Exception {
        safeClose(this.fstream);
        if (!this.file.delete()) {
            throw new Exception("could not delete temporary file");
        }
    }

    @Override
    public String getName() {
        return this.file.getAbsolutePath();
    }

    @Override
    public OutputStream open() throws Exception {
        return this.fstream;
    }
}

