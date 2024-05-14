package de.h_da.nzse.praktikum_sose22;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by NZSE
 * Serverzugriff (Dateidownload) als Beispiel für eine Serviceklasse
 */
public class RequestService extends Service {
    /**
     * @param intent - Intent, das beim Aufruf des Service verwendet wurde
     * @return binder - Kommunikationsobjekt zum Service
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Bezeichner für Bundleparameter: URL
     */
    public static final String REQUESTURL = "NZSE.url";
    /**
     * Bezeichner für Bundleparameter: Id zu Image
     */
    public static final String UNIQUEID = "NZSE.FileId";
    /**
     * Bezeichner für Bundleparameter: Dateiname
     */
    public static final String FILENAME = "NZSE.filename";
    /**
     * Bezeichner für Bundleparameter: Verzeichnispfad
     */
    public static final String FILEPATH = "NZSE.filepath";
    /**
     * Bezeichner für Bundleparameter:
     * Activity.RESULT_OK oder Activity.RESULT_CANCEL
     */
    public static final String RESULT = "NZSE.result";
    /**
     * Bezeichner für Bundleparameter: kurze Benachrichtigung
     */
    public static final String NOTIFICATION = "NZSE.notification";

    private Handler uiServiceCallbackHandler;
    //----------------------------

    /**
     * Binder für den Client
     */
    private final IBinder mBinder = new RequestServiceBinder();

    /**
     * Klasse RequestServiceBinder für den Client.
     * Dieser Service läuft im selben Process ab wie die Client Activity;
     * daher wird kein weiterer Kommunikationsaufwand (IPC) benötigt
     */
    public class RequestServiceBinder extends Binder {
        /**
         * @return liefert Instanz, damit der Client die public Methoden nutzen kann
         */
        RequestService getService() {
            return RequestService.this;
        }

        /**
         * Zugriff auf die Downloadmethode im RequestService
         *
         * @param id       - Kennzeichnung
         * @param urlPath  - URL
         * @param filePath - lokaler Pfad
         * @param fileName - lokaler Dateiname
         */
        void runURLDownload
        (final String id, final String urlPath, final String filePath,
         final String fileName) {
            getService().runURLDownload(id, urlPath, filePath, fileName);
        }

        /**
         * Für die Rückgabe von Ergebnissen
         *
         * @param callbackHandler
         */
        public void setCallback(Handler callbackHandler) {
            getService().setCallback(callbackHandler);
        }

    } // RequestServiceBinder

    /**
     * Methode wird vom System/android aufgerufen,
     * wenn ein Service(mit startService) gestartet wird
     *
     * @param intent  - intent von startService Aufruf
     * @param flags   - flags ist 0 oder eine Kombination aus
     *                START_FLAG_REDELIVERY oder START_FLAG_RETRY
     * @param startId - eindeutiger Id
     * @return Rückgabe ist einer der Werte START_STICKY_COMPATIBILITY,
     * START_STICKY, START_NOT_STICKY oder START_REDELIVER_INTENT
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    /**
     * Callback registrieren
     *
     * @param callbackHandler - Handler im Client bereitstellen
     */
    public void setCallback(Handler callbackHandler) {
        uiServiceCallbackHandler = callbackHandler;
    }

    /**
     * Konstruktor
     */
    public RequestService() {
        super();
        Log.i("RequestService", "*** wird erzeugt! ***");
    }

    /**
     * Download als Thread organisieren und starten
     *
     * @param id       eindeutiger Bezeichner
     * @param urlPath  URL
     * @param filePath lokales Verzeichnis (Cache)
     * @param fileName Dateiname (auf dem Server und lokal)
     */
    private void runURLDownload
    (final String id, final String urlPath, final String filePath,
     final String fileName) {
        DownloadThread dThread = new DownloadThread(id, urlPath, filePath, fileName);
        dThread.start();
    }

