package com.smartcloud.communication;

import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.TaskType;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class SlaveServerCommunication extends ServerCommunication {

    public SlaveServerCommunication(Socket socket) {
        super(socket);
    }

    @Override
    public void taskExecution(String taskName) {
        TaskType task = TaskType.findAction(taskName);
        switch (task) {
            case MASTER_GET_MACHINE_HOLDER_FROM_SLAVE:
                sendObject(ClientDatabase.instance.selectMachine());
                break;
            case MASTER_SEND_SEGMENT_DATA_TO_SLAVE:
                getSegmentDataAndSaveAsFile(this, null);
                break;
            case MASTER_GET_SEGMENT_DATA_FROM_SLAVE_AND_WRITE_TO_STREAM:
                sendSegmentData();
                break;
            case MASTER_REQUEST_DELETE_SEGMENT_TO_SLAVE:
                deleteSegments();
                break;
            case MASTER_REQUEST_SLAVE_GET_SEGMENT:
                requestAndGetSegmentData();
                break;
            case MACHINE_GET_SEGMENT_FROM_MACHINE_AND_WRITE_TO_FILE:
                List<Long> mSegmentIds = (List<Long>) receiveObject();
                for (Long segmentId : mSegmentIds) {
                    sendSegmentFromFile(ClientDatabase.instance.selectSegment(segmentId));
                }
                break;
            default:
                break;
        }
    }

    void requestAndGetSegmentData() {
        Map<String, List<Long>> machineAddressListSegmentIds = (Map<String, List<Long>>) receiveObject();
        Map<Long, Long> oldNewSegmentId = (Map<Long, Long>) receiveObject();
        requestAndGetSegmentData(machineAddressListSegmentIds, oldNewSegmentId);
    }

    void deleteSegments() {
        List<Long> segmentIds = (List<Long>) receiveObject();
        for (Long segmentId : segmentIds) {
            SegmentHolder segmentHolder = ClientDatabase.instance.selectSegment(segmentId);
            if (segmentHolder == null) {
                return;
            }
            ClientDatabase.instance.deleteSegment(segmentHolder);
            new File(segmentHolder.getPath()).delete();
        }
    }
    public void sendSegmentData() {
        try {
            Long segmentId = Long.parseLong(mInput.readLine());
            sendSegmentFromFile(ClientDatabase.instance.selectSegment(segmentId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
