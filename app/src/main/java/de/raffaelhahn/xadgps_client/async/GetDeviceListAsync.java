package de.raffaelhahn.xadgps_client.async;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.raffaelhahn.xadgps_client.Constants;


public class GetDeviceListAsync extends AsyncTask<Void, Void, Void> {

    public AsyncCallback<JSONObject> callback;

    public String paramUserId;
    public String paramTypeId;
    public String paramMapType;
    public String paramLanguage;

    public JSONObject resultObject;

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
        try {
            Log.d("TESTT", Constants.API_URL + "/GetDeviceList?ID=" + paramUserId + "&TypeID=" + paramTypeId + "&MapType=" + paramMapType + "&Language=" + paramLanguage);
            resultObject = AsyncUtils.readJsonFromUrl(Constants.API_URL + "/GetDeviceList?ID=" + paramUserId + "&TypeID=" + paramTypeId + "&MapType=" + paramMapType + "&Language=" + paramLanguage);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(callback != null) {
            if (resultObject != null) {
                callback.received(resultObject);
            } else {
                callback.error();
            }
            callback.finished();
        }
    }

}