    /**
     * Callback organisieren
     *
     * @param uniqueId   eindeutiger Bezeichner
     * @param outputPath lokaler Verzeichnispfad
     * @param result     Activity.RESULT_OK oder Activity.RESULT_CANCELED
     */
    private void deliverResults(String uniqueId, String outputPath, int result, int size) {

        Message msg = new Message();

        Bundle bundle = new Bundle();
        if (size < 0 && result == AppCompatActivity.RESULT_OK) {
            bundle.putString(NOTIFICATION, "Won't download again");
        } else {
            bundle.putString(NOTIFICATION, String.valueOf(size)); // oder "Download erfolgreich beendet.");
        }
        bundle.putString(FILEPATH, outputPath);
        bundle.putString(UNIQUEID, uniqueId);
        bundle.putInt(RESULT, result);

        msg.setData(bundle);
        uiServiceCallbackHandler.sendMessage(msg); // Callback
    }

    /**
     * Organisation des Download Thread
     */
    class DownloadThread extends Thread {
        /**
         * eindeutiger Bezeichner
         */
        private String uniqueId;
        /**
         * URL
         */
        private String urlPath;
        /**
         * Verzeichnis
         */
        private String filePath;
        /**
         * Dateiname
         */
        private String fileName;
        /**
         * Activity.RESULT_OK, Activity.RESULT_CANCELED
         */
        private int result;

        /**
         * Download
         *
         * @param id       - Id zum Image
         * @param urlPath  - URL
         * @param filePath - Verzeichnis
         * @param fileName - Dateiname
         */
        DownloadThread(final String id, final String urlPath, final String filePath, final String fileName) {
            this.result = AppCompatActivity.RESULT_CANCELED;
            this.uniqueId = id;
            this.urlPath = urlPath;
            this.filePath = filePath;
            this.fileName = fileName;
        }

        /**
         * spezifische run-Methode
         * Verbindung zum Server herstellen, Daten herunterladen und
         * in einer Datei lokal (=im eigenen Cache) abspeichern
         */
        public void run() {
            try {
                String myfolder = Environment.getExternalStorageDirectory().getPath() + "/" + filePath;

                File f;
                f = new File(myfolder);
                System.out.println("(run) Verzeichnis: " + myfolder);
                // erzeuge Verzeichnis (Folder), falls erforderlich
                if (!f.exists()) {
                    boolean b = f.mkdir();
                    if (!b)
                        Log.i("(run) NZSE: ", "Verzeichnis " + filePath + " konnte nicht erstellt werden.");
                }
                // Kontrollausgabe (wird eigentlich nicht benötigt)
                String[] dateien = new File(myfolder).list();
                for (String d : dateien)
                    System.out.println("Datei im Verzeichnis:" + d);

                FileOutputStream fos = null;

                File output = new File(
                        Environment.getExternalStorageDirectory().getPath() + "/" + this.filePath + "/" + this.fileName);

                if (output.exists()) {
                    deliverResults(this.uniqueId, output.getPath(), AppCompatActivity.RESULT_OK, -1);
//                    output.delete();
                    return;
                }

                HttpURLConnection urlConnection = null;
                InputStream inputStream = null;
                int downloadedSize = 0;
                try {
                    Log.i("(run) FILE READER", "! ... some input **********************");
                    Log.i("(run) FILE READER", "! ... " + output.getPath());
                    System.out.println("(run) urlPath: " + urlPath);
                    URL url = new URL(urlPath);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    urlConnection.connect();

                    //------------------------------------------------
                    int responseCode = urlConnection.getResponseCode();
                    Log.i("**** **** FILE responseCode:", "... " + responseCode);

                    System.out.println("... einlesen:");
                    inputStream = urlConnection.getInputStream();

                    fos = new FileOutputStream(output.getPath());

                    byte[] buffer = new byte[2048];
                    int bufferLength;
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                    }
                    Log.i("(run) Progress: ", "downloadedSize: " + downloadedSize);
                    // successfully finished
                } catch (Exception e) {
                    Log.i("(run) Exception:", " URLConnection");
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.i("(run) Exception:", " InputStream null");
                            e.printStackTrace();
                        }
                    } // stream
                    if (fos != null) {
                        try {
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            Log.i("(run) Exception (1):", " close fails");
                            e.printStackTrace();
                        }
                    } // fos
                }// finally

                this.result = AppCompatActivity.RESULT_OK;
                deliverResults(this.uniqueId, output.getAbsolutePath(), this.result, downloadedSize);
            } catch (Exception e) {
                Log.i("(run) Exception(2):", e.getMessage());
            }
        }// run
    } // DownloadThread
} // RequestService