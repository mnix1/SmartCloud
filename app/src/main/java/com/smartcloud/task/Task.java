package com.smartcloud.task;

import com.smartcloud.communication.CommunicationManager;
import com.smartcloud.constant.SynchronizationMode;

public abstract class Task {
    protected TaskType taskType;
    protected SynchronizationMode synchronizationMode;
    protected CommunicationManager communicationManager;

    public Task(TaskType taskType, SynchronizationMode synchronizationMode) {
        this.taskType = taskType;
        this.synchronizationMode = synchronizationMode;
    }

    public abstract void perform();

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public SynchronizationMode getSynchronizationMode() {
        return synchronizationMode;
    }

    public void setSynchronizationMode(SynchronizationMode synchronizationMode) {
        this.synchronizationMode = synchronizationMode;
    }

    public CommunicationManager getCommunicationManager() {
        return communicationManager;
    }

    public void setCommunicationManager(CommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
    }
}
