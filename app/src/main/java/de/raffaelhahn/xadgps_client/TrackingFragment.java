package de.raffaelhahn.xadgps_client;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.card.MaterialCardView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import de.raffaelhahn.xadgps_client.services.DeviceListService;

public class TrackingFragment extends Fragment implements DeviceListService.DeviceListUpdateListener {
    public static final String ARG_PARAM_SHOW_DEVICE_ID = "paramShowDeviceId";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final String LATEST_LAT_PREF_KEY = "myLatestLat";
    private static final String LATEST_LON_PREF_KEY = "myLatestLon";

    private String paramShowDeviceId;
    private MapView map = null;
    private MaterialCardView trackingLoadingView = null;
    private MyLocationNewOverlay mLocationOverlay;

    public TrackingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paramShowDeviceId = getArguments().getString(ARG_PARAM_SHOW_DEVICE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        trackingLoadingView = view.findViewById(R.id.tracking_loading);
        if(((MainActivity) getActivity()).deviceListService.isInitialDeviceTrackingLoaded()){
            trackingLoadingView.setVisibility(View.GONE);
        }

        SharedPreferences preferences = getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        map = getView().findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.getController().setZoom(5.0);
        map.setMinZoomLevel(5.0);
        IMapController mapController = map.getController();
        mapController.setZoom(15.0);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);

        map.setMultiTouchControls(true);
        map.setFlingEnabled(true);
        map.setVerticalMapRepetitionEnabled(false);
        map.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(), MapView.getTileSystem().getMinLatitude(), 0);
        if(preferences.contains(LATEST_LAT_PREF_KEY) && preferences.contains(LATEST_LON_PREF_KEY) && paramShowDeviceId == null){
            mapController.setZoom(20.0);
            mapController.setCenter(new GeoPoint(preferences.getFloat(LATEST_LAT_PREF_KEY, 0), preferences.getFloat(LATEST_LON_PREF_KEY, 0)));
        }
        /*mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);*/

        mLocationOverlay.runOnFirstFix(() -> {
            if(getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if(paramShowDeviceId == null) {
                        mapController.setZoom(20.0);
                        mapController.setCenter(mLocationOverlay.getMyLocation());
                    }
                    editor.putFloat(LATEST_LAT_PREF_KEY, (float) mLocationOverlay.getMyLocation().getLatitude());
                    editor.putFloat(LATEST_LON_PREF_KEY, (float) mLocationOverlay.getMyLocation().getLongitude());
                    editor.apply();
                });
            }
        });

        requestPermissionsIfNecessary(new String[]{
                        // if you need to show the current location, uncomment the line below
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        // WRITE_EXTERNAL_STORAGE is required in order to show the map
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        ((MainActivity) getActivity()).deviceListService.registerUpdateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        ((MainActivity) getActivity()).deviceListService.unregisterUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).deviceListService.unregisterUpdateListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onDeviceListUpdate(ArrayList<Device> deviceList) {
        getActivity().runOnUiThread(() -> {
            trackingLoadingView.setVisibility(View.GONE);
            map.getOverlays().clear();
            map.getOverlays().add(mLocationOverlay);
            for (Device device : deviceList) {
                double lon = Double.parseDouble(device.longitude);
                double lat = Double.parseDouble(device.latitude);
                String[] deviceInfoShort = device.getDeviceInfoShort(getContext());
                String subDescription = deviceInfoShort[0];
                if(!deviceInfoShort[1].isEmpty()) {
                    subDescription += " (" + deviceInfoShort[1] + ")";
                }
                if(getActivity() == null || getActivity().isDestroyed()) return;
                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(lat, lon));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(device.name);
                marker.setSubDescription(subDescription);
                //marker.setIcon(getResources().getDrawable(R.drawable.router));
                map.getOverlays().add(marker);
                map.invalidate();
                if(paramShowDeviceId != null && paramShowDeviceId.equals(device.id)) {
                    IMapController mapController = map.getController();
                    mapController.setZoom(map.getZoomLevelDouble());
                    mapController.setCenter(new GeoPoint(lat, lon));
                }
            }
        });
    }
}