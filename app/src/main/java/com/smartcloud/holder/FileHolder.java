package com.smartcloud.holder;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.task.MasterRequestDeleteSegmentToSlaveTask;
import com.smartcloud.task.Task;
import com.smartcloud.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class FileHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String TABLE_NAME = "file";
    public final static String TABLE_COLUMNS_SERVER = "(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, size NUMBER)";

    private Long id;
    private String name;
    private Long size;
    private File file;

    public FileHolder(File file) {
        this.file = file;
        this.name = file.getName();
        this.size = file.length();
    }

    public FileHolder(Long id, File file) {
        this.id = id;
        this.file = file;
        this.size = file.length();
    }

    public FileHolder(Long id, String name, Long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public FileHolder(Long size) {
        this.size = size;
    }

    public static void deleteFile(Long fileId) {
        for (SegmentHolder segmentHolder : ServerDatabase.instance.selectSegment(fileId)) {
            MachineHolder machineHolder = ServerDatabase.instance.selectMachine(segmentHolder.getMachineId());
            if (machineHolder == null || !machineHolder.isActive()) {
                continue;
            }
            if (machineHolder.isServer()) {
                new File(ClientDatabase.instance.selectSegment(segmentHolder.getId()).getPath()).delete();
                ClientDatabase.instance.deleteSegment(segmentHolder);
            } else {
                try {
                    Task task = new MasterRequestDeleteSegmentToSlaveTask(segmentHolder.getId());
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
}
