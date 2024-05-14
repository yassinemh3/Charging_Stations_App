package de.h_da.nzse.praktikum_sose22;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {

    private StationListAdapter stationListAdapter;
    private RepairStationListAdapter adapter;
    private Set<StationItem> favorised = new HashSet<>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private List<StationItem> selectedStations;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    boolean isRepairMen;


    public ListFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
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
//            selectedStations = getArguments().getParcelableArrayList("selected");
            Log.e("MapFragment", "Received " +
                    selectedStations.size() + " Stations from the map");
            selectedStations.forEach(stationItem -> Log.e("LIST_FRAGMENT", stationItem.street + stationItem.streetNumber));
            isRepairMen = getArguments().getBoolean(WelcomeActivity.IS_REPAIR_MAN);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        int view;
//        if (isRepairMen) {
//            view = R.layout.repair_fragmet;
//        } else {
//            view = R.layout.fragment_list;
//        }
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createList(view);
    }

    private void createList(View view) {
        RecyclerView sList = view.findViewById(R.id.resultView);
        if (isRepairMen) {
            adapter = new RepairStationListAdapter(selectedStations, getContext());
            sList.setAdapter(adapter);
        } else {
            stationListAdapter = new StationListAdapter(requireContext(), selectedStations, StationListAdapter.MODUS.LIST);
            sList.setAdapter(stationListAdapter);
        }
        sList.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }


    public Set<StationItem> getFavorised() {
        return stationListAdapter.getFavorites();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(ArrayList<StationItem> forList) {
        if (adapter == null || stationListAdapter == null) {
            selectedStations= forList;
        }
        if (isRepairMen) {
            if (adapter != null) {
                adapter.setData(forList);
            }
        } else {
            if (stationListAdapter != null) {
                stationListAdapter.setData(forList);
            }
        }
    }
}