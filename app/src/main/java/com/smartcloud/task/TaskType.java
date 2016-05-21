package com.smartcloud.task;

public enum TaskType {
    MASTER_GET_MACHINE_HOLDER_FROM_SLAVE,
    SLAVE_SEND_MACHINE_HOLDER_TO_MASTER,

    MASTER_SEND_SEGMENT_DATA_TO_SLAVE,
    MASTER_REQUEST_DELETE_SEGMENT_TO_SLAVE,

    MASTER_GET_SEGMENT_DATA_FROM_SLAVE_AND_WRITE_TO_STREAM;
//    GET_CLIENT_MACHINE_HOLDER("readClientMachineHolder", "writeClientMachineHolder"),
//    //    SEND_FILE("readFile", "writeFile"),
//    SEND_SEGMENT_DATA("receiveSegmentDataAndSaveAsFile", "sendSegmentData"),
//    SEND_SEGMENT("receiveSegmentDataAndSaveAsFile", "writeSegment"),
//    REQUEST_FOR_SEND_SEGMENT_DATA("responseForRequestForSendSegment", "requestForSendSegment"),
//    RESPONSE_FOR_SEND_SEGMENT_DATA(null, null),
//    REQUEST_FOR_DELETE_SEGMENT("responseForRequestForDeleteSegment", "requestForDeleteSegment");


    public static TaskType findAction(String taskName) {
        TaskType task = null;
        try {
            task = Enum.valueOf(TaskType.class, taskName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return task;
    }
}
