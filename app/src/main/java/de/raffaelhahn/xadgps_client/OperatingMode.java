package de.raffaelhahn.xadgps_client;

public enum OperatingMode {

    USER("0"),
    DEVICE("1"),
    UNKNOWN("3");

    public final String numericString;
    OperatingMode(String numericString) {
        this.numericString = numericString;
    }

    public static OperatingMode fromNumericString(String value) {
        for (OperatingMode mode : OperatingMode.values()) {
            if (mode.numericString.equals(value)) {
                return mode;
            }
        }
        return OperatingMode.UNKNOWN;
    }
}
