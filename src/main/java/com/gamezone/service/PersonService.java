package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonPersistence;
import com.gamezone.validation.PersonValidator;

import java.util.ArrayList;
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
     * fulfilling the "load automatically on startup" requirement. The
     * persistence layer returns a single mixed list of Person, which this
     * service splits into customers and sellers by type.
     *
     * @param personPersistence repository used to read and write person data
     */
    public PersonService(PersonPersistence personPersistence) {
        this.personPersistence = personPersistence;
        this.customers = new ArrayList<>();
        this.sellers = new ArrayList<>();

        // Carga automática al iniciar la aplicación: se separa por tipo.
        List<Person> loadedPeople = personPersistence.loadAll();
        for (Person person : loadedPeople) {
            if (person instanceof Customer customer) {
                customers.add(customer);
            } else if (person instanceof Seller seller) {
                sellers.add(seller);
            }
        }
    }

    /**
     * Registers a new customer (already built by the caller) and
     * immediately persists the updated combined list.
     *
     * @param customer the customer to register
     * @throws InvalidDataException  if customer data is invalid
     * @throws BusinessRuleException if customer ID is already registered
     */
    public void registerCustomer(Customer customer) {
        PersonValidator.validate(customer);

        List<Person> allPeople = new ArrayList<>();
        allPeople.addAll(customers);
        allPeople.addAll(sellers);
        PersonValidator.validateUniquePerson(allPeople, customer.getId());

        customers.add(customer);
        saveAll(); // Guardado automático tras la operación.
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
        if (id == null) {
            return null;
        }
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        return null;
    }

    /**
     * Gets a customer by id or throws ResourceNotFoundException if not found.
     *
     * @param id customer id
     * @return the matching customer
     * @throws ResourceNotFoundException if customer is not found
     */
    public Customer getCustomerById(String id) {
        Customer customer = findCustomerById(id);
        PersonValidator.validatePersonExists(customer, id);
        return customer;
    }

    /**
     * Finds a seller by id. Used by the sale service/UI to validate that a
     * seller exists before registering a sale.
     *
     * @param id seller id
     * @return the matching seller, or null if none is found
     */
    public Seller findSellerById(String id) {
        if (id == null) {
            return null;
        }
        for (Seller seller : sellers) {
            if (seller.getId().equals(id)) {
                return seller;
            }
        }
        return null;
    }

    /**
     * Gets a seller by id or throws ResourceNotFoundException if not found.
     *
     * @param id seller id
     * @return the matching seller
     * @throws ResourceNotFoundException if seller is not found
     */
    public Seller getSellerById(String id) {
        Seller seller = findSellerById(id);
        PersonValidator.validatePersonExists(seller, id);
        return seller;
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
            saveAll();
        }
    }

    /**
     * Combines customers and sellers into a single list and delegates the
     * save to the persistence layer, since PersonPersistence works with one
     * mixed List&lt;Person&gt; instead of two separate lists.
     */
    private void saveAll() {
        List<Person> allPeople = new ArrayList<>();
        allPeople.addAll(customers);
        allPeople.addAll(sellers);
        personPersistence.save(allPeople);
    }
}