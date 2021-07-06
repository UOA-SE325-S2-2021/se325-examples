package se325.example12.parolee.domain;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Class to represent a Parolee. A Parolee is described by:
 * - Personal details: lastName, firstName, gender, date-of-birth, home address;
 * - Curfew: any constraints on the Parolee's location;
 * - Criminal profile: criminal history of the Parolee;
 * - Dissassociates: other Parolees who the Parolee is not permitted to be with;
 * - Movements: a timestamped history of where the Parolee has been.
 * <p>
 * A Parolee is uniquely identified by an id value of type Long.
 */
@Entity
public class Parolee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lastName;
    private String firstName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Address homeAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Conviction> convictions = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Parolee> disassociates = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Movement> movements = new HashSet<>();

    public Parolee() {
    }

    public Parolee(Long id,
                   String lastName,
                   String firstName,
                   Gender gender,
                   LocalDate dateOfBirth,
                   Address homeAddress) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.homeAddress = homeAddress;
    }

    public Parolee(String lastName,
                   String firstName,
                   Gender gender,
                   LocalDate dateOfBirth,
                   Address homeAddress) {
        this(null, lastName, firstName, gender, dateOfBirth, homeAddress);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public void addMovement(Movement movement) {
        this.movements.add(movement);
    }

    /**
     * Gets the movements, in a sorted order. Sorted by timestamp, latest first.
     */
    public List<Movement> getMovements() {
        List<Movement> movements = new ArrayList<>(this.movements);
        movements.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(movements);
    }

    public Movement getLastKnownPosition() {
        List<Movement> movements = getMovements();
        if (movements.isEmpty()) {
            return null;
        }
        return movements.get(0);
    }

    public Set<Parolee> getDisassociates() {
        return this.disassociates;
    }

    public void setDisassociates(Set<Parolee> disassociates) {
        this.disassociates = disassociates;
    }

    public Set<Conviction> getConvictions() {
        return convictions;
    }

    public void setConvictions(Set<Conviction> convictions) {
        this.convictions = convictions;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        DateTimeFormatter dOfBFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        buffer.append("Parolee: { [");
        buffer.append(id);
        buffer.append("]; ");
        if (lastName != null) {
            buffer.append(lastName);
            buffer.append(", ");
        }
        if (firstName != null) {
            buffer.append(firstName);
        }
        buffer.append("; ");
        if (gender != null) {
            buffer.append(gender);
        }
        buffer.append("; ");

        if (dateOfBirth != null) {
            buffer.append(dOfBFormatter.format(dateOfBirth));
        }
        buffer.append("\n  ");
        if (homeAddress != null) {
            buffer.append(homeAddress);
        }

        buffer.append("\n  ");
        if (convictions.isEmpty()) {
            buffer.append("No criminal profile");

        } else {
            buffer.append(convictions.size() + " convictions");
        }

        buffer.append("\n");
        buffer.append("  Dissassociates: ");
        if (disassociates.isEmpty()) {
            buffer.append("none");
        } else {
            for (Parolee disassociate : disassociates) {
                buffer.append("[");
                buffer.append(disassociate.id);
                buffer.append("]");
                buffer.append(" ");
                if (disassociate.lastName != null) {
                    buffer.append(disassociate.lastName);
                    buffer.append(", ");
                }
                if (disassociate.firstName != null) {
                    buffer.append(disassociate.firstName);
                }
                buffer.append(";");
            }
            buffer.deleteCharAt(buffer.length() - 1);
        }

        Movement lastKnownLocation = getLastKnownPosition();
        if (lastKnownLocation != null) {
            buffer.append("\n  Last known location: ");
            buffer.append(lastKnownLocation);
        }

        buffer.append(" }");

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Parolee))
            return false;
        if (obj == this)
            return true;

        Parolee other = (Parolee) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}