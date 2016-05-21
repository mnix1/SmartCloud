package com.smartcloud.task;

import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.database.ClientDatabase;

public class SlaveSendMachineHolderToMasterTask extends Task {

    public SlaveSendMachineHolderToMasterTask() {
        super(TaskType.SLAVE_SEND_MACHINE_HOLDER_TO_MASTER, SynchronizationMode.ASYNCHRONOUS);
    }

    @Override
    public void perform() {
        communicationManager.sendObject(ClientDatabase.instance.selectMachine());
    }
}
