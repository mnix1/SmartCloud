package com.smartcloud.holder;

import java.io.Serializable;

public class SegmentHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String TABLE_NAME = "segment";
    public final static String TABLE_COLUMNS_CLIENT = "(id NUMBER PRIMARY KEY, path VARCHAR)";
    public final static String TABLE_COLUMNS_SERVER = "(id INTEGER PRIMARY KEY AUTOINCREMENT, fileId NUMBER, machineId VARCHAR, byteFrom NUMBER, byteTo NUMBER)";

    private Long id;
    private Long fileId;
    private String machineId;
    private Long byteFrom;
    private Long byteTo;
    private String path;
    private boolean ready;

    public SegmentHolder(Long id) {
        this.id = id;
    }

    public SegmentHolder(Long id, String path) {
        this.id = id;
        this.path = path;
    }

    public SegmentHolder(Long id, Long fileId, String machineId, Long byteFrom, Long byteTo) {
        this.id = id;
        this.fileId = fileId;
        this.machineId = machineId;
        this.byteFrom = byteFrom;
        this.byteTo = byteTo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public Long getByteFrom() {
        return byteFrom;
    }

    public void setByteFrom(Long byteFrom) {
        this.byteFrom = byteFrom;
    }

    public Long getByteTo() {
        return byteTo;
    }

    public void setByteTo(Long byteTo) {
        this.byteTo = byteTo;
    }

    public Long getSize() {
        return byteTo - byteFrom;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = super.equals(o);
        if (equals) {
            return true;
        }
        if (o instanceof SegmentHolder) {
            return id.equals(((SegmentHolder) o).getId());
        }
        return equals;
    }

    @Override
    public String toString() {
        return "SegmentHolder{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", machineId='" + machineId + '\'' +
                ", byteFrom=" + byteFrom +
                ", byteTo=" + byteTo +
                ", path='" + path + '\'' +
                '}';
    }
}
