package de.h_da.nzse.praktikum_sose22;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RepairStationListAdapter extends RecyclerView.Adapter<RepairStationListAdapter.RepairViewHolder> {

    private List<StationItem> stations;
    private final Context context;

    public RepairStationListAdapter(List<StationItem> stations, Context context) {
        this.stations = stations;
        setHasStableIds(true);
        this.context = context;
    }

    @NonNull
    @Override
    public RepairStationListAdapter.RepairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("sdsd", String.valueOf(parent.getId()));
        return new RepairStationListAdapter.RepairViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.repair_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RepairStationListAdapter.RepairViewHolder holder, int position) {
        final StationItem stationItem = stations.get(holder.getAdapterPosition());

        holder.operatorText.setText(stationItem.getOperator());
        holder.placeText.setText(stationItem.location);
        holder.streetText.setText(stationItem.street + " " + stationItem.streetNumber);

        holder.repairButton.setOnClickListener(view -> {
//            stationItem.status = StationItem.READY;
            int pos = holder.getAdapterPosition();
            stations.remove(pos).status = StationItem.READY;
//            StationsProvider.INSTANCE.removeReported(stations.remove(pos));
            notifyItemRemoved(pos);

        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<StationItem> forList) {
        this.stations = forList;
        notifyDataSetChanged();
    }

    public static class RepairViewHolder extends RecyclerView.ViewHolder {
        private final TextView operatorText;
        private final TextView placeText;
        private final TextView streetText;
        private final Button repairButton;

        public RepairViewHolder(@NonNull View view) {
            super(view);
            this.operatorText = view.findViewById(R.id.operatorText);
            this.placeText = view.findViewById(R.id.placeText);
            this.streetText = view.findViewById(R.id.streetText);
//            this.repairButton = view.findViewById(R.id.repairButton);
            this.repairButton = view.findViewById(R.id.repairButton);
            assert repairButton != null;

//            assert repairButton != null;
        }
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    @Override
    public long getItemId(int position) {
        return stations.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
