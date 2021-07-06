package se325.example11.parolee.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Class to represent a geographic location in terms of latitude and longitude.
 * GeoPosition objects are immutable.
 */
public class GeoPosition {

    private double latitude;

    private double longitude;

    protected GeoPosition() {
    }

    /**
     * Creates a new {@link GeoPosition}. Demonstrates how we can tell Jackson to use this constructor rather than any
     * setters when creating new instances. We need this here because GeoPosition objects are immutable (and therefore
     * have no setters).
     *
     * @param lat the latitude
     * @param lng the longitude
     */
    @JsonCreator
    public GeoPosition(@JsonProperty("latitude") double lat, @JsonProperty("longitude") double lng) {
        latitude = lat;
        longitude = lng;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoPosition that = (GeoPosition) o;
        return Double.compare(that.latitude, latitude) == 0 && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("(");
        buffer.append(latitude);
        buffer.append(",");
        buffer.append(longitude);
        buffer.append(")");

        return buffer.toString();
    }
}
