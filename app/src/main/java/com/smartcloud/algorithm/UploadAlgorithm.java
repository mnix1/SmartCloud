package com.smartcloud.algorithm;

import com.smartcloud.web.NanoHTTPD;

public abstract class UploadAlgorithm {
    protected NanoHTTPD.HTTPSession mSession;

    public UploadAlgorithm(NanoHTTPD.HTTPSession session) {
        this.mSession = session;
    }
}
