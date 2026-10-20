package com.gamezone.validation;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Person;

/**
 * Validator class for Person objects.
 * Contains static methods to ensure person data is complete.
 * 
 * @author Dev 1
 */
public class PersonValidator {

    /**
     * Validates the essential fields of a person.
     * 
     * @param person The person to validate
     * @throws InvalidDataException If any validation rule fails
     */
    public static void validate(Person person) {
        if (person == null) {
            throw new InvalidDataException("La persona no puede ser nula.");
        }
        if (person.getId() == null || person.getId().trim().isEmpty()) {
            throw new InvalidDataException("El ID de la persona no puede estar vacío.");
        }
        if (person.getName() == null || person.getName().trim().isEmpty()) {
            throw new InvalidDataException("El nombre de la persona no puede estar vacío.");
        }
        if (person.getIdentification() == null || person.getIdentification().trim().isEmpty()) {
            throw new InvalidDataException("La identificación no puede estar vacía.");
        }
        if (person.getPhone() == null || person.getPhone().trim().isEmpty()) {
            throw new InvalidDataException("El teléfono no puede estar vacío.");
        }
    }
}