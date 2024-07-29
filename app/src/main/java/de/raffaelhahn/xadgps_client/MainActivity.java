package de.raffaelhahn.xadgps_client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;

import de.raffaelhahn.xadgps_client.async.Constants;
import de.raffaelhahn.xadgps_client.services.DeviceListService;

public class MainActivity extends AppCompatActivity {

    private static final String ARG_PARAM_BOTTOM_SELECTED = "paramBottomSelected";
    private BottomNavigationView bottomNavigationView;
    private FragmentContainerView fragmentContainerView;
    public DeviceListService deviceListService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
        if(!sharedPreferences.contains("operating_mode")){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainerView = findViewById(R.id.fragment_container_view);
        deviceListService = new DeviceListService(this);

        bottomNavigationView.setOnItemSelectedListener(item -> openFragmentById(item.getItemId()));
        if (savedInstanceState != null) {
            bottomNavigationView.setSelectedItemId(savedInstanceState.getInt(ARG_PARAM_BOTTOM_SELECTED));
            openFragmentById(savedInstanceState.getInt(ARG_PARAM_BOTTOM_SELECTED));
            Log.d("ASDFF", savedInstanceState.getInt(ARG_PARAM_BOTTOM_SELECTED)+"");
        } else {
            openFragmentById(R.id.item_tracking);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putInt("ARG_PARAM_BOTTOM_SELECTED", bottomNavigationView.getSelectedItemId());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private boolean openFragmentById(int itemId) {
        if (itemId == R.id.item_tracking) {
            openFragment(TrackingFragment.class);
            return true;
        } else if (itemId == R.id.item_devices) {
            openFragment(DevicesFragment.class);
            return true;
        }
        return false;
    }

    private void openFragment(Class clazz) {
        openFragment(clazz, null);
    }

    private void openFragment(Class clazz, Bundle args) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, clazz, args)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(deviceListService != null) {
            deviceListService.startRequestDeviceListUpdate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(deviceListService != null) {
            deviceListService.stopRequestDeviceListUpdate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(deviceListService != null) {
            deviceListService.stopRequestDeviceListUpdate();
        }
    }

    public void showDeviceOnMap(String deviceId) {
        Bundle args = new Bundle();
        args.putString(TrackingFragment.ARG_PARAM_SHOW_DEVICE_ID, deviceId);

        openFragment(TrackingFragment.class, args);
        //bottomNavigationView.setSelectedItemId(R.id.item_tracking);
        //bottomNavigationView.getMenu().getItem(0).setChecked(true);
        bottomNavigationView.getMenu().findItem(R.id.item_tracking).setChecked(true);
    }


}