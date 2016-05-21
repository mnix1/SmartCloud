package com.smartcloud.algorithm;

import com.smartcloud.holder.FileHolder;

public abstract class DownloadAlgorithm {
    protected FileHolder mFileHolder;
    protected String mMime;

    public DownloadAlgorithm(FileHolder fileHolder, String mime) {
        this.mFileHolder = fileHolder;
        this.mMime = mime;
    }
}
