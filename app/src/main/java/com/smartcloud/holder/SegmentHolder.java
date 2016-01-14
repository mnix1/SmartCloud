package com.smartcloud.holder;

import java.io.Serializable;

public class SegmentHolder implements Serializable {
    public final static String TABLE_NAME = "segment";
    public final static String TABLE_COLUMNS_CLIENT = "(id NUMBER PRIMARY KEY, path VARCHAR)";
    public final static String TABLE_COLUMNS_SERVER = "(id INTEGER PRIMARY KEY AUTOINCREMENT, fileId NUMBER, machineId VARCHAR, offset NUMBER, size NUMBER)";

    private Long id;
    private Long fileId;
    private String machineId;
    private Long offset;
    private Long size;
    private String path;

    public SegmentHolder(Long id, String path) {
        this.id = id;
        this.path = path;
    }

    public SegmentHolder(Long id, Long fileId, String machineId, Long offset, Long size) {
        this.id = id;
        this.fileId = fileId;
        this.machineId = machineId;
        this.offset = offset;
        this.size = size;
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

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "SegmentHolder{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", machineId='" + machineId + '\'' +
                ", offset=" + offset +
                ", size=" + size +
                ", path='" + path + '\'' +
                '}';
    }
}
