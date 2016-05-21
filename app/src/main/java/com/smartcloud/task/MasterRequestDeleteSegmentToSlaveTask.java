package com.smartcloud.task;

import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.SegmentHolder;

public class MasterRequestDeleteSegmentToSlaveTask extends Task {
    private Long mSegmentHolderId;
    public MasterRequestDeleteSegmentToSlaveTask(Long segmentHolderId) {
        super(TaskType.MASTER_REQUEST_DELETE_SEGMENT_TO_SLAVE, SynchronizationMode.ASYNCHRONOUS);
        this.mSegmentHolderId = segmentHolderId;
    }

    @Override
    public void perform() {
        communicationManager.getOutput().println(mSegmentHolderId);
    }
}
