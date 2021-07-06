package se325.example12.parolee.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se325.example12.parolee.domain.*;
import se325.example12.parolee.jackson.LocalDateDeserializer;
import se325.example12.parolee.jackson.LocalDateSerializer;

import java.time.LocalDate;
import java.util.Objects;

public class ParoleeDTO {

    private Long id;
    private String lastName;
    private String firstName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Address homeAddress;

    private Movement lastKnownPosition;

    public ParoleeDTO() {

    }

    public ParoleeDTO(String lastName, String firstName, Gender gender, LocalDate dateOfBirth, Address homeAddress) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.homeAddress = homeAddress;
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

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
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

    public Movement getLastKnownPosition() {
        return lastKnownPosition;
    }

    public void setLastKnownPosition(Movement lastKnownPosition) {
        this.lastKnownPosition = lastKnownPosition;
    }

    @Override
    public String toString() {
        return "ParoleeDTO{" +
                "id=" + id +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", homeAddress=" + homeAddress +
                ", lastKnownPosition=" + lastKnownPosition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParoleeDTO that = (ParoleeDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(lastName, that.lastName)
                && Objects.equals(firstName, that.firstName) && gender == that.gender
                && Objects.equals(dateOfBirth, that.dateOfBirth) && Objects.equals(homeAddress, that.homeAddress)
                && Objects.equals(lastKnownPosition, that.lastKnownPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastName, firstName, gender, dateOfBirth, homeAddress, lastKnownPosition);
    }

    public static ParoleeDTO fromDomain(Parolee domainParolee) {
        ParoleeDTO dtoParolee = new ParoleeDTO();
        dtoParolee.setId(domainParolee.getId());
        dtoParolee.setDateOfBirth(domainParolee.getDateOfBirth());
        dtoParolee.setGender(domainParolee.getGender());
        dtoParolee.setFirstName(domainParolee.getFirstName());
        dtoParolee.setLastName(domainParolee.getLastName());
        dtoParolee.setHomeAddress(domainParolee.getHomeAddress());

        dtoParolee.setLastKnownPosition(domainParolee.getLastKnownPosition());

        return dtoParolee;
    }

    public Parolee toDomain() {
        Parolee domainParolee = new Parolee();
        updateDomain(domainParolee);
        return domainParolee;
    }

    public void updateDomain(Parolee domainParolee) {
        domainParolee.setId(id);
        domainParolee.setDateOfBirth(dateOfBirth);
        domainParolee.setGender(gender);
        domainParolee.setFirstName(firstName);
        domainParolee.setLastName(lastName);
        domainParolee.setHomeAddress(homeAddress);
    }
}
