package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import java.util.List;

/**
 * Validator class for Person objects.
 * Ensures that all customers and sellers have complete and valid personal
 * information before being registered.
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
        if (person instanceof Customer customer) {
            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
                throw new InvalidDataException("El correo electrónico no puede estar vacío.");
            }
        }
    }

    /**
     * Validates basic person data fields.
     *
     * @param id    identifier
     * @param name  full name
     * @param email email address
     * @param phone phone number
     * @throws InvalidDataException if any field is null or blank
     */
    public static void validatePersonData(String id, String name, String email, String phone) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidDataException("El ID de la persona no puede estar vacío.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("El nombre de la persona no puede estar vacío.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidDataException("El teléfono no puede estar vacío.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidDataException("El correo electrónico no puede estar vacío.");
        }
    }

    /**
     * Overloaded method validating full person data including identification.
     *
     * @param id             identifier
     * @param name           full name
     * @param identification identity document
     * @param phone          phone number
     * @param email          email address
     * @throws InvalidDataException if any field is null or blank
     */
    public static void validatePersonData(String id, String name, String identification, String phone, String email) {
        validatePersonData(id, name, email, phone);
        if (identification == null || identification.trim().isEmpty()) {
            throw new InvalidDataException("La identificación no puede estar vacía.");
        }
    }

    /**
     * Validates that a person exists.
     *
     * @param person   person reference
     * @param personId id being looked up
     * @throws ResourceNotFoundException if person is null
     */
    public static void validatePersonExists(Person person, String personId) {
        if (person == null) {
            throw new ResourceNotFoundException("Persona con ID " + personId + " no encontrada.");
        }
    }

    /**
     * Validates that a person ID is not already used in the system.
     *
     * @param existing list of existing persons
     * @param newId    id to check for uniqueness
     * @throws InvalidDataException  if newId is blank
     * @throws BusinessRuleException if newId already exists
     */
    public static void validateUniquePerson(List<Person> existing, String newId) {
        if (newId == null || newId.trim().isEmpty()) {
            throw new InvalidDataException("El ID de la persona no puede estar vacío.");
        }
        if (existing != null) {
            for (Person person : existing) {
                if (person != null && person.getId() != null && person.getId().equals(newId)) {
                    throw new BusinessRuleException("Ya existe una persona registrada con el ID: " + newId);
                }
            }
        }
    }
}