package com.smartcloud.holder;

import android.util.Log;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudHolder implements Serializable {
    public static InetAddress serverAddress;
    private static Map<String, MachineHolder> machines = new HashMap<>();
    private static Map<FileHolder, FileHolder> files = new HashMap<>();

    public static List<MachineHolder> getClientMachines() {
        List<MachineHolder> machineHolders = new ArrayList<>(machines.size() - 1);
        for (MachineHolder machine : machines.values()) {
            if (!machine.isServer()) {
                machineHolders.add(machine);
            }
        }
        return machineHolders;
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

//    public static void updateFiles(File file) {
//        FileHolder fileHolder = new FileHolder(file);
//
//    }

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
