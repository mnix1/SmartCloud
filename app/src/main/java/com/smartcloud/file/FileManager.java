package com.smartcloud.file;

import android.os.Environment;

import com.smartcloud.communication.CommunicationTask;
import com.smartcloud.communication.CommunicationTaskHolder;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.CloudHolder;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;

import java.io.File;
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
        ConfigManager.configFile = new File(rootDirPath + "/config");
    }

    public static Long getFreeSpace() {
        return rootDir.getFreeSpace();
    }

    public static Long getTotalSpace() {
        return rootDir.getTotalSpace();
    }

    public static void manageUploadedFile(File file) {
        FileHolder fileHolder = new FileHolder(file);
        ServerDatabase.instance.insertFile(fileHolder);
        List<MachineHolder> machines = CloudHolder.getClientMachines();
        sendSegments(fileHolder, machines);
    }

    private static void sendSegments(FileHolder fileHolder, List<MachineHolder> machines) {
        long offset = 0;
        long size = machines.isEmpty() ? fileHolder.getSize() : fileHolder.getSize() / machines.size();
        for (int i = 0; i < machines.size(); i++) {
            MachineHolder machineHolder = machines.get(i);
            SegmentHolder segmentHolder = new SegmentHolder(null, fileHolder.getId(), machineHolder.getId(), offset, size);
            ServerDatabase.instance.insertSegment(segmentHolder);
            System.out.println("sendSegments " + segmentHolder);
            List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
            synchronized (tasks) {
                System.out.println("TASK ADDED " + CommunicationTask.SEND_SEGMENT);
                tasks.add(new CommunicationTaskHolder(CommunicationTask.SEND_SEGMENT, fileHolder, segmentHolder));
                System.out.println("TASK ADDED END");
            }
            offset = size;
            if (i == machines.size() - 1) {
                size = fileHolder.getSize() - offset;
            } else {
                size += fileHolder.getSize() / machines.size();
            }
        }
    }

    public static File manageDownloadFile(FileHolder fileHolder ) {
        Long fileId = fileHolder.getId();
        List<SegmentHolder> segments = ServerDatabase.instance.selectSegment(fileId);
        for (SegmentHolder segmentHolder : segments) {
            MachineHolder machineHolder = CloudHolder.getMachine(segmentHolder.getMachineId());
            List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
            synchronized (tasks) {
                System.out.println("TASK ADDED " + CommunicationTask.SEND_REQUEST_FOR_SEGMENT);
                tasks.add(new CommunicationTaskHolder(CommunicationTask.SEND_REQUEST_FOR_SEGMENT, segmentHolder.getId()));
                System.out.println("TASK ADDED END");
            }
        }
        return null;
    }

}
