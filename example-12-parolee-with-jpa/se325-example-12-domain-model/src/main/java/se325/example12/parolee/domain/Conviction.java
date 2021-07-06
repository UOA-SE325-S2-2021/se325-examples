package se325.example12.parolee.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se325.example12.parolee.jackson.LocalDateDeserializer;
import se325.example12.parolee.jackson.LocalDateSerializer;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Class to represent a particular criminal conviction. A conviction is made up
 * of one or more CriminalProfile.Offence tags, the date of conviction, and a
 * description of the conviction. Convictions are immutable.
 */
@Embeddable
@Access(AccessType.FIELD)
public class Conviction {

    private Offence offence;

    private LocalDate date;

    private String description;

    public Conviction() {}

    @JsonCreator
    public Conviction(@JsonProperty("date") LocalDate convictionDate,
                      @JsonProperty("description") String description,
                      @JsonProperty("offence") Offence offence) {
        date = convictionDate;
        this.description = description;
        this.offence = offence;
    }

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public Offence getOffence() {
        return offence;
    }

    @Override
    public String toString() {
        return "Conviction{" +
                "offence=" + offence +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conviction that = (Conviction) o;
        return Objects.equals(offence, that.offence) && Objects.equals(date, that.date) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offence, date, description);
    }
}
