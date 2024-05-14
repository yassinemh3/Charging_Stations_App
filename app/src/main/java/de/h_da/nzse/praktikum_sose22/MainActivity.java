package de.h_da.nzse.praktikum_sose22;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ListFragment listFragment;
    private MapFragment mapFragment;
    private FavoriteFragment favoriteFragment;
    BottomNavigationView navigationView;

    private static Context mContext;
    private boolean isRepairman;

    final int MY_PERMISSIONS_STORAGE_INTENRET = 1;

    private RequestService mService = null;
    private RequestService.RequestServiceBinder binder;
    boolean mBound = false;

    private static String filePath = "NZSE";
    private static String csvFile; // liefert die Downloadfunktion

    private String ladestationen = "ladestationen.txt"; // Name der lokalen Datei

    ArrayList<StationItem> stationList;
    ArrayList<Integer> brokenStations;
    // unser Beispiel
    // **********************
    private String url = "https://www.bundesnetzagentur.de/SharedDocs/Downloads/DE/Sachgebiete/Energie/Unternehmen_Institutionen/E_Mobilitaet/Ladesaeulenregister_CSV.csv;jsessionid=03A305AA73A87C442992FE18995CAEBE?__blob=publicationFile&v=37";

    // **********************

    //--------------------------    Serviceverbindung einrichten
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // an den RequestService binden,
            // Service-Objekt casting auf IBinder und LocalService instance erhalten
            binder = (RequestService.RequestServiceBinder) service;
            mService = binder.getService();
            // callback setzen
            mService.setCallback(getHandler());

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    //--------------------------

    //--------------------------Handler einrichten:
    // Callbacks vereinbaren für Service Binding,
    // weiterleiten an bindService() für Ergebnismitteilung
    private Handler getHandler() {
//        Process.THREAD_PRIORITY_URGENT_DISPLAY
        HandlerThread handlerr = new HandlerThread(
                "ServiceThreadRead",
                Process.THREAD_PRIORITY_MORE_FAVORABLE
        );
        handlerr.start();
        return new Handler(handlerr.getLooper()) {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                csvFile = (String) bundle.get(RequestService.FILEPATH);
                // Datei einlesen
                StationsProvider.INSTANCE.init(MainActivity.csvRead());
            }// handleMessage
        };
    }

    //--------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
        mContext = this;
        getIntent().getBooleanExtra(WelcomeActivity.IS_REPAIR_MAN, true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnItemSelectedListener(item -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(WelcomeActivity.IS_REPAIR_MAN,
                    getIntent().getBooleanExtra(WelcomeActivity.IS_REPAIR_MAN, false));
            switch (item.getItemId()) {
                case R.id.nav_favorite: {
                    favoriteFragment = (FavoriteFragment) (getSupportFragmentManager().findFragmentByTag("fav_frag"));
                    if (favoriteFragment == null) {
                        Toast.makeText(this, "created Fav Frag", Toast.LENGTH_SHORT).show();
                        favoriteFragment = new FavoriteFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.body_container, favoriteFragment, "fav_frag").commit();
                        bundle.putParcelableArrayList("selected", new ArrayList<>(listFragment.getFavorised()));
                        favoriteFragment.setArguments(bundle);
                    }
                    favoriteFragment.setData(listFragment.getFavorised());
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .show(favoriteFragment);
                    if (mapFragment != null) transaction.hide(mapFragment);
                    if (listFragment != null) transaction.hide(listFragment);
                    transaction.commit();
                    break;
                }
                case R.id.nav_list: {
                    listFragment = (ListFragment) getSupportFragmentManager().findFragmentByTag("list_frag");
                    if (listFragment == null) {
                        Toast.makeText(this, "created List Frag", Toast.LENGTH_SHORT).show();
                        listFragment = new ListFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.body_container, listFragment, "list_frag").commit();
                    }
                    ArrayList<StationItem> forList = new ArrayList<>();
                    if (bundle.getBoolean(WelcomeActivity.IS_REPAIR_MAN)) {
                        forList.addAll(StationsProvider.INSTANCE.getReported());
                        if (forList.isEmpty()) {
                            Toast toast = Toast.makeText(
                                    this,
                                    "Users have not reported any stations. Nothing to show",
                                    Toast.LENGTH_SHORT
                            );
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        forList.addAll(mapFragment.getSelectedStations());
                    }
                    listFragment.setList(forList);
                    bundle.putParcelableArrayList("selected", forList);
                    Log.e("MainAc", "sending " + bundle.getParcelableArrayList("selected").size());
                    listFragment.setArguments(bundle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .show(listFragment);
                    if (mapFragment != null) transaction.hide(mapFragment);
                    if (favoriteFragment != null) transaction.hide(favoriteFragment);
                    transaction.commit();
                    break;
                }
                default: {
                    mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("map_frag");
                    if (mapFragment == null) {
                        Toast.makeText(this, "created Map Frag", Toast.LENGTH_SHORT).show();
                        mapFragment = new MapFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.body_container, mapFragment, "map_frag")
                                .setReorderingAllowed(true).commit();
                    }
                    if (!isRepairman) {
                        mapFragment.refresh();
                    }
                    mapFragment.setArguments(bundle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .show(mapFragment);
                    if (listFragment != null) transaction.hide(listFragment);
                    if (favoriteFragment != null) transaction.hide(favoriteFragment);
                    transaction.commit();
                }
            }

            return true;
        });

        // Bind to RequestService
        Intent myIntent = new Intent(this, RequestService.class);
        // startService(myIntent); oder alternativ:
        bindService(myIntent, mConnection, Context.BIND_AUTO_CREATE);

        // Permission grant/gewähren
        String[] permissions =
                {Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions,
                MY_PERMISSIONS_STORAGE_INTENRET);


        SharedPreferences pref = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        boolean isRepairman = pref.getBoolean("isRepairman", true);
        MenuItem favItem = (MenuItem) navigationView.getMenu().findItem(R.id.nav_favorite);

        // sorting the list depending on user role
        if (isRepairman) {
            favItem.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_history));
            favItem.setTitle(getString(R.string.menu_repair_history));
        } else {
            favItem.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_favorite));
            favItem.setTitle(getString(R.string.menu_favorite));
        }

    }// onCreate

    public void loadData() {
        if (mService != null) {
            binder.runURLDownload("download", url, filePath, ladestationen);
        } else {
            System.out.println("onCreate: no service");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
                navigationView.setSelectedItemId(navigationView.getMenu().getItem(0).getItemId());
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_STORAGE_INTENRET) {// wenn die Anfrage gecancelled wird, sind die Ergnisfelder leer.
            if (grantResults.length > 0) {
                for (int grant = 0; grant < grantResults.length; grant++) {
                    if (grantResults[grant] == PackageManager.PERMISSION_GRANTED) {
                        System.out.println(permissions[grant] + " vorhanden");
                    } else {
                        System.out.println(permissions[grant] + "  n i c h t  vorhanden");
                    }
                }
            }
            // ... u.U. Prüfung anderer/weiterer Permissions
        }
    }


    public static Context getContext() {
        return mContext;
    }

    public static ArrayList<StationItem> csvRead() {
        String csvline = ""; // eingelesene csvZeile
        File myFile; // Fileobjekt
        ArrayList<StationItem> list = new ArrayList<>();


        // Position in der csvZeile
        final int posOperator = 0;
        final int posStreet = 1;
        final int posStreetNumber = 2;
        final int posPostcode = 4;
        final int posLocation = 5;
        final int posLatitude = 8;
        final int posLongitude = 9;
        final int posOutputPower = 11;
        // ... weitere Positionen
        float lat, lon; // Breiten- und Längenangabe

        try {
            // lokale Datei ansprechen
            myFile = new File(csvFile);
            FileInputStream fIn = new FileInputStream(myFile);
            InputStreamReader isr = new InputStreamReader(fIn, "ISO_8859-1");//"Windows-1252");// StandardCharsets.UTF_16);
            BufferedReader myReader = new BufferedReader(isr);

            for (int n = 0; n < 11; n++) {
                myReader.readLine();
            } // for

            int lines = 0;
            String[] lineArray = null;
            String select = "--";
            while ((csvline = myReader.readLine()) != null) // Dateiende?
            {
                lines++;
//                if (BuildConfig.DEBUG && lines == 2000) break;
                ;
                lineArray = csvline.split(";");
                try {
                    NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
                    StationItem chargingStation = new StationItem(lineArray[posOperator], lineArray[posStreet], lineArray[posStreetNumber], format.parse(lineArray[posPostcode]).intValue(), lineArray[posLocation], format.parse(lineArray[posLatitude]).floatValue(), format.parse(lineArray[posLongitude]).floatValue(), format.parse(lineArray[posOutputPower]).intValue());
                    list.add(chargingStation);
                } catch (NumberFormatException e) {
                    Log.e("E-Eichhoernchen", "exception", e);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(MainActivity.class.getName(), "Will scape this mal formatted line");
                } catch (Exception e) {
                    Log.e("", e.getMessage());
                }


            } // while
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } // try-catch

        return list;
    } // csvRead

    @Override
    public void finish() {
        unbindService(mConnection);
        super.finish();
    }


}