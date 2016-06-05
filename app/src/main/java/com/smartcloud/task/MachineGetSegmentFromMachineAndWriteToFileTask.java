package com.smartcloud.task;

import com.smartcloud.communication.ServerCommunication;
import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.SegmentHolder;

import java.util.List;
import java.util.Map;

public class MachineGetSegmentFromMachineAndWriteToFileTask extends Task {
    List<Long> mSegmentIds;
    Map<Long, Long> mOldNewSegmentId;

    public MachineGetSegmentFromMachineAndWriteToFileTask(List<Long> segmentIds, Map<Long, Long> oldNewSegmentId) {
        super(TaskType.MACHINE_GET_SEGMENT_FROM_MACHINE_AND_WRITE_TO_FILE, SynchronizationMode.ASYNCHRONOUS);
        this.mSegmentIds = segmentIds;
        this.mOldNewSegmentId = oldNewSegmentId;
    }

    @Override
    public void perform() {
        communicationManager.sendObject(mSegmentIds);
        for (Long segmentId : mSegmentIds) {
            ServerCommunication.getSegmentDataAndSaveAsFile(communicationManager, mOldNewSegmentId.get(segmentId));
        }
    }
}
