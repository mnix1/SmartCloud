package com.smartcloud.connection;

import android.util.Log;

import com.smartcloud.communication.CommunicationManager;
import com.smartcloud.communication.MasterServerCommunication;
import com.smartcloud.communication.ServerCommunication;
import com.smartcloud.communication.SlaveServerCommunication;
import com.smartcloud.database.ClientDatabase;

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
                ServerCommunication serverCommunication = null;
                if (ClientDatabase.instance.selectMachine().isServer()) {
                    serverCommunication = new MasterServerCommunication(socket);
                } else {
                    serverCommunication = new SlaveServerCommunication(socket);
                }
                new Thread(serverCommunication).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
