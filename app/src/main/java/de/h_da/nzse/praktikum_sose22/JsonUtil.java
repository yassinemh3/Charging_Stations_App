package de.h_da.nzse.praktikum_sose22;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool for writing and reading the json data
 */
public class JsonUtil {

    /**
     * Stores the hashes of the stations into the JSON file.
     * The filename is defined in strings.xml
     *
     * @param c
     * @param stations
     * @throws JSONException
     * @throws IOException
     */
    public static void storeStationData(Context c, List<Integer> stations) throws JSONException, IOException {
        JSONArray jsonArray = new JSONArray();

        for (int s : stations) {
            jsonArray.put(s);
        }
        storeData(c, jsonArray.toString());
    }

    /**
     * Writes a given string into a file
     *
     * @param c
     * @param data
     * @throws IOException
     */
    private static void storeData(Context c, String data) throws IOException {
        File f = new File(c.getFilesDir(), c.getResources().getString(R.string.filename));

        FileOutputStream fOut = new FileOutputStream(f);
        OutputStreamWriter outWriter = new OutputStreamWriter(fOut);

        outWriter.append(data);
        outWriter.close();
    }

    /**
     * Reads the data from the file.
     * If the file specified in strings.xml doesn't exist, the sample data in the resources is read.
     *
     * @param c
     * @return ArrayList of hashes read from the file
     * @throws JSONException
     * @throws IOException
     */
    public static ArrayList<Integer> readStationData(Context c) throws JSONException, IOException {
        JSONArray hashes = new JSONArray(JsonUtil.readData(c));
        return stationListFromData(hashes);
    }

    /**
     * Reads a string from the file specified in strings.xml
     * or the sample data in the resources, if the file doesn't exist
     *
     * @param c
     * @return String containing the read data
     * @throws IOException
     */
    private static String readData(Context c) throws IOException {
        File f = new File(c.getFilesDir(), c.getResources().getString(R.string.filename));
        InputStream is;
        if (f.exists()) {
            is = new FileInputStream(f);
        } else {
            return "";
        }
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }

    /**
     * Converts a JSONArray into a ArrayList of hashes
     *
     * @param hashes
     * @return ArrayList of hashes
     * @throws JSONException
     */
    private static ArrayList<Integer> stationListFromData(JSONArray hashes) throws JSONException {
        ArrayList<Integer> stations = new ArrayList<>();
        for (int i = 0; i < hashes.length(); i++) {
            stations.add(hashes.getInt(i));
        }
        return stations;
    }

}
