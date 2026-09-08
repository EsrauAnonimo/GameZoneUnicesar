package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Vendor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Client and Vendor data to and from plain text
 * files. This class belongs to the persistence layer and is the ONLY class
 * in the person module allowed to touch the file system (see
 * docs/analysis.md, questions 9 and 10). It knows nothing about business
 * rules; it only reads and writes data.
 */
public class PersonRepository {

    // Rutas de los archivos de datos. Formato elegido: texto plano separado por ";".
    private static final String CLIENTS_FILE = "data/clients.txt";
    private static final String VENDORS_FILE = "data/vendors.txt";
    private static final String SEPARATOR = ";";

    /**
     * Saves the given list of clients to the clients file, overwriting any
     * previous content.
     *
     * @param clients list of clients to persist
     */
    public void saveClients(List<Customer> clients) {
        // Cada cliente se escribe como una línea: id;name;identification;phone;email
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CLIENTS_FILE))) {
            for (Customer client : clients) {
                writer.write(String.join(SEPARATOR,
                        client.getId(),
                        client.getName(),
                        client.getIdentification(),
                        client.getPhone(),
                        client.getEmail()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving clients: " + e.getMessage());
        }
    }

    /**
     * Loads all clients stored in the clients file.
     *
     * @return list of clients found in the file (empty list if the file does
     *         not exist yet, e.g. on the very first execution)
     */
    public List<Customer> loadClients() {
        List<Customer> clients = new ArrayList<>();
        File file = new File(CLIENTS_FILE);
        if (!file.exists()) {
            // Primera ejecución: aún no hay archivo, se retorna lista vacía.
            return clients;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(SEPARATOR);
                clients.add(new Customer(parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        } catch (IOException e) {
            System.out.println("Error loading clients: " + e.getMessage());
        }
        return clients;
    }

    /**
     * Saves the given list of vendors to the vendors file, overwriting any
     * previous content.
     *
     * @param vendors list of vendors to persist
     */
    public void saveVendors(List<Vendor> vendors) {
        // Cada vendedor se escribe como una línea: id;name;identification;phone;employeeCode;shift
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VENDORS_FILE))) {
            for (Vendor vendor : vendors) {
                writer.write(String.join(SEPARATOR,
                        vendor.getId(),
                        vendor.getName(),
                        vendor.getIdentification(),
                        vendor.getPhone(),
                        vendor.getEmployeeCode(),
                        vendor.getShift()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving vendors: " + e.getMessage());
        }
    }

    /**
     * Loads all vendors stored in the vendors file.
     *
     * @return list of vendors found in the file (empty list if the file does
     *         not exist yet)
     */
    public List<Vendor> loadVendors() {
        List<Vendor> vendors = new ArrayList<>();
        File file = new File(VENDORS_FILE);
        if (!file.exists()) {
            return vendors;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(SEPARATOR);
                vendors.add(new Vendor(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
            }
        } catch (IOException e) {
            System.out.println("Error loading vendors: " + e.getMessage());
        }
        return vendors;
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