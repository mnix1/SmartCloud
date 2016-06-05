package com.smartcloud.task;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.MachineHolder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterGetMachineHolderFromSlaveTask extends Task {
    private static final int LAST_CONTACT_TRESHHOLD = 100;
    private static final int NOT_RESPONDING_TRESHHOLD = 1000;

    private MachineHolder mSlaveMachineHolder;

    public MasterGetMachineHolderFromSlaveTask() {
        super(TaskType.MASTER_GET_MACHINE_HOLDER_FROM_SLAVE, SynchronizationMode.ASYNCHRONOUS);
    }

    public static void updateMachines() {
        Date now = new Date();
        Map<MachineHolder, MasterGetMachineHolderFromSlaveTask> machineHolderTasks = new HashMap<MachineHolder, MasterGetMachineHolderFromSlaveTask>();
        for (MachineHolder machineHolder : ServerDatabase.instance.selectMachine()) {
            if (now.getTime() - machineHolder.getLastContact().getTime() <= LAST_CONTACT_TRESHHOLD) {
                continue;
            }
            if (machineHolder.isServer()) {
                machineHolder = ClientDatabase.instance.selectMachine();
                ServerDatabase.instance.updateMachine(machineHolder);
            } else {
                if (machineHolder.getAddress() == null) {
                    continue;
                }
                MasterGetMachineHolderFromSlaveTask task = null;
                try {
                    task = new MasterGetMachineHolderFromSlaveTask();
                    machineHolderTasks.put(machineHolder, task);
                    new ClientCommunication(new Socket(InetAddress.getByName(machineHolder.getAddress()), ConnectionServer.SERVER_PORT), task).init();
                } catch (IOException e) {
                    machineHolderTasks.remove(task);
                    machineHolder.setActive(false);
                    ServerDatabase.instance.updateMachine(machineHolder);
                    e.printStackTrace();
                }
            }
        }
        int i = 0;
        List<MachineHolder> machines = new ArrayList<>(machineHolderTasks.keySet());
        while (i < machines.size()) {
            MachineHolder machineHolder = machineHolderTasks.get(machines.get(i)).getSlaveMachineHolder();
            if (machineHolder != null) {
                ServerDatabase.instance.updateMachine(machineHolder);
                i++;
            } else {
                try {
                    if (new Date().getTime() - now.getTime() > NOT_RESPONDING_TRESHHOLD) {
                        machineHolder = machines.get(i);
                        machineHolder.setActive(false);
                        ServerDatabase.instance.updateMachine(machineHolder);
                        i++;
                    } else {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void perform() {
        mSlaveMachineHolder = (MachineHolder) communicationManager.receiveObject();
        MachineHolder.updateFromSlave(mSlaveMachineHolder, communicationManager.getSocket().getInetAddress().getHostAddress());
    }

    public MachineHolder getSlaveMachineHolder() {
        return mSlaveMachineHolder;
    }
}
