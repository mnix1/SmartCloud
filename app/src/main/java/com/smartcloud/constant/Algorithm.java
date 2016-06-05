package com.smartcloud.constant;

public enum Algorithm {
    FIXED__RANDOM_WITHOUT_MEM("", "Stały, nie większy niż wybrany", "Losowa bez pamięci"),
    FIXED__RANDOM_WITH_MEM("", "Stały, nie większy niż wybrany", "Losowa z pamięcią"),
    RANDOM__RANDOM_WITHOUT_MEM("", "Losowy", "Losowa bez pamięci"),
    RANDOM__RANDOM_WITH_MEM("", "Losowy", "Losowa z pamięcią"),
    FIXED_BY_FILE_SIZE__EVERY("", "Stały, zależny od rozmiaru pliku: ROZMIAR PLIKU / ILOŚĆ MASZYN", "Każda maszyna"),
    BY_CAPACITY_AND_FILE_SIZE__EVERY("", "Zmienny, zależny od pojemności i rozmiaru pliku: ROZMIAR PLIKU  * POJEMNOŚĆ MASZYNY / POJEMNOŚĆ CHMURY", "Każda maszyna"),
    BY_CAPACITY__BY_CAPACITY("", "Zmienny, zależny od pojemności: MIN(POJEMNOŚĆ MASZYNY, ROZMIAR PLIKU)", "Według pojemności (od największej dostępnej)");
    public static int[] sizes = new int[]{64, 128, 256, 512, 1024, 2048, 4096, 8192};
    public String description;
    public String segmentDescription;
    public String distributionDescription;

    Algorithm(String description, String segmentDescription, String distributionDescription) {
        this.description = description;
        this.segmentDescription = segmentDescription;
        this.distributionDescription = distributionDescription;
    }
}
