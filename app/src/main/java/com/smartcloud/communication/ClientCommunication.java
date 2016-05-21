package com.smartcloud.communication;

import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.task.Task;
import com.smartcloud.constant.SynchronizationMode;
import com.smartcloud.util.NetworkHelper;

import java.io.IOException;
import java.net.Socket;

public class ClientCommunication extends CommunicationManager {

    protected Task mTask;

    public ClientCommunication(Socket socket, Task task) {
        super(socket);
        this.mTask = task;
        task.setCommunicationManager(this);
    }

    public ClientCommunication(Task task) throws IOException {
        this(new Socket(NetworkHelper.getMasterAddress(), ConnectionServer.SERVER_PORT), task);
    }


    public void init() {
        if (mTask.getSynchronizationMode() == SynchronizationMode.ASYNCHRONOUS) {
            initThread();
            mThread.start();
        } else if (mTask.getSynchronizationMode() == SynchronizationMode.SYNCHRONOUS) {
            mOutput.println(mTask.getTaskType());
            mTask.perform();
        }
    }

    private void initThread() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mOutput.println(mTask.getTaskType());
                mTask.perform();
            }
        });
    }

}
