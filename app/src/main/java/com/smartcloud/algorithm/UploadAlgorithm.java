package com.smartcloud.algorithm;

import com.smartcloud.web.NanoHTTPD;

public abstract class UploadAlgorithm {
    protected NanoHTTPD.HTTPSession mSession;
    protected Algorithm mAlgorithm;

    public UploadAlgorithm(NanoHTTPD.HTTPSession session) {
        this.mSession = session;
    }
}
