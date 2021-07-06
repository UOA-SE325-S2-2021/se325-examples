package se325.example12.parolee.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Class to represent a geographic location in terms of latitude and longitude.
 * GeoPosition objects are immutable.
 */
@Embeddable
public class GeoPosition {

    @Column(nullable = true)
    private double latitude;

    @Column(nullable = true)
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

        double latDiff = Math.abs(that.latitude - latitude);
        double lngDiff = Math.abs(that.longitude - longitude);
        return latDiff < 1e-10 && lngDiff < 1e-10;
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
