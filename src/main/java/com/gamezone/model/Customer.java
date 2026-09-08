package com.gamezone.model;

/**
 * Represents a customer of the store, i.e. a person who buys products.
 * <p>
 * Design note: the purchase history is NOT stored inside this class. It is
 * obtained on demand by the sale service, which searches the sale records
 * that reference this client's id. Keeping it out of Customer avoids coupling
 * the person module with the sale module (see docs/analysis.md, question 5).
 */
public class Customer extends Person {

    // Atributo particular del cliente (no lo tienen los vendedores).
    private String email;

    /**
     * Constructs a new Customer.
     *
     * @param id             unique identifier
     * @param name           full name
     * @param identification identification document number
     * @param phone          contact phone number
     * @param email          contact email address
     */
    public Customer(String id, String name, String identification, String phone, String email) {
        super(id, name, identification, phone);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoleDescription() {
        // Descripción concreta para el rol Cliente.
        return "Role: Customer | Email: " + email;
    }
}
