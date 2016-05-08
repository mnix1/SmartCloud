package com.smartcloud.holder;

import android.util.Log;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    public static InetAddress serverAddress;
    private static Map<String, MachineHolder> machines = new HashMap<>();
    private static Map<Long, List<SegmentHolder>> filesToManage = new HashMap<>();

    public static void addFilesToManage(Long fileId, List<SegmentHolder> segments) {
        filesToManage.put(fileId, segments);
    }

    public static void updateFilesToManage(Long fileId, Long segmentId, boolean ready) {
        for (SegmentHolder segmentHolder : filesToManage.get(fileId)) {
            if (segmentHolder.getId().equals(segmentId)) {
                synchronized (segmentHolder) {
                    segmentHolder.setReady(ready);
                }
            }
        }
    }

    public static boolean isFileReady(Long fileId) {
        for (SegmentHolder segmentHolder : filesToManage.get(fileId)) {
            if (!segmentHolder.isReady()) {
                return false;
            }
        }
        return true;
    }

    public static List<SegmentHolder> getSegmentsOfFileToManage(Long fileId) {
        List<SegmentHolder> segments = filesToManage.get(fileId);
        Collections.sort(segments, new Comparator<SegmentHolder>() {
            @Override
            public int compare(SegmentHolder lhs, SegmentHolder rhs) {
                return lhs.getByteFrom().compareTo(rhs.getByteFrom());
            }
        });
        return segments;
    }

    public static List<MachineHolder> getMachines() {
        return new ArrayList<MachineHolder>(machines.values());
    }

    public static List<MachineHolder> getClientMachines() {
        List<MachineHolder> machines = new ArrayList<>(CloudHolder.machines.size() - 1);
        for (MachineHolder machine : CloudHolder.machines.values()) {
            if (!machine.isServer()) {
                machines.add(machine);
            }
        }
        return machines;
    }

    public static MachineHolder getMachine(String id) {
        return machines.get(id);
    }

    public static void updateMachines(MachineHolder machineHolder) {
        if (machines.containsKey(machineHolder)) {
            machines.remove(machineHolder);
        }
        machines.put(machineHolder.getId(), machineHolder);
    }

    public static long freeSpace() {
        long freeSpace = 0;
        for (MachineHolder machine : machines.values()) {
            freeSpace += machine.getFreeSpace();
        }
        return freeSpace;
    }

    public static void log() {
        StringBuilder stringBuilder = new StringBuilder("freeSpace: " + freeSpace());
        stringBuilder.append("\n");
        for (MachineHolder machine : machines.values()) {
            stringBuilder.append(machine.toString());
            stringBuilder.append("\n");
        }
        Log.v("SMART CLOUD", stringBuilder.toString());
    }
}
