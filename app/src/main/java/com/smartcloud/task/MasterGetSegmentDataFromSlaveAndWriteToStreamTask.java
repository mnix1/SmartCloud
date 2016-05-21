package com.smartcloud.task;

import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.holder.SegmentHolder;

import java.io.OutputStream;

public class MasterGetSegmentDataFromSlaveAndWriteToStreamTask extends Task {
    private SegmentHolder mSegmentHolder;
    private OutputStream mOutputStream;

    public MasterGetSegmentDataFromSlaveAndWriteToStreamTask(SegmentHolder segmentHolder, OutputStream outputStream) {
        super(TaskType.MASTER_GET_SEGMENT_DATA_FROM_SLAVE_AND_WRITE_TO_STREAM, SynchronizationMode.SYNCHRONOUS);
        this.mSegmentHolder = segmentHolder;
        this.mOutputStream = outputStream;
    }

    @Override
    public void perform() {
        communicationManager.getOutput().println(mSegmentHolder.getId());
        communicationManager.receiveSegmentDataAndWriteToStream(mSegmentHolder.getId(), mOutputStream);
    }
}
