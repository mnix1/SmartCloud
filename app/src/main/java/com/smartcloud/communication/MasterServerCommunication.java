package com.smartcloud.communication;

import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.TaskType;

import java.net.Socket;
import java.util.List;

public class MasterServerCommunication extends ServerCommunication {

    public MasterServerCommunication(Socket socket) {
        super(socket);
    }

    @Override
    public void taskExecution(String taskName) {
        TaskType task = TaskType.findAction(taskName);
        switch (task) {
            case SLAVE_SEND_MACHINE_HOLDER_TO_MASTER:
                receiveSlaveMachineHolder();
                break;
            case SLAVE_REQUEST_ACTIVE_SLAVE_SEGMENT_TO_MASTER:
                getMachineHolderAndSendActiveSegments();
                break;
            default:
                break;
        }
    }

    void getMachineHolderAndSendActiveSegments() {
        MachineHolder slaveMachineHolder = (MachineHolder) receiveObject();
        sendObject(ServerDatabase.instance.selectSegment(slaveMachineHolder.getId()));
        List<SegmentHolder> segmentsNotActive = (List<SegmentHolder>) receiveObject();
        ServerDatabase.instance.deleteSegments(segmentsNotActive);
    }

    void receiveSlaveMachineHolder() {
        MachineHolder slaveMachineHolder = (MachineHolder) receiveObject();
        MachineHolder.updateFromSlave(slaveMachineHolder, mSocket.getInetAddress().getHostAddress());
    }
}
