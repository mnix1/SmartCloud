package com.smartcloud.communication;

import com.smartcloud.holder.MachineHolder;
import com.smartcloud.task.TaskType;

import java.net.Socket;

public class MasterServerCommunication extends ServerCommunication {

    public MasterServerCommunication(Socket socket) {
        super(socket);
    }

    @Override
    public void taskExecution(String taskName) {
        TaskType task = TaskType.findAction(taskName);
        switch (task) {
            case SLAVE_SEND_MACHINE_HOLDER_TO_MASTER:
                sendMachineHolder();
                break;
            default:
                break;
        }
    }

    void sendMachineHolder() {
        MachineHolder slaveMachineHolder = (MachineHolder) receiveObject();
        MachineHolder.updateFromSlave(slaveMachineHolder, mSocket.getInetAddress().getHostAddress());
    }
}
