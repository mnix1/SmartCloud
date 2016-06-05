package com.smartcloud.communication;

import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.TaskType;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

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
                SegmentHolder segmentHolder = receiveSegmentDataAndSaveAsFile();
                if (segmentHolder != null) {
                    ClientDatabase.instance.insertSegment(segmentHolder);
                }
                break;
            case MASTER_GET_SEGMENT_DATA_FROM_SLAVE_AND_WRITE_TO_STREAM:
                getSegmentData();
                break;
            case MASTER_REQUEST_DELETE_SEGMENT_TO_SLAVE:
                deleteSegments();
                break;
            default:
                break;
        }
    }

    void getSegmentData() {
        try {
            Long segmentId = Long.parseLong(mInput.readLine());
            sendSegmentFromFile(ClientDatabase.instance.selectSegment(segmentId));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
