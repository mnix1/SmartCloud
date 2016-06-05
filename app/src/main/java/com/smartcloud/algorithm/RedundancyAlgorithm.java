package com.smartcloud.algorithm;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.communication.ServerCommunication;
import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.constant.Redundancy;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.MasterRequestSlaveGetSegmentTask;
import com.smartcloud.task.Task;
import com.smartcloud.util.Util;
import com.smartcloud.web.NanoHTTPD;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedundancyAlgorithm {
    protected NanoHTTPD.HTTPSession mSession;
    protected Redundancy mRedundancy;
    private boolean mUploadToMaster;
    private Set<String> usedMachineHoldersIds = new HashSet<>();

    public RedundancyAlgorithm(NanoHTTPD.HTTPSession session) {
        this.mSession = session;
        this.mRedundancy = Enum.valueOf(Redundancy.class, session.getParms().get("redundancy"));
        this.mUploadToMaster = Boolean.parseBoolean(session.getParms().get("uploadToMaster"));
    }

    public void perform() {
        if (mRedundancy.equals(Redundancy.NONE)) {
            return;
        }
        List<MachineHolder> machines = ServerDatabase.instance.getAvailableMachines(mUploadToMaster);
        if (machines.size() <= 1) {
            return;
        }
        FileHolder fileHolder = ServerDatabase.instance.selectFile(mSession.parms.get("file"));
        List<SegmentHolder> segments = segmentsToDuplicate(fileHolder.getId());
        Map<String, List<SegmentHolder>> machineSegmentIds = new HashMap<>();
        for (SegmentHolder segmentHolder : segments) {
            machines = getAvailableMachines();
            usedMachineHoldersIds.add(segmentHolder.getMachineId());
            if (!mRedundancy.memory || machines.size() <= usedMachineHoldersIds.size()) {
                usedMachineHoldersIds.clear();
                usedMachineHoldersIds.add(segmentHolder.getMachineId());
            }
            MachineHolder machineHolder = findRandomMachine(machines);
            usedMachineHoldersIds.add(machineHolder.getId());
            if (machineSegmentIds.containsKey(machineHolder.getId())) {
                machineSegmentIds.get(machineHolder.getId()).add(segmentHolder);
            } else {
                List<SegmentHolder> segmentHolders = new ArrayList<>();
                segmentHolders.add(segmentHolder);
                machineSegmentIds.put(machineHolder.getId(), segmentHolders);
            }
        }
        for (String machineId : machineSegmentIds.keySet()) {
            Map<String, List<Long>> machineAddressListSegmentIds = new HashMap<>();
            Map<Long, Long> oldNewSegmentId = new HashMap<>();
            for (SegmentHolder segmentHolder : machineSegmentIds.get(machineId)) {
                Long oldSegmentId = segmentHolder.getId();
                MachineHolder machineHolder = ServerDatabase.instance.selectMachine(segmentHolder.getMachineId());
                if (machineAddressListSegmentIds.containsKey(machineHolder.getAddress())) {
                    machineAddressListSegmentIds.get(machineHolder.getAddress()).add(oldSegmentId);
                } else {
                    List<Long> segmentIds = new ArrayList<>();
                    segmentIds.add(oldSegmentId);
                    machineAddressListSegmentIds.put(machineHolder.getAddress(), segmentIds);
                }
                segmentHolder.setMachineId(machineId);
                ServerDatabase.instance.insertSegment(segmentHolder);
                oldNewSegmentId.put(oldSegmentId, segmentHolder.getId());
            }
            MachineHolder machineHolder = ServerDatabase.instance.selectMachine(machineId);
            if (machineHolder.isServer()) {
                ServerCommunication.requestAndGetSegmentData(machineAddressListSegmentIds, oldNewSegmentId);
            } else {
                Task task = new MasterRequestSlaveGetSegmentTask(machineAddressListSegmentIds, oldNewSegmentId);
                try {
                    new ClientCommunication(new Socket(InetAddress.getByName(machineHolder.getAddress()), ConnectionServer.SERVER_PORT), task).init();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<SegmentHolder> segmentsToDuplicate(Long fileId) {
        List<SegmentHolder> segments = ServerDatabase.instance.selectSegment(fileId);
        int segmentsToDuplicate = mRedundancy.percent == null ? segments.size() : (segments.size() * mRedundancy.percent / 100);
        int segmentsNotToDuplicate = segments.size() - segmentsToDuplicate;
        for (int i = 0; i < segmentsNotToDuplicate; i++) {
            int index = Util.RANDOM.nextInt(segments.size());
            segments.remove(index);
        }
        return segments;
    }

    private List<MachineHolder> getAvailableMachines() {
        return ServerDatabase.instance.getAvailableMachines(mUploadToMaster);
    }

    private MachineHolder findRandomMachine(List<MachineHolder> machines) {
        for (String usedMachineId : usedMachineHoldersIds) {
            int indexToRemove = 0;
            for (MachineHolder machineHolder : machines) {
                if (machineHolder.getId().equals(usedMachineId)) {
                    break;
                }
                indexToRemove++;
            }
            machines.remove(indexToRemove);
        }
        if (machines.size() == 0) {
            return null;
        }
        return machines.get(Util.RANDOM.nextInt(machines.size()));

    }
}
