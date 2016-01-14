package com.smartcloud.connection;

import android.util.Log;

import com.smartcloud.communication.CommunicationManager;
import com.smartcloud.communication.ServerCommunication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionServer implements Runnable {
    public static final int SERVER_PORT = 6000;

    private ServerSocket mServerSocket;

    @Override
    public void run() {
        try {
            mServerSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = mServerSocket.accept();
                Log.v("ConnectionServer", "new host connected: " + socket.getInetAddress());
                CommunicationManager communicationManager = new ServerCommunication(socket);
                new Thread(communicationManager).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
