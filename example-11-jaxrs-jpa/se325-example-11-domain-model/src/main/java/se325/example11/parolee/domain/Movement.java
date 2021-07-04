package se325.example11.parolee.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se325.example11.parolee.jackson.LocalDateTimeDeserializer;
import se325.example11.parolee.jackson.LocalDateTimeSerializer;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Class to represent a Parolee's movement. A Movement instance stores a
 * timestamp and a latitude/longitude position. Movement objects are immutable.
 */
@Embeddable
public class Movement implements Comparable<Movement> {

    private LocalDateTime timestamp;

    private GeoPosition geoPosition;

    protected Movement() {
    }

    /**
     * Creates a new {@link Movement}. Demonstrates how we can tell Jackson to use this constructor rather than any
     * setters when creating new instances. We need this here because Movement objects are immutable (and therefore
     * have no setters).
     *
     * @param timestamp the time of the movement
     * @param position  the position of the movement
     */
    @JsonCreator
    public Movement(@JsonProperty("timestamp") LocalDateTime timestamp, @JsonProperty("position") GeoPosition position) {
        this.timestamp = timestamp;
        geoPosition = position;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public GeoPosition getGeoPosition() {
        return geoPosition;
    }

    public void setGeoPosition(GeoPosition geoPosition) {
        this.geoPosition = geoPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movement movement = (Movement) o;
        return Objects.equals(timestamp, movement.timestamp) && Objects.equals(geoPosition, movement.geoPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, geoPosition);
    }

    @Override
    public int compareTo(Movement movement) {
        return timestamp.compareTo(movement.timestamp);
    }

    @Override
    public String toString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        StringBuffer buffer = new StringBuffer();

        buffer.append(geoPosition);
        buffer.append(" @ ");
        buffer.append(timeFormatter.format(timestamp));
        buffer.append(" on ");
        buffer.append(dateFormatter.format(timestamp));

        return buffer.toString();
    }
}