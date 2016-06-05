package com.smartcloud.task;

import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.util.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SlaveRequestActiveSlaveSegmentToMasterTask extends Task {

    public SlaveRequestActiveSlaveSegmentToMasterTask() {
        super(TaskType.SLAVE_REQUEST_ACTIVE_SLAVE_SEGMENT_TO_MASTER, SynchronizationMode.ASYNCHRONOUS);
    }

    @Override
    public void perform() {
        communicationManager.sendObject(ClientDatabase.instance.selectMachine());
        List<SegmentHolder> segmentsFromMaster = (List<SegmentHolder>) communicationManager.receiveObject();
        communicationManager.sendObject(FileManager.checkConsistency(segmentsFromMaster));
    }
}
