package de.raffaelhahn.xadgps_client.services;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.raffaelhahn.xadgps_client.Device;
import de.raffaelhahn.xadgps_client.async.AsyncCallback;
import de.raffaelhahn.xadgps_client.async.Constants;
import de.raffaelhahn.xadgps_client.async.GetDeviceListAsync;
import de.raffaelhahn.xadgps_client.async.GetDeviceTrackingAsync;

public class DeviceListService {
    private ArrayList<Device> deviceList;
    private Context context;
    private Timer deviceListUpdateTimer;
    private ArrayList<DeviceListUpdateListener> deviceListUpdateListeners;

    public DeviceListService(Context context) {
        this.deviceList = new ArrayList<>();
        this.context = context;
        this.deviceListUpdateListeners = new ArrayList<>();
    }

    public void startRequestDeviceListUpdate() {
        if(deviceListUpdateTimer != null){
            return;
        }

        deviceListUpdateTimer = new Timer();
        deviceListUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDeviceList();
            }
        }, 0, 10000);
    }

    public void stopRequestDeviceListUpdate() {
        if(deviceListUpdateTimer == null){
            return;
        }
        deviceListUpdateTimer.cancel();
        deviceListUpdateTimer = null;
    }

    public void updateDeviceList() {
        GetDeviceListAsync getDeviceListAsync = new GetDeviceListAsync();
        getDeviceListAsync.paramUserId = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE).getString("userId", "");
        getDeviceListAsync.paramTypeId = "0";
        getDeviceListAsync.paramMapType = "Google";
        getDeviceListAsync.paramLanguage = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        getDeviceListAsync.callback = new AsyncCallback<JSONObject>() {
            @Override
            public void received(JSONObject data) throws Exception {
                JSONArray arr = data.getJSONArray("arr");
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    Device device = new Device();
                    boolean exists = false;
                    for(Device arrDevice : deviceList) {
                        if(arrDevice.id.equals(obj.getString("id"))) {
                            device = arrDevice;
                            exists = true;
                            break;
                        }
                    }

                    device.setFromJson(obj);

                    if(!exists) {
                        deviceList.add(device);
                    }
                    updateDeviceTracking(device);

                }
                deviceListUpdateListeners.forEach(listener -> listener.onDeviceListUpdate(deviceList));
            }

            @Override
            public void error() throws Exception { }

            @Override
            public void finished() throws Exception {

            }
        };
        getDeviceListAsync.execute();
    }

    public void updateDeviceTracking(Device device) {
        GetDeviceTrackingAsync getDeviceTrackingAsync = new GetDeviceTrackingAsync();
        getDeviceTrackingAsync.paramDeviceId = device.id;
        getDeviceTrackingAsync.paramTimezone = TimeZone.getDefault().getRawOffset() / 3_600_000 + 1;
        getDeviceTrackingAsync.paramMapType = "Google";
        getDeviceTrackingAsync.paramLanguage = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        getDeviceTrackingAsync.callback = new AsyncCallback<JSONObject>() {
            @Override
            public void received(JSONObject data) throws Exception {
                device.setFromJson(data);
                deviceListUpdateListeners.forEach(listener -> listener.onDeviceListUpdate(deviceList));
            }

            @Override
            public void error() throws Exception { }

            @Override
            public void finished() throws Exception { }
        };
        getDeviceTrackingAsync.execute();
    }

    public void registerUpdateListener(DeviceListUpdateListener listener) {
        deviceListUpdateListeners.add(listener);
        if(!deviceList.isEmpty()) {
            listener.onDeviceListUpdate(deviceList);
        }
    }

    public void unregisterUpdateListener(DeviceListUpdateListener listener) {
        deviceListUpdateListeners.remove(listener);
    }

    public interface DeviceListUpdateListener {
        void onDeviceListUpdate(ArrayList<Device> deviceList);
    }
}
