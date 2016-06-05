package com.smartcloud.task;

import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.SegmentHolder;

import java.util.List;

public class MasterRequestDeleteSegmentToSlaveTask extends Task {
    private List<Long> mSegmentHolderIds;
    public MasterRequestDeleteSegmentToSlaveTask(List<Long> segmentIds) {
        super(TaskType.MASTER_REQUEST_DELETE_SEGMENT_TO_SLAVE, SynchronizationMode.ASYNCHRONOUS);
        this.mSegmentHolderIds = segmentIds;
    }

    @Override
    public void perform() {
        communicationManager.sendObject(mSegmentHolderIds);
    }
}
