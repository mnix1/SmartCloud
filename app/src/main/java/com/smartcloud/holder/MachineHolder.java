package com.smartcloud.holder;

import com.smartcloud.constant.MachineRole;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.util.FileManager;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class MachineHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String TABLE_NAME = "machine";
    public final static String TABLE_COLUMNS_CLIENT = "(id VARCHAR PRIMARY KEY, machineRole VARCHAR, active NUMBER)";
    public final static String TABLE_COLUMNS_SERVER = "(id VARCHAR PRIMARY KEY, machineRole VARCHAR, address VARCHAR, freeSpace NUMBER, totalSpace NUMBER, active NUMBER, lastContact NUMBER)";

    private String id;
    private MachineRole machineRole;
    private String address;
    private Long freeSpace;
    private Long totalSpace;
    private Boolean active;
    private Date lastContact;

    public MachineHolder(String id) {
        this();
        this.id = id;
    }

    public MachineHolder() {
        this.freeSpace = FileManager.getFreeSpace();
        this.totalSpace = FileManager.getTotalSpace();
        this.lastContact = new Date();
        setActive(true);
    }

    public MachineHolder(String id, String machineRole) {
        this(id);
        if (machineRole != null) {
            this.machineRole = Enum.valueOf(MachineRole.class, machineRole);
        }

    }

    public MachineHolder(String id, MachineRole machineRole, String address, Long freeSpace, Long totalSpace, Integer active, Date lastContact) {
        this.id = id;
        this.machineRole = machineRole;
        this.address = address;
        this.freeSpace = freeSpace;
        this.totalSpace = totalSpace;
        setActive(active);
        this.lastContact = lastContact;
    }

    public static void updateFromSlave(MachineHolder machineHolder, String address){
        machineHolder.setActive(true);
        machineHolder.setAddress(address);
        machineHolder.setLastContact(new Date());
        ServerDatabase.instance.updateMachine(machineHolder);
    }

    public static void setMyId() {
        MachineHolder machineHolder = ClientDatabase.instance.selectMachine();
        String myId = machineHolder != null ? machineHolder.getId() : null;
        if (myId == null) {
            myId = UUID.randomUUID().toString();
            ClientDatabase.instance.insertMachine(myId);
        }
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Boolean isActive() {
        return active;
    }

    public int getActive() {
        if (this.active) {
            return 1;
        }
        return 0;
    }

    public void setActive(Integer active) {
        if (active > 0) {
            this.active = true;
        } else {
            this.active = false;
        }
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getLastContact() {
        return lastContact;
    }

    public void setLastContact(Date lastContact) {
        this.lastContact = lastContact;
    }

    @Override
    public String toString() {
        return "MachineHolder{" +
                "id='" + id + '\'' +
                ", machineRole=" + getMachineRole() +
                ", address=" + address +
                ", freeSpace=" + freeSpace +
                ", totalSpace=" + totalSpace +
                ", active=" + active +
                ", lastContact=" + lastContact +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MachineHolder) {
            return id.equals(((MachineHolder) o).getId());
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
