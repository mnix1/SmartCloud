package com.smartcloud.holder;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.exception.FileNotAvailable;
import com.smartcloud.task.MasterRequestDeleteSegmentToSlaveTask;
import com.smartcloud.task.Task;
import com.smartcloud.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String TABLE_NAME = "file";
    public final static String TABLE_COLUMNS_SERVER = "(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, size NUMBER, status VARCHAR)";

    private Long id;
    private String name;
    private Long size;
    private File file;

    public FileHolder(Long id, String name, Long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public FileHolder(Long size) {
        this.size = size;
    }

    public static void deleteFile(Long fileId) {
        Map<String, List<Long>> map = new HashMap<String, List<Long>>();
        for (SegmentHolder segmentHolder : ServerDatabase.instance.selectSegment(fileId)) {
            if (map.containsKey(segmentHolder.getMachineId())) {
                map.get(segmentHolder.getMachineId()).add(segmentHolder.getId());
            } else {
                List<Long> segmentIds = new ArrayList<Long>();
                segmentIds.add(segmentHolder.getId());
                map.put(segmentHolder.getMachineId(), segmentIds);
            }
        }
        for (String machineId : map.keySet()) {
            MachineHolder machineHolder = ServerDatabase.instance.selectMachine(machineId);
            if (machineHolder == null || !machineHolder.isActive()) {
                continue;
            }
            if (machineHolder.isServer()) {
                for (Long segmentId : map.get(machineId)) {
                    SegmentHolder segmentHolder = ClientDatabase.instance.selectSegment(segmentId);
                    if (segmentHolder != null) {
                        new File(segmentHolder.getPath()).delete();
                    }
                    ClientDatabase.instance.deleteSegment(segmentId);
                }
            } else {
                try {
                    Task task = new MasterRequestDeleteSegmentToSlaveTask(map.get(machineId));
                    new ClientCommunication(new Socket(InetAddress.getByName(machineHolder.getAddress()), ConnectionServer.SERVER_PORT), task).init();
                } catch (IOException e) {
                    machineHolder.setActive(false);
                    ServerDatabase.instance.updateMachine(machineHolder);
                    e.printStackTrace();
                }
            }
        }
        ServerDatabase.instance.deleteFile(fileId);
        ServerDatabase.instance.deleteSegments(fileId);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public String getSizeReadable() {
        return Util.sizeToReadableUnit(getSize());
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isAvailable() {
        List<SegmentHolder> segments = ServerDatabase.instance.selectSegment(getId());
        if (segments.isEmpty() || segments.get(0).getByteFrom() > 0) {
            return false;
        }
        long segmentTo = segments.get(0).getByteTo();
        for (int i = 1; i < segments.size(); i++) {
            SegmentHolder segmentHolder = segments.get(i);
            if (segmentTo >= segmentHolder.getByteFrom() - 1 && segmentTo < segmentHolder.getByteTo()) {
                segmentTo = segmentHolder.getByteTo();
            }
        }
        return segmentTo == getSize() - 1;
    }

    public boolean isActive() {
        long byteTo = -1;
        for (SegmentHolder segmentHolder : ServerDatabase.instance.selectSegment(getId())) {
            if (!segmentHolder.isActive() || segmentHolder.getByteFrom() - 1 < byteTo) {
                continue;
            }
            byteTo = segmentHolder.getByteTo();
        }
        return byteTo + 1 >= getSize();
    }
}
