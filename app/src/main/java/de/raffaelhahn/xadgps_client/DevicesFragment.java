package de.raffaelhahn.xadgps_client;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import de.raffaelhahn.xadgps_client.services.DeviceListService;

public class DevicesFragment extends Fragment implements DeviceListService.DeviceListUpdateListener {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private DevicesRecyclerAdapter mAdapter;

    public DevicesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = getView().findViewById(R.id.recycler_view_devices);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DevicesRecyclerAdapter(item -> {
            DeviceInfoSheet infoSheet = DeviceInfoSheet.newInstance(item.id);
            infoSheet.show(getActivity().getSupportFragmentManager(), "deviceInfoSheet");
        });
        // Set CustomAdapter as the adapter for RecyclerView.
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).deviceListService.registerUpdateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).deviceListService.unregisterUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity)getActivity()).deviceListService.unregisterUpdateListener(this);
    }

    @Override
    public void onDeviceListUpdate(ArrayList<Device> deviceList) {
        getActivity().runOnUiThread(() -> {
            mAdapter.setDeviceList(deviceList);
            mAdapter.notifyDataSetChanged();
        });
    }
}