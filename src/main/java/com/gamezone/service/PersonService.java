package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonPersistence;

import java.util.List;
import java.util.UUID;

/**
 * Contains the business rules of the person module (customers and sellers).
 * This is the only class in the module allowed to call PersonPersistence;
 * the UI layer must always go through this service, never directly to
 * persistence (see docs/analysis.md, questions 9-11).
 */
public class PersonService {

    private final PersonPersistence personPersistence;
    private List<Customer> customers;
    private List<Seller> sellers;

    /**
     * Creates the service and immediately loads the previously stored data,
     * fulfilling the "load automatically on startup" requirement.
     *
     * @param personPersistence repository used to read and write person data
     */
    public PersonService(PersonPersistence personPersistence) {
        this.personPersistence = personPersistence;
        // Carga automática al iniciar la aplicación.
        this.customers = personPersistence.loadClients();
        this.sellers = personPersistence.loadVendors();
    }

    /**
     * Registers a new customer and immediately persists the updated list.
     *
     * @param name           customer's full name
     * @param identification customer's identification document
     * @param phone          customer's phone number
     * @param email          customer's email
     * @return the newly created customer
     */
    public Customer registerCustomer(String name, String identification, String phone, String email) {
        String id = UUID.randomUUID().toString();
        Customer customer = new Customer(id, name, identification, phone, email);
        customers.add(customer);
        personPersistence.saveClients(customers); // Guardado automático tras la operación.
        return customer;
    }

    /**
     * Returns the list of all registered customers.
     *
     * @return list of customers
     */
    public List<Customer> listCustomers() {
        return customers;
    }

    /**
     * Returns the list of all registered sellers.
     *
     * @return list of sellers
     */
    public List<Seller> listSellers() {
        return sellers;
    }

    /**
     * Finds a customer by id. Used by the sale service/UI to validate that a
     * customer exists before registering a sale.
     *
     * @param id customer id
     * @return the matching customer, or null if none is found
     */
    public Customer findCustomerById(String id) {
        return customers.stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a seller by id. Used by the sale service/UI to validate that a
     * seller exists before registering a sale.
     *
     * @param id seller id
     * @return the matching seller, or null if none is found
     */
    public Seller findSellerById(String id) {
        return sellers.stream()
                .filter(seller -> seller.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Preloads at least three sellers on the very first execution, as
     * required by the assignment (sellers are already hired and are not
     * registered through the UI). Call this once from Main/startup, after
     * building the service.
     */
    public void preloadVendorsIfEmpty() {
        if (sellers.isEmpty()) {
            // Datos precargados: vendedores que ya trabajan en la tienda.
            sellers.add(new Seller(UUID.randomUUID().toString(), "Laura Gomez",
                    "1065123456", "3001234567", "V001", "Morning"));
            sellers.add(new Seller(UUID.randomUUID().toString(), "Carlos Perez",
                    "1065123457", "3007654321", "V002", "Afternoon"));
            sellers.add(new Seller(UUID.randomUUID().toString(), "Maria Rodriguez",
                    "1065123458", "3009876543", "V003", "Evening"));
            personPersistence.saveVendors(sellers);
        }
    }
}