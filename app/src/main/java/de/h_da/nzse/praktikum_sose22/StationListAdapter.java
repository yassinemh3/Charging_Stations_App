package de.h_da.nzse.praktikum_sose22;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.ViewHolder> {
    private List<StationItem> stations;
    private final Context context;
    private final MODUS modus;


    public StationListAdapter(@NonNull Context context, List<StationItem> stations, MODUS modus) {
        //super(context, 0, properties);
        this.stations = stations;
        this.context = context;
        this.modus = modus;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public StationListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("sdsd", String.valueOf(parent.getId()));
        return new ViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.list_item, parent, false)
        );
    }

    //fill text and imageviews with content
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull StationListAdapter.ViewHolder holder, int position) {
        final StationItem stationItem = stations.get(holder.getAdapterPosition());

        holder.operatorText.setText(stationItem.getOperator());
        holder.placeText.setText(stationItem.location);
        holder.streetText.setText(stationItem.street + " " + stationItem.streetNumber);


        setUpFavoriteButton(holder, position);

        holder.reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            int modPosition = holder.getAdapterPosition();
                            System.out.println(modPosition);
                            StationItem stationItem1 = stations.remove(modPosition);
                            stationItem1.status = StationItem.DEFECT;
                            notifyItemRemoved(modPosition);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.report_question)).setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
            }
        });


    }

    private void setUpFavoriteButton(ViewHolder holder, int position) {
        final StationItem stationItem = stations.get(position);
        updateFavColorOnInit(stationItem, holder);
        if (modus == MODUS.LIST) {
            holder.favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!stationItem.isFavorite) {
                        holder.favButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    } else {
                        holder.favButton.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                    }
                    stationItem.isFavorite = !stationItem.isFavorite;
                }
            });
        } else if (modus == MODUS.FAVORITE) {
            holder.favButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            holder.favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int changePos = holder.getAdapterPosition();
                    stations.remove(changePos);
                    stationItem.isFavorite = false;
                    notifyItemRemoved(changePos);
                }
            });
        }
    }

    private void updateFavColorOnInit(StationItem stationItem, ViewHolder holder) {
        if (modus == MODUS.LIST) {
            if (stationItem.isFavorite) {
                holder.favButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                holder.favButton.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            }
        } else if (modus == MODUS.FAVORITE) {
            holder.favButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }
    }


    @Override
    public int getItemCount() {
        return this.stations.size();
    }

    public Set<StationItem> getFavorites() {
        return (stations.stream().filter(stationItem -> stationItem.isFavorite).collect(Collectors.toSet()));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<StationItem> forList) {
        this.stations = forList;
        notifyDataSetChanged();
    }

    //assign all Views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView operatorText;
        private TextView placeText;
        private TextView streetText;
        private ImageButton favButton;
        private Button reportButton;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.operatorText = view.findViewById(R.id.operatorText);
            this.placeText = view.findViewById(R.id.placeText);
            this.streetText = view.findViewById(R.id.streetText);
            this.favButton = view.findViewById(R.id.favButton);
            this.reportButton = view.findViewById(R.id.reportButton);
        }

    }


    @Override
    public long getItemId(int position) {
        return stations.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    enum MODUS {
        FAVORITE,
        LIST
    }

    ;
}
