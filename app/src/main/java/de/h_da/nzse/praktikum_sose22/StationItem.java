package de.h_da.nzse.praktikum_sose22;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class StationItem implements Parcelable {
    private final static String OPERATOR = "stationOperator";
    private final static String STREET = "stationStreet";

    String operator;
    String street;
    String streetNumber;
    int postcode;
    String location;
    Float latitude;
    Float longitude;
    int outputPower;
    int status;
    boolean isFavorite = false;
    public static final int DEFECT = -1;
    public static final int READY = 1;
    public static final int IN_USE = 0;


    public StationItem(String operator, String street, String streetNumber, int postcode, String location, Float latitude, Float longitude, int outputPower) {
        this.operator = operator;
        this.street = street;
        this.streetNumber = streetNumber;
        this.postcode = postcode;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.outputPower = outputPower;
        status = READY;
    }

    protected StationItem(Parcel in) {
        operator = in.readString();
        street = in.readString();
        streetNumber = in.readString();
        postcode = in.readInt();
        location = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readFloat();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readFloat();
        }
        outputPower = in.readInt();
    }

    public static final Creator<StationItem> CREATOR = new Creator<StationItem>() {
        @Override
        public StationItem createFromParcel(Parcel in) {
            return new StationItem(in);
        }

        @Override
        public StationItem[] newArray(int size) {
            return new StationItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationItem that = (StationItem) o;
        return latitude.equals(that.latitude) && longitude.equals(that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    public String getOperator() {
        return operator;
    }

    public String getStreet() {
        return street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public int getPostcode() {
        return postcode;
    }

    public String getLocation() {
        return location;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public int getOutputPower() {
        return outputPower;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(operator);
        parcel.writeString(street);
        parcel.writeInt(postcode);
        parcel.writeInt(outputPower);
        parcel.writeString(location);
        parcel.writeFloat(latitude);
        parcel.writeFloat(longitude);
        parcel.writeString(streetNumber);
        parcel.writeInt(status);
        parcel.writeInt(isFavorite ? 1 : 0);
    }


}
