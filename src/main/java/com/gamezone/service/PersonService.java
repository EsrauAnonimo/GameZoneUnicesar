package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Vendor;
import com.gamezone.persistence.PersonRepository;

import java.util.List;
import java.util.UUID;

/**
 * Contains the business rules of the person module (clients and vendors).
 * This is the only class in the module allowed to call PersonRepository; the
 * UI layer must always go through this service, never directly to
 * persistence (see docs/analysis.md, questions 9-11).
 */
public class PersonService {

    private final PersonRepository personRepository;
    private List<Client> clients;
    private List<Vendor> vendors;

    /**
     * Creates the service and immediately loads the previously stored data,
     * fulfilling the "load automatically on startup" requirement.
     *
     * @param personRepository repository used to read and write person data
     */
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
        // Carga automática al iniciar la aplicación.
        this.clients = personRepository.loadClients();
        this.vendors = personRepository.loadVendors();
    }

    /**
     * Registers a new client and immediately persists the updated list.
     *
     * @param name           client's full name
     * @param identification client's identification document
     * @param phone          client's phone number
     * @param email          client's email
     * @return the newly created client
     */
    public Client registerClient(String name, String identification, String phone, String email) {
        String id = UUID.randomUUID().toString();
        Client client = new Client(id, name, identification, phone, email);
        clients.add(client);
        personRepository.saveClients(clients); // Guardado automático tras la operación.
        return client;
    }

    /**
     * Returns the list of all registered clients.
     *
     * @return list of clients
     */
    public List<Client> listClients() {
        return clients;
    }

    /**
     * Returns the list of all registered vendors.
     *
     * @return list of vendors
     */
    public List<Vendor> listVendors() {
        return vendors;
    }

    /**
     * Finds a client by id. Used by the sale service/UI to validate that a
     * client exists before registering a sale.
     *
     * @param id client id
     * @return the matching client, or null if none is found
     */
    public Client findClientById(String id) {
        return clients.stream()
                .filter(client -> client.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a vendor by id. Used by the sale service/UI to validate that a
     * vendor exists before registering a sale.
     *
     * @param id vendor id
     * @return the matching vendor, or null if none is found
     */
    public Vendor findVendorById(String id) {
        return vendors.stream()
                .filter(vendor -> vendor.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Preloads at least three vendors on the very first execution, as
     * required by the assignment (vendors are already hired and are not
     * registered through the UI). Call this once from Main/startup, after
     * building the service.
     */
    public void preloadVendorsIfEmpty() {
        if (vendors.isEmpty()) {
            // Datos precargados: vendedores que ya trabajan en la tienda.
            vendors.add(new Vendor(UUID.randomUUID().toString(), "Laura Gomez",
                    "1065123456", "3001234567", "V001", "Morning"));
            vendors.add(new Vendor(UUID.randomUUID().toString(), "Carlos Perez",
                    "1065123457", "3007654321", "V002", "Afternoon"));
            vendors.add(new Vendor(UUID.randomUUID().toString(), "Maria Rodriguez",
                    "1065123458", "3009876543", "V003", "Evening"));
            personRepository.saveVendors(vendors);
        }
    }
}
