package com.smartcloud.communication;

public enum CommunicationTask {
    GET_CLIENT_MACHINE_HOLDER("readClientMachineHolder", "writeClientMachineHolder"),
//    SEND_FILE("readFile", "writeFile"),
    SEND_SEGMENT("readSegment", "writeSegment"),
    SEND_REQUEST_FOR_SEGMENT("responseForRequestForSegment", "sendRequestForSegment"),
    RESPONSE_FOR_SEGMENT("receiveSegment", null);

    String methodNameRead;
    String methodNameWrite;

    CommunicationTask(String methodNameRead, String methodNameWrite) {
        this.methodNameRead = methodNameRead;
        this.methodNameWrite = methodNameWrite;
    }
}
