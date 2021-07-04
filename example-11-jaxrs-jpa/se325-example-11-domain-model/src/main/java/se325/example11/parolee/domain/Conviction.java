package se325.example11.parolee.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se325.example11.parolee.jackson.LocalDateDeserializer;
import se325.example11.parolee.jackson.LocalDateSerializer;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Class to represent a particular criminal conviction. A conviction is made up
 * of one or more CriminalProfile.Offence tags, the date of conviction, and a
 * description of the conviction. Convictions are immutable.
 */
@Entity
public class Conviction {

    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    private Set<Offence> offenceTags;

    private LocalDate date;

    private String description;

    public Conviction() {
        this(null, null);
    }

    @JsonCreator
    public Conviction(@JsonProperty("date") LocalDate convictionDate,
                      @JsonProperty("description") String description,
                      @JsonProperty("offenceTags") Offence... offenceTags) {
        date = convictionDate;
        this.description = description;
        this.offenceTags = new HashSet<>(Arrays.asList(offenceTags));
    }

    public Set<Offence> getOffenceTags() {
        return Collections.unmodifiableSet(offenceTags);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conviction that = (Conviction) o;
        return Objects.equals(offenceTags, that.offenceTags) && Objects.equals(date, that.date) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offenceTags, date, description);
    }
}
