package se325.example11.parolee.services;

import se325.example11.parolee.domain.Parolee;

/**
 * Helper class to convert between domain-model and DTO objects representing
 * Parolees.
 */
public class ParoleeMapper {

    static Parolee toDomainModel(se325.example11.parolee.dto.Parolee dtoParolee) {
        Parolee fullParolee = new Parolee(dtoParolee.getId(),
                dtoParolee.getLastName(),
                dtoParolee.getFirstName(),
                dtoParolee.getGender(),
                dtoParolee.getDateOfBirth(),
                dtoParolee.getHomeAddress(),
                dtoParolee.getCurfew());
        return fullParolee;
    }

    static se325.example11.parolee.dto.Parolee toDto(Parolee parolee) {
        se325.example11.parolee.dto.Parolee dtoParolee =
                new se325.example11.parolee.dto.Parolee(
                        parolee.getId(),
                        parolee.getLastName(),
                        parolee.getFirstName(),
                        parolee.getGender(),
                        parolee.getDateOfBirth(),
                        parolee.getHomeAddress(),
                        parolee.getCurfew(),
                        parolee.getLastKnownPosition());
        return dtoParolee;

    }
}
