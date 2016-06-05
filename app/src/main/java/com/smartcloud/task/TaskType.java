package com.smartcloud.task;

public enum TaskType {
    MASTER_GET_MACHINE_HOLDER_FROM_SLAVE,
    SLAVE_SEND_MACHINE_HOLDER_TO_MASTER,

    SLAVE_REQUEST_ACTIVE_SLAVE_SEGMENT_TO_MASTER,

    MASTER_SEND_SEGMENT_DATA_TO_SLAVE,
    MASTER_REQUEST_DELETE_SEGMENT_TO_SLAVE,

    MASTER_GET_SEGMENT_DATA_FROM_SLAVE_AND_WRITE_TO_STREAM,

    MASTER_REQUEST_SLAVE_GET_SEGMENT,

    MACHINE_GET_SEGMENT_FROM_MACHINE_AND_WRITE_TO_FILE;

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
