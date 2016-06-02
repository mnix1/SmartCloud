package com.smartcloud.util;

import android.os.Environment;

import java.io.File;

public class FileManager {
    public static File storageDir;
    public static File rootDir;

    public static void setDirs() {
        String rootDirPath = Environment.getExternalStorageDirectory() + "/SmartCloud";
        rootDir = new File(rootDirPath);
        storageDir = new File(rootDirPath + "/storage");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    public static Long getFreeSpace() {
        return rootDir.getFreeSpace();
    }

    public static Long getTotalSpace() {
        return rootDir.getTotalSpace();
    }
}
