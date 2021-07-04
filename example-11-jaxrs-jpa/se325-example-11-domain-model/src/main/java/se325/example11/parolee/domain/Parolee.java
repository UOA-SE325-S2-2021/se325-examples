package se325.example11.parolee.domain;

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
    @GeneratedValue
    private long id;
    private String lastName;
    private String firstName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Address homeAddress;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Curfew curfew;
    private CriminalProfile criminalProfile;
    @OneToMany
    private Set<Parolee> dissassociates;
    @ElementCollection
    private List<Movement> movements;

    public Parolee() {
    }

    public Parolee(long id,
                   String lastName,
                   String firstName,
                   Gender gender,
                   LocalDate dateOfBirth,
                   Address address,
                   Curfew curfew) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        homeAddress = address;
        this.curfew = curfew;
        dissassociates = new HashSet<>();
        movements = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Curfew getCurfew() {
        return curfew;
    }

    public void setCurfew(Curfew curfew) {
        this.curfew = curfew;
    }

    public CriminalProfile getCriminalProfile() {
        return criminalProfile;
    }

    public void setCriminalProfile(CriminalProfile profile) {
        criminalProfile = profile;
    }

    public void addMovement(Movement movement) {
        // Store the new movement.
        movements.add(movement);

        // Ensure that movements are sorted in descending order (i.e. that the
        // most recent movement appears first.
        Collections.sort(movements, Collections.reverseOrder());
    }

    public List<Movement> getMovements() {
        // Returns the Parolee's movements in a read-only collection.
        return Collections.unmodifiableList(movements);
    }

    public Movement getLastKnownPosition() {
        Movement movement = null;

        if (!movements.isEmpty()) {
            movement = movements.get(0);
        }
        return movement;
    }

    public void addDissassociate(Parolee parolee) {
        dissassociates.add(parolee);
    }

    public void removeDissassociate(Parolee parolee) {
        dissassociates.remove(parolee);
    }

    public Set<Parolee> getDissassociates() {
        return Collections.unmodifiableSet(dissassociates);
    }

    public void updateDissassociates(Set<Parolee> dissassociates) {
        this.dissassociates = dissassociates;
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

        if (curfew != null) {
            buffer.append("\n  Curfew from ");
            buffer.append(timeFormatter.format(curfew.getStartTime()));
            buffer.append(" to ");
            buffer.append(timeFormatter.format(curfew.getEndTime()));
            buffer.append(" @ ");

            if (homeAddress != null && homeAddress.equals(curfew.getConfinementAddress())) {
                buffer.append("home");
            } else {
                buffer.append(curfew.getConfinementAddress());
            }
        } else {
            buffer.append("No curfew conditions");
        }

        buffer.append("\n  ");
        if (criminalProfile != null) {
            buffer.append(criminalProfile);
        } else {
            buffer.append("No criminal profile");
        }

        buffer.append("\n");
        buffer.append("  Dissassociates: ");
        if (dissassociates.isEmpty()) {
            buffer.append("none");
        } else {
            for (Parolee dissassociate : dissassociates) {
                buffer.append("[");
                buffer.append(dissassociate.id);
                buffer.append("]");
                buffer.append(" ");
                if (dissassociate.lastName != null) {
                    buffer.append(dissassociate.lastName);
                    buffer.append(", ");
                }
                if (dissassociate.firstName != null) {
                    buffer.append(dissassociate.firstName);
                }
                buffer.append(";");
            }
            buffer.deleteCharAt(buffer.length() - 1);
        }

        if (!movements.isEmpty()) {
            buffer.append("\n  Last known location: ");
            Movement lastMovement = movements.get(0);
            buffer.append(lastMovement);
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