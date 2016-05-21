package com.smartcloud.web;

import com.smartcloud.util.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerFactory implements NanoHTTPD.TempFileManagerFactory {
    @Override
    public NanoHTTPD.TempFileManager create() {
        return new HTTPDFileManager();
    }

    private static class HTTPDFileManager implements NanoHTTPD.TempFileManager {

        private final File tmpdir;

        private final List<NanoHTTPD.TempFile> tempFiles;

        private HTTPDFileManager() {
            this.tmpdir = FileManager.storageDir;
            this.tempFiles = new ArrayList<NanoHTTPD.TempFile>();
        }

        @Override
        public void clear() {
            if (!this.tempFiles.isEmpty()) {
                System.out.println("Cleaning up:");
            }
            for (int i = 0; i < this.tempFiles.size() - 1; i++) {
                try {
                    System.out.println("   " + this.tempFiles.get(i).getName());
                    this.tempFiles.get(i).delete();
                } catch (Exception ignored) {
                }
            }
            this.tempFiles.clear();
        }

        @Override
        public NanoHTTPD.TempFile createTempFile(String filename_hint) throws Exception {
            NanoHTTPD.TempFile tempFile = filename_hint != null ? new PartFile(new File(this.tmpdir.getAbsoluteFile() + "/" + filename_hint)) : new NanoHTTPD.DefaultTempFile(this.tmpdir);
//            NanoHTTPD.DefaultTempFile tempFile = filename_hint != null ? new NanoHTTPD.DefaultTempFile(this.tmpdir.getAbsoluteFile() + "/" + filename_hint) : new NanoHTTPD.DefaultTempFile(this.tmpdir);
            this.tempFiles.add(tempFile);
            System.out.println("Created tempFile: " + tempFile.getName());
            return tempFile;
        }

    }
}


