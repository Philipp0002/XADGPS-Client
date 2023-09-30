package de.raffaelhahn.xadgps_client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DevicesRecyclerAdapter extends RecyclerView.Adapter<DevicesRecyclerAdapter.DeviceViewHolder> {

    private ArrayList<Device> deviceList = new ArrayList<>();

    public void setDeviceList(ArrayList<Device> deviceList) {
        this.deviceList = deviceList;
    }

    private OnItemClickListener listener;

    public DevicesRecyclerAdapter(OnItemClickListener onItemClickListener){
        this.listener = onItemClickListener;
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        public final TextView deviceName;
        public final TextView deviceItemDescription;

        public DeviceViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            deviceName = view.findViewById(R.id.deviceItemName);
            deviceItemDescription = view.findViewById(R.id.deviceItemDescription);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_list_item, viewGroup, false);

        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder viewHolder, final int position) {
        Device device = deviceList.get(position);
        viewHolder.deviceName.setText(device.name);

        viewHolder.deviceItemDescription.setText(device.getDeviceInfoShort(viewHolder.itemView.getContext()));
        viewHolder.itemView.setOnClickListener(a -> listener.onItemClick(device));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Device item);
    }
}
