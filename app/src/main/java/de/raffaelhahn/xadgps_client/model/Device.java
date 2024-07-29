package de.raffaelhahn.xadgps_client.model;

import android.content.Context;

import de.raffaelhahn.xadgps_client.R;

public class Device extends NotifyDevice {
    public String sn;
    public String groupID;
    public String groupName;
    public String car;
    public String status;
    public String model;
    public String iccid;
    public String icon;
    public String iconid;
    public String StopTime;
    public String iconurl;
    public String course;
    public String coursedesc;
    public String acc;
    public String type;
    public String headImage;
    public String Fortification;
    public String style;
    public String lastCommunication;
    public String speed;
    public String isStop;
    public String distance;
    public String GPS;
    public String GSM;

    public String[] getDeviceInfoShort(Context context) {
        String[] description = new String[]{"?", ""};
        if(isStop != null) {
            if ("1".equalsIgnoreCase(isStop)) {
                description[0] = context.getString(R.string.device_parked);
                if(StopTime != null) {
                    description[1] = StopTime;
                }
            } else {
                description[0] = context.getString(R.string.device_moving);
                if (speed != null) {
                    description[1]= speed + " km/h";
                }
            }
        }
        return description;
    }
}
