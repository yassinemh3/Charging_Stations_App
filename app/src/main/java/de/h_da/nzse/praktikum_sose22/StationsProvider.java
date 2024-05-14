package de.h_da.nzse.praktikum_sose22;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StationsProvider {

    private StationsProvider() {
    }

    public static StationsProvider INSTANCE = new StationsProvider();
    private ArrayList<StationItem> stationList;
    private List<StationItem> lastSelected = new ArrayList<>();
    private Set<StationItem> reported = new HashSet<>();

    public ArrayList<StationItem> getStationList() {
        return stationList;
    }

    public void init(ArrayList<StationItem> csvReadStations) {
        stationList = csvReadStations;
    }

    public boolean checkDataAreReady(Context context) {
        List<StationItem> allStations = getStationList();
        if (allStations == null) {
            Toast.makeText(context,
                    "Daten werden noch geladen. Please wait", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public ArrayList<StationItem> getReported() {
        return new ArrayList<>(reported);
    }

    public ArrayList<StationItem> getSelected() {
        return new ArrayList<>(lastSelected);
    }

    public void saveSelected(List<StationItem> selectedByUser) {
        this.lastSelected = selectedByUser;
        saveReported(selectedByUser);
    }

    public void saveReported(List<StationItem> selectedByUser) {
        reported.addAll(selectedByUser.stream().filter(
                it -> it.status == StationItem.DEFECT
        ).collect(Collectors.toList()));
        System.out.println(reported.size());
    }

    public void removeReported(StationItem stationItem) {
        reported.remove(stationItem);
        stationItem.status = StationItem.READY;
    }
}
