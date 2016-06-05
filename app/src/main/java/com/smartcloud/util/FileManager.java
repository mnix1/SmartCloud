package com.smartcloud.util;

import android.os.Environment;

import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.SegmentHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public static void checkConsistency(){
        List<SegmentHolder> localSegments = ClientDatabase.instance.selectSegment();
        for (File file : FileManager.storageDir.listFiles()) {
            boolean found = false;
            for (SegmentHolder localSegment : localSegments) {
                if (localSegment.getPath().equals(file.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                file.delete();
            }
        }
        for (SegmentHolder localSegment : localSegments) {
            boolean found = false;
            for (File file : FileManager.storageDir.listFiles()) {
                if (localSegment.getPath().equals(file.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ClientDatabase.instance.deleteSegment(localSegment);
            }
        }
    }
    public static List<SegmentHolder> checkConsistency(List<SegmentHolder> segmentsFromMaster){
        List<SegmentHolder> localSegments = ClientDatabase.instance.selectSegment();
        for (SegmentHolder localSegment : localSegments) {
            boolean found = false;
            for (SegmentHolder segmentFromMaster : segmentsFromMaster) {
                if (localSegment.getId().equals(segmentFromMaster.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                File file = new File(localSegment.getPath());
                if (file.delete()) {
                    ClientDatabase.instance.deleteSegment(localSegment);
                }
            }
        }
        List<SegmentHolder> segmentsNotActive = new ArrayList<>();
        for (SegmentHolder segmentFromMaster : segmentsFromMaster) {
            boolean found = false;
            for (SegmentHolder localSegment : localSegments) {
                if (localSegment.getId().equals(segmentFromMaster.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                segmentsNotActive.add(segmentFromMaster);
            }
        }
        return segmentsNotActive;
    }

    public static Long getFreeSpace() {
        return rootDir.getFreeSpace();
    }

    public static Long getTotalSpace() {
        return rootDir.getTotalSpace();
    }
}
