package com.smartcloud.task;

import com.smartcloud.constant.SynchronizationMode;

import java.util.List;
import java.util.Map;

public class MasterRequestSlaveGetSegmentTask extends Task {
    private Map<String, List<Long>> mMachineAddressListSegmentIds;
    private Map<Long, Long> mOldNewSegmentId;

    public MasterRequestSlaveGetSegmentTask(Map<String, List<Long>> machineAddressListSegmentIds, Map<Long, Long> oldNewSegmentId) {
        super(TaskType.MASTER_REQUEST_SLAVE_GET_SEGMENT, SynchronizationMode.ASYNCHRONOUS);
        this.mMachineAddressListSegmentIds = machineAddressListSegmentIds;
        this.mOldNewSegmentId = oldNewSegmentId;
    }

    @Override
    public void perform() {
        communicationManager.sendObject(mMachineAddressListSegmentIds);
        communicationManager.sendObject(mOldNewSegmentId);
    }
}
