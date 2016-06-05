package com.smartcloud.communication;

import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.MachineGetSegmentFromMachineAndWriteToFileTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public abstract class ServerCommunication extends CommunicationManager implements Runnable {

    public ServerCommunication(Socket socket) {
        super(socket);
    }

    void initThread() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && mSocket.isConnected()) {
                    System.out.println("ServerCommunication mThread begin loop");
                    String taskName = null;
                    try {
                        taskName = mInput.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (taskName == null) {
                        break;
                    }
                    System.out.println("ServerCommunication mThread end loop " + taskName);
                    taskExecution(taskName);
                }
            }
        });
    }

    public abstract void taskExecution(String taskName);

    @Override
    public void run() {
        initThread();
        mThread.start();
    }

    public static void requestAndGetSegmentData(Map<String, List<Long>> machineAddressListSegmentIds, Map<Long, Long> oldNewSegmentId) {
        for (String address : machineAddressListSegmentIds.keySet()) {
            MachineGetSegmentFromMachineAndWriteToFileTask task = new MachineGetSegmentFromMachineAndWriteToFileTask(machineAddressListSegmentIds.get(address), oldNewSegmentId);
            try {
                if (address == null) {
                    new ClientCommunication(task).init();
                } else {
                    new ClientCommunication(new Socket(InetAddress.getByName(address), ConnectionServer.SERVER_PORT), task).init();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static SegmentHolder getSegmentDataAndSaveAsFile(CommunicationManager communicationManager, Long segmentId) {
        SegmentHolder segmentHolder = communicationManager.receiveSegmentDataAndSaveAsFile(segmentId);
        if (segmentHolder != null) {
            ClientDatabase.instance.insertSegment(segmentHolder);
        }
        return segmentHolder;
    }
}
