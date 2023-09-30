package de.raffaelhahn.xadgps_client.async;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;


public class LoginAsync extends AsyncTask<Void, Void, Void> {

    public AsyncCallback<JSONObject> callback;

    public String paramName;
    public String paramPass;
    public String paramLoginType;
    public String paramAppID;
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
            obj = AsyncUtils.readJsonFromUrl(Constants.API_URL + "/Login2?Name=" + paramName + "&Pass=" + paramPass + "&LoginType=" + paramLoginType + "&AppID=" + paramAppID + "&Language=" + paramLanguage);
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