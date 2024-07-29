package de.raffaelhahn.xadgps_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;

import de.raffaelhahn.xadgps_client.async.AsyncCallback;
import de.raffaelhahn.xadgps_client.async.LoginAsync;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.login).setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        EditText usernameField = findViewById(R.id.username);
        EditText passwordField = findViewById(R.id.password);
        ProgressBar loadingSpinner = findViewById(R.id.loading);
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        loadingSpinner.setVisibility(View.VISIBLE);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);

        LoginAsync loginAsync = new LoginAsync();
        loginAsync.paramName = username;
        loginAsync.paramPass = password;
        loginAsync.paramLoginType = OperatingMode.UNKNOWN.numericString;
        loginAsync.paramAppID = Constants.APP_ID;
        loginAsync.paramLanguage = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();


        loginAsync.callback = new AsyncCallback<JSONObject>() {
            @Override
            public void received(JSONObject data) throws Exception {
                if("0".equals(data.getString("state"))){
                    switch(OperatingMode.fromNumericString(data.getString("loginType"))) {
                        case USER:
                            JSONObject userInfo = data.getJSONObject("userInfo");
                            saveUserData(userInfo.getString("userName"), userInfo.getString("loginName"), userInfo.getString("userID"));
                            break;
                        case DEVICE:
                            JSONObject deviceInfo = data.getJSONObject("deviceInfo");
                            saveDeviceData(deviceInfo.getString("deviceID"), deviceInfo.getString("deviceName"));
                            break;
                    }
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void error() {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void finished() {
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    usernameField.setEnabled(true);
                    passwordField.setEnabled(true);
                });
            }
        };
        loginAsync.execute();
    }

    public void saveUserData(String username, String loginName, String userId) {
        SharedPreferences preferences = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.SP_KEY_OPERATING_MODE, OperatingMode.USER.name());
        editor.putString(Constants.SP_KEY_USERNAME, username);
        editor.putString(Constants.SP_KEY_LOGIN_NAME, loginName);
        editor.putString(Constants.SP_KEY_USER_ID, userId);
        editor.apply();
    }

    public void saveDeviceData(String deviceId, String deviceName) {
        SharedPreferences preferences = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.SP_KEY_OPERATING_MODE, OperatingMode.DEVICE.name());
        editor.putString(Constants.SP_KEY_DEVICE_ID, deviceId);
        editor.apply();
    }
}