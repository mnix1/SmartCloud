package com.smartcloud.communication;

import android.content.Context;

import com.smartcloud.constant.MethodType;
import com.smartcloud.network.NetworkManager;

import java.io.IOException;
import java.net.Socket;

public class ClientCommunication extends CommunicationManager {
    private ClientCommunication instance = null;

    public ClientCommunication(Socket socket, Context context) {
        super(socket, context);
        this.instance = this;
    }

    void initReadThread() {
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && mSocket.isConnected()) {
                    System.out.println("ClientCommunication readThread");
                    String message = null;
                    try {
                        message = mInput.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            mSocket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        NetworkManager.createAp = false;
                        NetworkManager.init(mContext);
                        break;
                    }
                    if (message == null || message.isEmpty() || message.equals(" ")) {
                        try {
                            mSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        NetworkManager.createAp = false;
                        NetworkManager.init(mContext);
                        break;
                    }
                    CommunicationTaskHolder.findAction(message, instance);
                    System.out.println("ClientCommunication readThread end loop " + message);
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
                    while (!tasks.isEmpty()) {
                        tasks.get(0).findAction(MethodType.WRITE, instance);
                        tasks.remove(0);
                    }
                }
            }
        });
        writeThread.start();
    }

    @Override
    public void run() {
        tasks.add(new CommunicationTaskHolder(CommunicationTask.GET_CLIENT_MACHINE_HOLDER));
        initReadThread();
        initWriteThread();
    }

}
