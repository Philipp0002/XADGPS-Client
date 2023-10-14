package de.raffaelhahn.xadgps_client;

public class Utils {

    public static int getGpsIcon(int gpsSignal) {
        return gpsSignal >= 5 ? R.drawable.satellite_strong : R.drawable.satellite_weak;
    }

    public static int getGsmIcon(int gsmSignal) {
        if(gsmSignal >= 24) {
            return R.drawable.signal4;
        } else if(gsmSignal >= 21) {
            return R.drawable.signal3;
        } else if(gsmSignal >= 18) {
            return R.drawable.signal2;
        } else if(gsmSignal >= 15) {
            return R.drawable.signal1;
        } else {
            return R.drawable.signal0;
        }
    }

    public static int getGpsHint(int gpsSignal) {
        if (gpsSignal >= 5) {
            return R.string.signal_strength_strong;
        } else {
            return R.string.signal_strength_weak;
        }
    }

    public static int getGsmHint(int gsmSignal) {
        if (gsmSignal >= 21) {
            return R.string.signal_strength_strong;
        } else if (gsmSignal >= 15) {
            return R.string.signal_strength_normal;
        } else {
            return R.string.signal_strength_weak;
        }
    }

}
