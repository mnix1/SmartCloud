package com.smartcloud.file;

import android.os.Environment;

import com.smartcloud.communication.CommunicationTask;
import com.smartcloud.communication.CommunicationTaskHolder;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.CloudHolder;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.web.NanoHTTPD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
        List<MachineHolder> machines = CloudHolder.getMachines();
        sendSegments(fileHolder, machines);
    }

    private static void sendSegments(final FileHolder fileHolder, List<MachineHolder> machines) {
        final List<SegmentHolder> segments = new ArrayList<SegmentHolder>();
        long from = 0;
        long to = machines.isEmpty() ? fileHolder.getSize() - 1 : fileHolder.getSize() / machines.size();
        for (int i = 0; i < machines.size(); i++) {
            MachineHolder machineHolder = machines.get(i);
            SegmentHolder segmentHolder = new SegmentHolder(null, fileHolder.getId(), machineHolder.getId(), from, to);
            segments.add(segmentHolder);
            ServerDatabase.instance.insertSegment(segmentHolder);
            System.out.println("sendSegments " + segmentHolder);
            if (machineHolder.isServer()) {
                segmentHolder.setPath(FileManager.storageDir + "/" + fileHolder.getId() + "^_^" + segmentHolder.getId());
                ClientDatabase.instance.insertSegment(segmentHolder);
                try {
                    File file = new File(segmentHolder.getPath());
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                    FileInputStream fileInputStream = new FileInputStream(fileHolder.getFile());
                    fileInputStream.skip(from);
                    int bufferSize = 1024 * 1024;
                    byte[] buffer = new byte[bufferSize];
                    long totalToRead = segmentHolder.getByteTo() - segmentHolder.getByteFrom() + 1;
                    int totalRead = 0;
                    int read = 0;
                    while ((read = fileInputStream.read(buffer, 0, Math.min((int) totalToRead - totalRead, bufferSize))) > 0 && totalRead < totalToRead) {
                        fileOutputStream.write(buffer, 0, read);
                        totalRead += read;
                    }
                    segmentHolder.setReady(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
                synchronized (tasks) {
                    System.out.println("TASK ADDED " + CommunicationTask.SEND_SEGMENT);
                    tasks.add(new CommunicationTaskHolder(CommunicationTask.SEND_SEGMENT, fileHolder, segmentHolder));
                    System.out.println("TASK ADDED END");
                }
            }
            from = to + 1;
            if (i == machines.size() - 2) {
                to = fileHolder.getSize() - 1;
            } else {
                to += fileHolder.getSize() / machines.size();
            }
        }
        addDeleteFileListener(fileHolder, segments);
    }

    public static void manageDownloadFile(FileHolder fileHolder) {
        Long fileId = fileHolder.getId();
        List<SegmentHolder> segments = ServerDatabase.instance.selectSegment(fileId);
        CloudHolder.addFilesToManage(fileId, segments);
        for (SegmentHolder segmentHolder : segments) {
            MachineHolder machineHolder = CloudHolder.getMachine(segmentHolder.getMachineId());
            System.out.println("manageDownloadFile segment " + segmentHolder + " machine " + machineHolder);
            if (machineHolder.isServer()) {
                segmentHolder.setReady(true);
            } else {
                List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
                synchronized (tasks) {
                    System.out.println("TASK ADDED " + CommunicationTask.REQUEST_FOR_SEND_SEGMENT);
                    tasks.add(new CommunicationTaskHolder(CommunicationTask.REQUEST_FOR_SEND_SEGMENT, fileHolder.getId(), segmentHolder.getId()));
                    System.out.println("TASK ADDED END");
                }
            }
        }
        while (!CloudHolder.isFileReady(fileId)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        File file = fileHolder.getFile();
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fileOutputStream = null;
        try {
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file, false);
            for (SegmentHolder segmentHolder : CloudHolder.getSegmentsOfFileToManage(fileId)) {
                File segmentFile = new File(storageDir + "/" + fileId + "^_^" + segmentHolder.getId());
                byte[] buffer = new byte[1024 * 1024];
                FileInputStream segmentFileInputStream = new FileInputStream(segmentFile);
                int read;
                while ((read = segmentFileInputStream.read(buffer)) >= 0) {
                    fileOutputStream.write(buffer, 0, read);
                }
                segmentFileInputStream.close();
                if (!segmentHolder.getMachineId().equals(MachineHolder.ME.getId())) {
                    segmentFile.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void manageDeleteFile(Long fileId) {
        List<SegmentHolder> segments = ServerDatabase.instance.selectSegment(fileId);
        for (SegmentHolder segmentHolder : segments) {
            MachineHolder machineHolder = CloudHolder.getMachine(segmentHolder.getMachineId());
            if (machineHolder == null) {
                continue;
            }
            System.out.println("manageDeleteFile segment " + segmentHolder + " machine " + machineHolder);
            if (machineHolder.isServer()) {
                new File(ClientDatabase.instance.selectSegment(segmentHolder.getId()).getPath()).delete();
            } else {
                List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
                synchronized (tasks) {
                    System.out.println("TASK ADDED " + CommunicationTask.REQUEST_FOR_DELETE_SEGMENT);
                    tasks.add(new CommunicationTaskHolder(CommunicationTask.REQUEST_FOR_DELETE_SEGMENT, segmentHolder.getId()));
                    System.out.println("TASK ADDED END");
                }
            }
        }
        ServerDatabase.instance.deleteFile(fileId);
        ServerDatabase.instance.deleteSegments(fileId);
    }

    public static void deleteSegment(Long segmentId) {
        SegmentHolder segmentHolder = ClientDatabase.instance.selectSegment(segmentId);
        File file = new File(segmentHolder.getPath());
        file.delete();
        ClientDatabase.instance.deleteSegment(segmentHolder);
    }


    public static void addDeleteFileListener(final File file, final NanoHTTPD.Response response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!response.isCloseConnection()) {
                    System.out.println("WAITING FOR DELETE FILE");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                file.delete();
            }
        }).start();
    }

    public static void addDeleteFileListener(final FileHolder fileHolder, final List<SegmentHolder> segments) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ready = false;
                while (!ready) {
                    ready = true;
                    for (SegmentHolder segmentHolder : segments) {
                        if (!segmentHolder.isReady()) {
                            System.out.println("DELETE FILE NOT " + segmentHolder.toString());
                            ready = false;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
                System.out.println("DELETE FILE " + fileHolder.getName());
                fileHolder.getFile().delete();
            }
        }).start();
    }
}
