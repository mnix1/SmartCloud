package com.smartcloud.network;

import com.smartcloud.constant.MachineRole;

public interface NetworkInitializedListener {
    public void initialized(MachineRole machineRole);
}
