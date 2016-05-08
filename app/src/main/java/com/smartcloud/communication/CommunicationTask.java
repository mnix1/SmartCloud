package com.smartcloud.communication;

public enum CommunicationTask {
    GET_CLIENT_MACHINE_HOLDER("readClientMachineHolder", "writeClientMachineHolder"),
    //    SEND_FILE("readFile", "writeFile"),
    SEND_SEGMENT("readSegment", "writeSegment"),
    REQUEST_FOR_SEND_SEGMENT("responseForRequestForSendSegment", "requestForSendSegment"),
    RESPONSE_FOR_SEND_SEGMENT("receiveSegment", null),
    REQUEST_FOR_DELETE_SEGMENT("responseForRequestForDeleteSegment", "requestForDeleteSegment");

    String methodNameRead;
    String methodNameWrite;

    CommunicationTask(String methodNameRead, String methodNameWrite) {
        this.methodNameRead = methodNameRead;
        this.methodNameWrite = methodNameWrite;
    }
}
