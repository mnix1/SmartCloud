package com.smartcloud.communication;

import com.smartcloud.constant.MethodType;

import java.io.IOException;
import java.net.Socket;

public class ServerCommunication extends CommunicationManager {
    ServerCommunication instance = null;

    public ServerCommunication(Socket socket) {
        super(socket);
        instance = this;
    }

    void initReadThread() {
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && mSocket.isConnected()) {
                    String message = null;
                    try {
                        message = mInput.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (message == null) {
                        break;
                    }
                    System.out.println("ServerCommunication readThread " + message);
                    CommunicationTaskHolder.findAction(message, instance);
                }
            }
        });
        readThread.start();
    }

    void initWriteThread() {
        writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && mSocket.isConnected()) {
                    synchronized (tasks) {
                        while (!tasks.isEmpty()) {
                            System.out.println("ServerCommunication writeThread isEmpty");
                            CommunicationTaskHolder taskHolder = tasks.get(0);
                            taskHolder.findAction(MethodType.WRITE, instance);
                            tasks.remove(0);
                            System.out.println("ServerCommunication writeThread isEmpty end " + taskHolder.task);
                        }
                    }
                }
                System.out.println("ServerCommunication END");
            }
        });
        writeThread.start();
    }

    @Override
    public void run() {
        initReadThread();
        initWriteThread();
    }

}
