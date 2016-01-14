package com.smartcloud.connection;

import android.content.Context;

import com.smartcloud.database.Database;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.CloudHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.communication.CommunicationManager;
import com.smartcloud.constant.MachineRole;
import com.smartcloud.network.NetworkInitializedListener;
import com.smartcloud.network.NetworkManager;
import com.smartcloud.util.NetworkHelper;
import com.smartcloud.web.WebServer;

import java.net.Socket;

public class ConnectionManager implements NetworkInitializedListener {
    private final Context mContext;
    private WebServer mWebServer;
    private Database mDatabase;
    private Thread mServerThread;
    private Thread mClientThread;

    public ConnectionManager(Context context) {
        this.mContext = context;
    }

    private void init() {
//        WifiManager wifiManager = NetworkHelper.getWifiManager(mContext);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//        Log.v("C WifiInfo", wifiInfo.toString());
//        Log.v("CM DhcpInfo", dhcpInfo.toString());
        if (MachineHolder.ME.isServer()) {
            startServer();
        } else {
            startClient();
        }
    }

    private void startServer() {
        ServerDatabase.init(mContext);
        CloudHolder.updateMachines(MachineHolder.ME);
        ConnectionServer connectionServer = new ConnectionServer();
        mServerThread = new Thread(connectionServer);
        mServerThread.start();
        mWebServer = new WebServer();
        mWebServer.start();
    }

    private void startClient() {
        try {
            NetworkHelper.setServerAddress(mContext);
            CommunicationManager communicationManager = new ClientCommunication(new Socket(CloudHolder.serverAddress, ConnectionServer.SERVER_PORT));
            mClientThread = new Thread(communicationManager);
            mClientThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            NetworkManager.init(mContext);
        }
    }

    @Override
    public void initialized(MachineRole machineRole) {
        MachineHolder.ME.setMachineRole(machineRole);
        init();
    }
}
