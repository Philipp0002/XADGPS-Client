package de.raffaelhahn.xadgps_client.async;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.raffaelhahn.xadgps_client.Constants;


public class GetDeviceTrackingAsync extends AsyncTask<Void, Void, Void> {

    public AsyncCallback<JSONObject> callback;

    public String paramDeviceId;
    public int paramTimezone;
    public String paramMapType;
    public String paramLanguage;

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            runFetch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void runFetch() throws Exception {
        JSONObject obj = null;
        try {
            Log.d("TESTT", Constants.API_URL + "/GetTracking?DeviceID=" + paramDeviceId + "&TimeZone=" + paramTimezone + "&MapType=" + paramMapType + "&Language=" + paramLanguage);
            obj = AsyncUtils.readJsonFromUrl(Constants.API_URL + "/GetTracking?DeviceID=" + paramDeviceId + "&TimeZone=" + paramTimezone + "&MapType=" + paramMapType + "&Language=" + paramLanguage);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (obj != null) {
            callback.received(obj);
        } else {
            callback.error();
        }
        callback.finished();
    }

}