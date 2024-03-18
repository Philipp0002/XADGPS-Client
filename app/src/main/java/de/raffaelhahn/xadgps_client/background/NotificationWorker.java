package de.raffaelhahn.xadgps_client.background;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import de.raffaelhahn.xadgps_client.NotifyDevice;
import de.raffaelhahn.xadgps_client.R;
import de.raffaelhahn.xadgps_client.async.Constants;
import de.raffaelhahn.xadgps_client.async.GetDeviceListAsync;
import de.raffaelhahn.xadgps_client.services.MovementMonitorService;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);

            GetDeviceListAsync getDeviceListAsync = new GetDeviceListAsync();
            getDeviceListAsync.paramUserId = preferences.getString("userId", "");
            getDeviceListAsync.paramTypeId = "0";
            getDeviceListAsync.paramMapType = "Google";
            getDeviceListAsync.paramLanguage = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
            getDeviceListAsync.runFetch();

            if (getDeviceListAsync.resultObject != null) {
                ArrayList<NotifyDevice> liveDevices = new ArrayList<>();
                ArrayList<NotifyDevice> movedDevices = new ArrayList<>();
                JSONArray liveDeviceJSON = getDeviceListAsync.resultObject.getJSONArray("arr");
                for (int i = 0; i < liveDeviceJSON.length(); i++) {
                    JSONObject liveDevice = liveDeviceJSON.getJSONObject(i);
                    liveDevices.add(new NotifyDevice().setFromJson(liveDevice));
                }

                String notifyDevicesRaw = preferences.getString(Constants.NOTIFY_DEVICES_PREF_KEY, "[]");
                JSONArray array = new JSONArray(notifyDevicesRaw);
                for (int i = 0; i < array.length(); i++) {
                    NotifyDevice notifyDevice = new NotifyDevice().setFromJson(array.getJSONObject(0));
                    Optional<NotifyDevice> liveDeviceOpt = liveDevices.stream().filter(d -> d.id.equals(notifyDevice.id)).findFirst();
                    if (liveDeviceOpt.isPresent()) {
                        NotifyDevice liveDevice = liveDeviceOpt.get();
                        double liveLat = Double.parseDouble(liveDevice.latitude);
                        double liveLon = Double.parseDouble(liveDevice.longitude);
                        double notifyLat = Double.parseDouble(notifyDevice.latitude);
                        double notifyLon = Double.parseDouble(notifyDevice.longitude);

                        if (liveLat != notifyLat || liveLon != notifyLon) {
                            movedDevices.add(liveDevice);
                        }
                    }
                }

                if (!movedDevices.isEmpty()) {
                    String movedDevicesString = movedDevices.stream().map(d -> {
                        MovementMonitorService.stopMonitoring(getApplicationContext(), d);
                        return d.name;
                    }).collect(Collectors.joining(", "));
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "1")
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(getApplicationContext().getString(R.string.monitor_notify_device_moved))
                            .setContentText(movedDevicesString)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(movedDevicesString))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        NotificationManagerCompat.from(getApplicationContext()).notify(1, builder.build());
                    }
                }
                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.retry();
        }

    }
}
