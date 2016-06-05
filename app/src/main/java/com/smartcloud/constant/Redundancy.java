package com.smartcloud.constant;

public enum Redundancy {
    NONE,
    LOW__RANDOM_WITHOUT_MEM(25, false),
    MEDIUM__RANDOM_WITHOUT_MEM(50, false),
    HIGH__RANDOM_WITHOUT_MEM(100, false),
    FULL__RANDOM_WITHOUT_MEM(false),
    LOW__RANDOM_WITH_MEM(25, true),
    MEDIUM__RANDOM_WITH_MEM(50, true),
    HIGH__RANDOM_WITH_MEM(100, true),
    FULL__RANDOM_WITH_MEM(true);

    public Integer percent;
    public Boolean memory;

    private Redundancy() {
    }

    private Redundancy(int percent, boolean memory) {
        this.percent = percent;
        this.memory = memory;
    }

    private Redundancy(boolean memory) {
        this.memory = memory;
    }
}
