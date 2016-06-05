package com.smartcloud.task;

import android.util.Base64;

import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.web.NanoHTTPD;

import java.io.IOException;
import java.io.PrintWriter;

public class MasterSendSegmentDataToSlaveTask extends Task {
    private byte[] mData;
    private int mOffset;
    private int mLength;
    private NanoHTTPD.HTTPSession mSession;
    private SegmentHolder mSegmentHolder;

    public MasterSendSegmentDataToSlaveTask(byte[] data, int offset, int length, NanoHTTPD.HTTPSession mSession, SegmentHolder segmentHolder) {
        super(TaskType.MASTER_SEND_SEGMENT_DATA_TO_SLAVE, SynchronizationMode.SYNCHRONOUS);
        this.mData = data;
        this.mOffset = offset;
        this.mLength = length;
        this.mSession = mSession;
        this.mSegmentHolder = segmentHolder;
    }

    @Override
    public void perform() {
        long size = mSegmentHolder.getSize();
        PrintWriter output = communicationManager.getOutput();
        output.println(mSegmentHolder.getId());
        output.println(size);
        output.println(new String(Base64.encode(mData, mOffset, mLength, Base64.NO_WRAP)));
        size -= mLength;
        while (mSession.rlen > 0 && size > 0) {
            try {
                mSession.rlen = mSession.inputStream.read(mData, 0, Math.min((int) size, mData.length));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mSession.rlen > 0) {
                size -= mSession.rlen;
                output.println(new String(Base64.encode(mData, 0, mSession.rlen, Base64.NO_WRAP)));
            }
        }
    }
}
