package se325.example12.parolee.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se325.example12.parolee.jackson.LocalDateTimeDeserializer;
import se325.example12.parolee.jackson.LocalDateTimeSerializer;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Class to represent a Parolee's movement. A Movement instance stores a
 * timestamp and a latitude/longitude position. Movement objects are immutable.
 */
@Embeddable
@Access(AccessType.FIELD)
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
     * @param geoPosition  the position of the movement
     */
    @JsonCreator
    public Movement(@JsonProperty("timestamp") LocalDateTime timestamp, @JsonProperty("geoPosition") GeoPosition geoPosition) {
        this.timestamp = timestamp;
        this.geoPosition = geoPosition;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public GeoPosition getGeoPosition() {
        return geoPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return toString().equals(o.toString());
//        Movement movement = (Movement) o;
//        return timestamp.isEqual(movement.timestamp) && Objects.equals(geoPosition, movement.geoPosition);
    }

    @Override
    public int hashCode() {
//        return Objects.hash(timestamp, geoPosition);
        return Objects.hash(toString());
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