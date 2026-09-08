package com.gamezone.persistence;

import com.gamezone.model.Customer;
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
 * Handles saving and loading Customer and Seller data to and from plain text
 * files. This class belongs to the persistence layer and is the ONLY class
 * in the person module allowed to touch the file system (see
 * docs/analysis.md, questions 9 and 10). It knows nothing about business
 * rules; it only reads and writes data.
 */
public class PersonPersistence {

    // Rutas de los archivos de datos. Formato elegido: texto plano separado por ";".
    private static final String CLIENTS_FILE = "data/clients.txt";
    private static final String VENDORS_FILE = "data/vendors.txt";
    private static final String SEPARATOR = ";";

    /**
     * Saves the given list of customers to the customers file, overwriting
     * any previous content.
     *
     * @param customers list of customers to persist
     */
    public void saveClients(List<Customer> customers) {
        // Cada cliente se escribe como una línea: id;name;identification;phone;email
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CLIENTS_FILE))) {
            for (Customer customer : customers) {
                writer.write(String.join(SEPARATOR,
                        customer.getId(),
                        customer.getName(),
                        customer.getIdentification(),
                        customer.getPhone(),
                        customer.getEmail()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving customers: " + e.getMessage());
        }
    }

    /**
     * Loads all customers stored in the customers file.
     *
     * @return list of customers found in the file (empty list if the file
     *         does not exist yet, e.g. on the very first execution)
     */
    public List<Customer> loadClients() {
        List<Customer> customers = new ArrayList<>();
        File file = new File(CLIENTS_FILE);
        if (!file.exists()) {
            // Primera ejecución: aún no hay archivo, se retorna lista vacía.
            return customers;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(SEPARATOR);
                customers.add(new Customer(parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        } catch (IOException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
        return customers;
    }

    /**
     * Saves the given list of sellers to the sellers file, overwriting any
     * previous content.
     *
     * @param sellers list of sellers to persist
     */
    public void saveVendors(List<Seller> sellers) {
        // Cada vendedor se escribe como una línea: id;name;identification;phone;employeeCode;workShift
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VENDORS_FILE))) {
            for (Seller seller : sellers) {
                writer.write(String.join(SEPARATOR,
                        seller.getId(),
                        seller.getName(),
                        seller.getIdentification(),
                        seller.getPhone(),
                        seller.getEmployeeCode(),
                        seller.getWorkShift()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving sellers: " + e.getMessage());
        }
    }

    /**
     * Loads all sellers stored in the sellers file.
     *
     * @return list of sellers found in the file (empty list if the file does
     *         not exist yet)
     */
    public List<Seller> loadVendors() {
        List<Seller> sellers = new ArrayList<>();
        File file = new File(VENDORS_FILE);
        if (!file.exists()) {
            return sellers;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(SEPARATOR);
                sellers.add(new Seller(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
            }
        } catch (IOException e) {
            System.out.println("Error loading sellers: " + e.getMessage());
        }
        return sellers;
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