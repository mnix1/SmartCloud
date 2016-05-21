package com.smartcloud.communication;

import java.io.IOException;
import java.net.Socket;

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

}
