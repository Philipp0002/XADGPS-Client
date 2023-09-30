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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import de.raffaelhahn.xadgps_client.async.Constants;
import de.raffaelhahn.xadgps_client.services.DeviceListService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrackingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrackingFragment extends Fragment implements DeviceListService.DeviceListUpdateListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MapView map = null;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MyLocationNewOverlay mLocationOverlay;

    //private RotationGestureOverlay mRotationGestureOverlay;

    public TrackingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrackingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrackingFragment newInstance(String param1, String param2) {
        TrackingFragment fragment = new TrackingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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

        if(preferences.contains("myLatestLat") && preferences.contains("myLatestLon")){
            mapController.setZoom(20.0);
            mapController.setCenter(new GeoPoint(preferences.getFloat("myLatestLat", 0), preferences.getFloat("myLatestLon", 0)));
        }
        /*mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);*/

        mLocationOverlay.runOnFirstFix(() -> {
            if(getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mapController.setZoom(20.0);
                    mapController.setCenter(mLocationOverlay.getMyLocation());
                    editor.putFloat("myLatestLat", (float) mLocationOverlay.getMyLocation().getLatitude());
                    editor.putFloat("myLatestLon", (float) mLocationOverlay.getMyLocation().getLongitude());
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
            map.getOverlays().clear();
            map.getOverlays().add(mLocationOverlay);
            for (Device device : deviceList) {
                double lon = Double.parseDouble(device.longitude);
                double lat = Double.parseDouble(device.latitude);
                if(getActivity() == null || getActivity().isDestroyed()) return;
                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(lat, lon));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(device.name);
                marker.setSubDescription(device.getDeviceInfoShort(getContext()));
                //marker.setIcon(getResources().getDrawable(R.drawable.router));
                map.getOverlays().add(marker);
                map.invalidate();
            }
        });
    }
}