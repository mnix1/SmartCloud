package com.smartcloud.holder;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

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
