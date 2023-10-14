package de.raffaelhahn.xadgps_client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Optional;

import de.raffaelhahn.xadgps_client.services.DeviceListService;

public class DeviceInfoSheet extends BottomSheetDialogFragment implements DeviceListService.DeviceListUpdateListener {

    private static final String ARG_PARAM_ID = "paramId";
    private String id;

    private TextView deviceName;
    private TextView deviceId;
    private TextView deviceStatus;
    private TextView deviceGroupName;
    private TextView deviceGroupId;
    private TextView deviceCar;
    private TextView deviceModel;
    private TextView deviceICCID;
    private TextView deviceStopTime;
    private TextView deviceCourse;
    private TextView deviceCourseDesc;
    private TextView deviceType;
    private TextView deviceFortification;
    private TextView deviceStyle;
    private TextView deviceLonLat;
    private TextView deviceSerialNumber;
    private TextView deviceLastCommunication;
    private TextView deviceGPSSignal;
    private ImageView deviceGPSSignalIcon;
    private TextView deviceGSMSignal;
    private ImageView deviceGSMSignalIcon;
    private TextView deviceIsStopped;
    private TextView deviceSpeed;
    private TextView deviceDistance;
    private Button deviceShowOnMapButton;

    public DeviceInfoSheet() {
    }

    public static DeviceInfoSheet newInstance(String paramId) {
        DeviceInfoSheet fragment = new DeviceInfoSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_ID, paramId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString(ARG_PARAM_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_info_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceName = view.findViewById(R.id.deviceName);
        deviceStatus = view.findViewById(R.id.deviceStatus);
        deviceGroupName = view.findViewById(R.id.deviceGroupName);
        deviceGroupId = view.findViewById(R.id.deviceGroupId);
        deviceCar = view.findViewById(R.id.deviceCar);
        deviceModel = view.findViewById(R.id.deviceModel);
        deviceICCID = view.findViewById(R.id.deviceICCID);
        deviceStopTime = view.findViewById(R.id.deviceStopTime);
        deviceCourse = view.findViewById(R.id.deviceCourse);
        deviceType = view.findViewById(R.id.deviceType);
        deviceFortification = view.findViewById(R.id.deviceFortification);
        deviceStyle = view.findViewById(R.id.deviceStyle);
        deviceLonLat = view.findViewById(R.id.deviceLonLat);
        deviceSerialNumber = view.findViewById(R.id.deviceSerial);
        deviceId = view.findViewById(R.id.deviceId);
        deviceCourseDesc = view.findViewById(R.id.deviceCourseDesc);
        deviceLastCommunication = view.findViewById(R.id.deviceLastCommunication);
        deviceGPSSignal = view.findViewById(R.id.deviceGPSSignal);
        deviceGPSSignalIcon = view.findViewById(R.id.deviceGPSSignalIcon);
        deviceGSMSignal = view.findViewById(R.id.deviceGSMSignal);
        deviceGSMSignalIcon = view.findViewById(R.id.deviceGSMSignalIcon);
        deviceIsStopped = view.findViewById(R.id.deviceStopped);
        deviceSpeed = view.findViewById(R.id.deviceSpeed);
        deviceDistance = view.findViewById(R.id.deviceDistance);
        deviceShowOnMapButton = view.findViewById(R.id.deviceShowOnMap);

        deviceShowOnMapButton.setVisibility(View.GONE);
        deviceShowOnMapButton.setOnClickListener(v -> showDeviceOnMap());
    }

    private void updateView(Device device) {
        int gpsSignal = Integer.parseInt(device.GPS);
        int gsmSignal = Integer.parseInt(device.GSM);
        deviceName.setText(device.name);
        deviceStatus.setText(device.status);
        deviceGroupName.setText(device.groupName);
        deviceGroupId.setText(device.groupID);
        deviceCar.setText(device.car);
        deviceModel.setText(device.model);
        deviceICCID.setText(device.iccid);
        deviceStopTime.setText(device.StopTime);
        deviceCourse.setText(device.course);
        deviceType.setText(device.type);
        deviceFortification.setText("1".equals(device.Fortification) ? R.string.yes : R.string.no);
        deviceStyle.setText(device.style);
        deviceLonLat.setText(device.longitude + ", " + device.latitude);
        deviceSerialNumber.setText(device.sn);
        deviceId.setText(device.id);
        deviceCourseDesc.setText(device.coursedesc);
        deviceLastCommunication.setText(device.lastCommunication);
        deviceGPSSignal.setText(getString(Utils.getGpsHint(gpsSignal)) + " (" + device.GPS + ")");
        deviceGPSSignalIcon.setImageResource(Utils.getGpsIcon(gpsSignal));
        deviceGSMSignal.setText(getString(Utils.getGsmHint(gsmSignal)) + " (" + device.GSM + ")");
        deviceGSMSignalIcon.setImageResource(Utils.getGsmIcon(gsmSignal));
        deviceIsStopped.setText("1".equals(device.isStop) ? R.string.yes : R.string.no);
        deviceSpeed.setText(device.speed + " km/h");
        deviceDistance.setText(device.distance);
        deviceShowOnMapButton.setVisibility(View.VISIBLE);
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
        Optional<Device> optDevice = deviceList.stream().filter(device -> id.equals(device.id)).findFirst();
        if(optDevice.isPresent()) {
            if(getActivity() != null){
                getActivity().runOnUiThread(() -> updateView(optDevice.get()));
            }
        }
    }

    public void showDeviceOnMap() {
        ((MainActivity)getActivity()).showDeviceOnMap(id);
        this.dismiss();
    }
}
