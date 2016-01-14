package com.smartcloud.holder;

import com.smartcloud.communication.CommunicationManager;
import com.smartcloud.constant.MachineRole;
import com.smartcloud.file.FileManager;

import java.io.Serializable;
import java.net.InetAddress;

public class MachineHolder implements Serializable {
    public final static MachineHolder ME = new MachineHolder();

    public final static String TABLE_NAME = "machine";
    public final static String TABLE_COLUMNS_CLIENT = "(id VARCHAR PRIMARY KEY)";

    private String id;
    private MachineRole machineRole;
    private CommunicationManager communicationManager;
    private Long freeSpace;
    private Long totalSpace;

    public MachineHolder(String id) {
        this.id = id;
    }

    public MachineHolder() {
        this.freeSpace = FileManager.getFreeSpace();
        this.totalSpace = FileManager.getTotalSpace();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isServer() {
        if (machineRole == MachineRole.MASTER) {
            return true;
        }
        return false;
    }

    public MachineRole getMachineRole() {
        return machineRole;
    }

    public void setMachineRole(MachineRole machineRole) {
        this.machineRole = machineRole;
    }

    public CommunicationManager getCommunicationManager() {
        return communicationManager;
    }

    public void setCommunicationManager(CommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
    }

    public InetAddress getAddress() {
        return communicationManager.getSocket().getLocalAddress();
    }


    public Long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(Long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public Long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }

    @Override
    public String toString() {
        return "MachineHolder{" +
                "id='" + id + '\'' +
                ", machineRole=" + machineRole +
                ", communicationManager=" + communicationManager +
                ", freeSpace=" + freeSpace +
                ", totalSpace=" + totalSpace +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return id.equals(((MachineHolder) o).getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
