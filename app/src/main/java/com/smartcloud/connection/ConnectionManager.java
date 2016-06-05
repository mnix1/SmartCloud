package com.smartcloud.connection;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.constant.MachineRole;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.network.NetworkInitializedListener;
import com.smartcloud.task.SlaveRequestActiveSlaveSegmentToMasterTask;
import com.smartcloud.task.SlaveSendMachineHolderToMasterTask;
import com.smartcloud.util.FileManager;
import com.smartcloud.web.WebServer;

public class ConnectionManager implements NetworkInitializedListener {
    private WebServer mWebServer;
    private Thread mServerThread;


    private void init() {
        if (ClientDatabase.instance.selectMachine().isServer()) {
            startServer();
        } else {
            startClient();
        }
    }

    private void startServer() {
        ServerDatabase.init();
//        ServerDatabase.instance.deleteMachine(null);
        MachineHolder machineHolder = ClientDatabase.instance.selectMachine();
        ServerDatabase.instance.updateMachine(machineHolder);
        ConnectionServer connectionServer = new ConnectionServer();
        mServerThread = new Thread(connectionServer);
        mServerThread.start();
        mWebServer = new WebServer();
        mWebServer.start();
        FileManager.checkConsistency();
        ServerDatabase.instance.deleteSegments(FileManager.checkConsistency(ServerDatabase.instance.selectSegment(machineHolder.getId())));
    }

    private void startClient() {
        try {
            ConnectionServer connectionServer = new ConnectionServer();
            mServerThread = new Thread(connectionServer);
            mServerThread.start();
            new ClientCommunication(new SlaveSendMachineHolderToMasterTask()).init();
            new ClientCommunication(new SlaveRequestActiveSlaveSegmentToMasterTask()).init();
            FileManager.checkConsistency();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialized(MachineRole machineRole) {
        MachineHolder machineHolder = ClientDatabase.instance.selectMachine();
        machineHolder.setMachineRole(machineRole);
        ClientDatabase.instance.updateMachine(machineHolder);
        init();
    }
}
