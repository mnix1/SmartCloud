package com.smartcloud.task;

import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.holder.SegmentHolder;

public class MasterSendSegmentDataToSlaveTask extends Task {
    private byte[] mData;
    private SegmentHolder mSegmentHolder;

    public MasterSendSegmentDataToSlaveTask(byte[] data, SegmentHolder segmentHolder) {
        super(TaskType.MASTER_SEND_SEGMENT_DATA_TO_SLAVE, SynchronizationMode.SYNCHRONOUS);
        this.mData = data;
        this.mSegmentHolder = segmentHolder;
    }

    @Override
    public void perform() {
        communicationManager.sendSegmentData(mData, mSegmentHolder);
    }
}
