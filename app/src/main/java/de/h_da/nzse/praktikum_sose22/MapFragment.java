package de.h_da.nzse.praktikum_sose22;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MapFragment extends Fragment {
    private GoogleMap mMap;
    SearchView searchView;
    Slider distanceSlider;
    private Address selectedAddress;
    private CircularProgressIndicator circularProgressIndicator;
    private boolean isRepairMan;
    private List<StationItem> selectedByUser = new ArrayList<>();
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            Toast.makeText(MapFragment.this.getContext(), "Map ready", Toast.LENGTH_SHORT).show();

            HandlerThread handlerThread = new HandlerThread("", Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.start();
            Handler init = new Handler(handlerThread.getLooper());
            init.postDelayed(() -> {
                while (!StationsProvider.INSTANCE.checkDataAreReady(MapFragment.this.getContext())) {
                    try {
                        Log.e("ds", "waiting");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    circularProgressIndicator.setVisibility(View.GONE);
                    searchView.setQuery("Hochschule Darmstadt", true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markDarmstadt(), 13));
                });
            }, 100);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    };

    private LatLng markDarmstadt() {
        LatLng initialPosition = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(
                initialPosition
        ).title("Hochschule Darmstadt"));
        return initialPosition;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedAddress = savedInstanceState.getParcelable("address");
            distanceSlider.setValue(savedInstanceState.getFloat("dist_val"));
        }
        isRepairMan = getArguments().getBoolean(WelcomeActivity.IS_REPAIR_MAN, false);
        if (getArguments().containsKey(WelcomeActivity.IS_REPAIR_MAN)) {
            Log.e("IS+REPAI", String.valueOf(isRepairMan));
        }
    }


    private void markAllStations() {
        StationsProvider.INSTANCE.getStationList().forEach(stationItem -> {
            LatLng mark = new LatLng(stationItem.getLatitude(), stationItem.getLongitude());
            mMap.addMarker(new MarkerOptions().position(mark));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        circularProgressIndicator = view.findViewById(R.id.data_progress_indicator);
        searchView = view.findViewById(R.id.idSearchView);
        distanceSlider = view.findViewById(R.id.distance_slider);
        distanceSlider.setLabelFormatter(value -> value + " KM");
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        setUpSearchViewInteractions();
        setUpDistanceBarInteractions();
    }

    private void setUpDistanceBarInteractions() {
        distanceSlider.addOnChangeListener((slider, value, fromUser) -> {
            mMap.clear();
            selectedByUser = findStationsWithinTheCircle(selectedAddress, value);
            addStationsToMapAndAnimate(selectedByUser);
        });
    }


    private void setUpSearchViewInteractions() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!StationsProvider.INSTANCE.checkDataAreReady(MapFragment.this.getContext())) {
                    return false;
                }
                String location = searchView.getQuery().toString();
                List<Address> addressList = new ArrayList<>();
                Geocoder geocoder = new Geocoder(MapFragment.this.getContext());
                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressList.isEmpty()) {
                    Toast.makeText(MapFragment.this.getContext(), "No matching city found", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (!Objects.equals(addressList.get(0).getCountryCode(), "DE")) {
                    Toast.makeText(MapFragment.this.getContext(), "Country not supported", Toast.LENGTH_SHORT).show();
                    return false;
                }
                selectedAddress = addressList.get(0);
                int radius = (int) (distanceSlider.getValue());
                mMap.clear();
                if (!isRepairMan) {
                    StationsProvider.INSTANCE.saveReported(selectedByUser);
                }
                selectedByUser = findStationsWithinTheCircle(selectedAddress, radius);
                addStationsToMapAndAnimate(selectedByUser);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }


    private void addStationsToMapAndAnimate(List<StationItem> withinCircle) {
        withinCircle.forEach(stationItem -> {
            MarkerOptions markerOptions = new MarkerOptions().position(
                    new LatLng(stationItem.getLatitude(), stationItem.getLongitude())
            ).title(stationItem.street + " " + stationItem.streetNumber);
            if (isRepairMan) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_MAGENTA
                ));
            }
            mMap.addMarker(markerOptions);
            markDarmstadt();
            LatLng center = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 12));
        });
    }


    private List<StationItem> findStationsWithinTheCircle(Address address, double radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude()));
        circleOptions.radius(radius * 1000);
        circleOptions.fillColor(Color.TRANSPARENT);
        circleOptions.strokeColor(Color.BLACK);
        circleOptions.strokeWidth(2);
        mMap.addCircle(circleOptions);
        double latitude = address.getLatitude();
        double longitude = address.getLongitude();
        if (isRepairMan) return StationsProvider.INSTANCE.getReported();
        List<StationItem> selectedStations = new ArrayList<>();
        if (!isRepairMan) {
            int size = StationsProvider.INSTANCE.getStationList().stream()
                    .filter(it -> it.status == StationItem.DEFECT).collect(Collectors.toList()).size();
            System.out.println(size);
        }
        StationsProvider.INSTANCE.getStationList().forEach(stationItem -> {
            if (!isRepairMan && stationItem.status == StationItem.DEFECT) {
                return;
            }
            if (isRepairMan && stationItem.status != StationItem.DEFECT) {
                return;
            }
            if (isRepairMan && stationItem.status == StationItem.DEFECT) {
                int i = 0;
            }
            double distance = distance(
                    latitude, longitude, stationItem.getLatitude(), stationItem.getLongitude());
            if (Math.abs(distance) <= radius) {
                selectedStations.add(stationItem);
            }
        });
        return selectedStations;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Erdradius in km
        double dLat = (lat1 - lat2) * Math.PI / 180.0;
        double dLon = (lon1 - lon2) * Math.PI / 180.0;
        double a = Math.sin(dLat / 2.) * Math.sin(dLat / 2.)
                + Math.cos(lat1 * Math.PI / 180.0)
                * Math.cos(lat2 * Math.PI / 180.0)
                * Math.sin(dLon / 2.) * Math.sin(dLon / 2.);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1. - a));
        return R * c; // distance
    }


    public ArrayList<StationItem> getSelectedStations() {
        if (StationsProvider.INSTANCE.getSelected().isEmpty()) {
            return new ArrayList<>(selectedByUser);
        }
        return new ArrayList<>(StationsProvider.INSTANCE.getSelected());
    }

//    public ArrayList<StationItem> getReported() {
//        return selectedByUser.stream().filter(
//                stationItem -> stationItem.status == StationItem.DEFECT
//        ).collect(Collectors.toCollection(ArrayList::new));
//    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable("address", selectedAddress);
        outState.putFloat("dist_val", distanceSlider.getValue());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (!isRepairMan) {
            StationsProvider.INSTANCE.saveSelected(selectedByUser);
        }
        super.onPause();
    }

    public void refresh() {
        if (mMap != null) {
            mMap.clear();
            selectedByUser = findStationsWithinTheCircle(selectedAddress, distanceSlider.getValue());
            addStationsToMapAndAnimate(selectedByUser);
        }
        if (!isRepairMan) {

            StationsProvider.INSTANCE.saveReported(selectedByUser);

        }
    }
}