package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Person data (both Customers and Sellers) to and
 * from a single plain text file. This class belongs to the persistence layer
 * and is the ONLY class in the person module allowed to touch the file
 * system (see docs/analysis.md, questions 9 and 10). It knows nothing about
 * business rules; it only reads and writes data.
 */
public class PersonPersistence {

    // Un solo archivo para todas las personas. Cada línea empieza con una
    // etiqueta (CUSTOMER o SELLER) para saber qué subclase reconstruir al cargar.
    private static final String PEOPLE_FILE = "data/people.txt";
    private static final String SEPARATOR = ";";

    /**
     * Saves the given list of people (customers and sellers mixed) to the
     * people file, overwriting any previous content.
     *
     * @param people list of people to persist
     */
    public void save(List<Person> people) {
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PEOPLE_FILE))) {
            for (Person person : people) {
                if (person instanceof Customer customer) {
                    // Línea: CUSTOMER;id;name;identification;phone;email
                    writer.write(String.join(SEPARATOR,
                            "CUSTOMER",
                            customer.getId(),
                            customer.getName(),
                            customer.getIdentification(),
                            customer.getPhone(),
                            customer.getEmail()));
                    writer.newLine();
                } else if (person instanceof Seller seller) {
                    // Línea: SELLER;id;name;identification;phone;employeeCode;workShift
                    writer.write(String.join(SEPARATOR,
                            "SELLER",
                            seller.getId(),
                            seller.getName(),
                            seller.getIdentification(),
                            seller.getPhone(),
                            seller.getEmployeeCode(),
                            seller.getWorkShift()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving people: " + e.getMessage());
        }
    }

    /**
     * Loads all people (customers and sellers) stored in the people file.
     *
     * @return list of people found in the file (empty list if the file does
     *         not exist yet, e.g. on the very first execution)
     */
    public List<Person> loadAll() {
        List<Person> people = new ArrayList<>();
        File file = new File(PEOPLE_FILE);
        if (!file.exists()) {
            // Primera ejecución: aún no hay archivo, se retorna lista vacía.
            return people;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(SEPARATOR);
                String type = parts[0];
                if (type.equals("CUSTOMER")) {
                    people.add(new Customer(parts[1], parts[2], parts[3], parts[4], parts[5]));
                } else if (type.equals("SELLER")) {
                    people.add(new Seller(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading people: " + e.getMessage());
        }
        return people;
    }

    /**
     * Creates the data folder if it does not exist yet, so the first save
     * does not fail.
     */
    private void ensureDataFolderExists() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}