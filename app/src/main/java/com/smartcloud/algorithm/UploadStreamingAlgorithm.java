package com.smartcloud.algorithm;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.MasterSendSegmentDataToSlaveTask;
import com.smartcloud.task.Task;
import com.smartcloud.util.FileManager;
import com.smartcloud.web.NanoHTTPD;
import com.smartcloud.web.WebServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class UploadStreamingAlgorithm extends UploadAlgorithm {
    private static SecureRandom random = new SecureRandom();

    private long mSegmentSize;
    private boolean mUploadToMaster;
    private List<String> usedMachineHoldersIds = new ArrayList<>();

    public UploadStreamingAlgorithm(NanoHTTPD.HTTPSession session) {
        super(session);
        mAlgorithm = Enum.valueOf(Algorithm.class, session.getParms().get("algorithm"));
        mUploadToMaster = Boolean.parseBoolean(session.getParms().get("uploadToMaster"));
        if (mAlgorithm.equals(Algorithm.FIXED__RANDOM_WITHOUT_MEM) || mAlgorithm.equals(Algorithm.FIXED__RANDOM_WITH_MEM)) {
            mSegmentSize = Long.parseLong(session.getParms().get("segmentMaxSize")) * 1024;
        }
    }

    private int multipartHeaderDecode(byte[] buf) {
        String header = new String(buf);
        String patternFilename = "filename=\"";
        String afterFilename = header.substring(header.indexOf(patternFilename) + patternFilename.length());
        String filename = afterFilename.substring(0, afterFilename.indexOf("\""));
        mSession.parms.put("file", filename);
        String patternEnd = "\r\n\r\n";
        return header.indexOf(patternEnd) + patternEnd.length();
    }

    public void perform() throws IOException, NanoHTTPD.ResponseException {
        NanoHTTPD.ContentType contentType = new NanoHTTPD.ContentType(mSession.headers.get("content-type"));
        if (!NanoHTTPD.Method.POST.equals(mSession.method) || !contentType.isMultipart()) {
            return;
        }
        long bodySize = mSession.getBodySize();
        int bufferSize = WebServer.REQUEST_BUFFER_LEN;
        int headerEnd = 46;
        if (bodySize < 2 * bufferSize) {
            bufferSize *= 2;
        }
        byte[] buf = new byte[bufferSize];
        mSession.rlen = mSession.inputStream.read(buf, 0, (int) Math.min(bodySize, buf.length));
        int sizeOfHeaderStart = multipartHeaderDecode(buf);
        final long fileSize = bodySize - sizeOfHeaderStart - headerEnd;
        FileHolder fileHolder = new FileHolder(fileSize);
        fileHolder.setName(mSession.parms.get("file"));
        ServerDatabase.instance.insertFile(fileHolder);
        if (mAlgorithm.equals(Algorithm.FIXED_BY_FILE_SIZE__EVERY)) {
            double sizeForEachMachine = ((double) fileSize) / getAvailableMachines().size();
            mSegmentSize = (int) Math.ceil(sizeForEachMachine);
        }
        long toSend = fileSize;
        int offset = sizeOfHeaderStart;
        int length = mSession.rlen == buf.length ? (mSession.rlen - sizeOfHeaderStart) : (mSession.rlen - sizeOfHeaderStart - headerEnd);
        while (mSession.rlen > 0 && toSend > 0) {
            MachineHolder machineHolder = setup(fileSize);
            SegmentHolder segmentHolder = new SegmentHolder(null, fileHolder.getId(), machineHolder.getId(), fileSize - toSend, Math.min(fileSize - toSend + mSegmentSize, fileSize));
            ServerDatabase.instance.insertSegment(segmentHolder);
            if (machineHolder.isServer()) {
                long size = segmentHolder.getSize();
                FileOutputStream fileOutputStream = null;
                segmentHolder.setPath(FileManager.storageDir + "/" + fileHolder.getId() + "^_^" + segmentHolder.getId());
                ClientDatabase.instance.insertSegment(segmentHolder);
                try {
                    File file = new File(segmentHolder.getPath());
                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file, false);
                    fileOutputStream.write(buf, offset, length);
                    size -= length;
                    while (mSession.rlen > 0 && size > 0) {
                        try {
                            mSession.rlen = mSession.inputStream.read(buf, 0, Math.min((int) size, buf.length));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (mSession.rlen > 0) {
                            size -= mSession.rlen;
                            fileOutputStream.write(buf, 0, mSession.rlen);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Task task = new MasterSendSegmentDataToSlaveTask(buf, offset, length, mSession, segmentHolder);
                    new ClientCommunication(new Socket(InetAddress.getByName(machineHolder.getAddress()), ConnectionServer.SERVER_PORT), task).init();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            toSend = Math.max(toSend - mSegmentSize, 0);
            if (toSend > 0) {
                mSession.rlen = mSession.inputStream.read(buf, 0, (int) Math.min(toSend, buf.length));
                offset = 0;
                length = mSession.rlen == buf.length ? mSession.rlen : mSession.rlen - headerEnd;
            }
        }
        if(bufferSize == WebServer.REQUEST_BUFFER_LEN){
            mSession.rlen = mSession.inputStream.read(buf, 0, (int) (bodySize - sizeOfHeaderStart - fileSize));
        }
    }

    private MachineHolder setup(long fileSize) {
        MachineHolder machineHolder = getMachine();
        if (mAlgorithm.equals(Algorithm.RANDOM__RANDOM_WITHOUT_MEM) || mAlgorithm.equals(Algorithm.RANDOM__RANDOM_WITH_MEM)) {
            mSegmentSize = findRandomSegmentMaxSize();
        } else if (mAlgorithm.equals(Algorithm.BY_CAPACITY__BY_CAPACITY)) {
            mSegmentSize = machineHolder.getFreeSpace().intValue() - 1024;//zapas 1KB
        } else if (mAlgorithm.equals(Algorithm.BY_CAPACITY_AND_FILE_SIZE__EVERY)) {
            long totalFreeSpace = 0;
            for (MachineHolder machine : getAvailableMachines()) {
                totalFreeSpace += machine.getFreeSpace();
            }
            mSegmentSize = (int) Math.ceil((double) fileSize * machineHolder.getFreeSpace() / totalFreeSpace);
        }
        return machineHolder;
    }

    private MachineHolder getMachine() {
        MachineHolder machineHolder = null;
        if (mAlgorithm.equals(Algorithm.FIXED__RANDOM_WITHOUT_MEM) || mAlgorithm.equals(Algorithm.RANDOM__RANDOM_WITHOUT_MEM)) {
            machineHolder = findRandomMachine(false);
        } else if (mAlgorithm.equals(Algorithm.FIXED__RANDOM_WITH_MEM) || mAlgorithm.equals(Algorithm.RANDOM__RANDOM_WITH_MEM) ||
                mAlgorithm.equals(Algorithm.FIXED_BY_FILE_SIZE__EVERY) || mAlgorithm.equals(Algorithm.BY_CAPACITY_AND_FILE_SIZE__EVERY)) {
            machineHolder = findRandomMachine(true);
        } else if (mAlgorithm.equals(Algorithm.BY_CAPACITY__BY_CAPACITY)) {
            for (MachineHolder availableMachine : getAvailableMachines()) {
                if (machineHolder == null || availableMachine.getFreeSpace() > machineHolder.getFreeSpace()) {
                    machineHolder = availableMachine;
                }
            }
        }
        return machineHolder;
    }

    private List<MachineHolder> getAvailableMachines() {
        List<MachineHolder> machines = ServerDatabase.instance.selectMachine(true);
        if (!mUploadToMaster) {
            MachineHolder toRemove = null;
            for (MachineHolder machineHolder : machines) {
                if (machineHolder.isServer()) {
                    toRemove = machineHolder;
                    break;
                }
            }
            machines.remove(toRemove);
        }
        return machines;
    }

    private MachineHolder findRandomMachine(boolean withMemory) {
        List<MachineHolder> machines = getAvailableMachines();
        if (withMemory) {
            if (machines.size() <= usedMachineHoldersIds.size()) {
                usedMachineHoldersIds.clear();
            } else {
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
            }
        }
        MachineHolder machineHolder = machines.get(random.nextInt(machines.size()));
        if (withMemory) {
            usedMachineHoldersIds.add(machineHolder.getId());
        }
        return machineHolder;
    }

    private int findRandomSegmentMaxSize() {
        return Algorithm.sizes[random.nextInt(Algorithm.sizes.length)] * 1024;
    }
}
