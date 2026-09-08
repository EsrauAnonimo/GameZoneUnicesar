package com.gamezone.model;

import java.io.Serializable;

/**
 * Abstract base class representing a generic person interacting with the store.
 * It cannot be instantiated directly: every real person in the system must be
 * either a Client or a Vendor (see docs/analysis.md, question 2).
 */
public abstract class Person implements Serializable {

    // Atributos comunes a TODAS las personas (clientes y vendedores).
    // Deben ser privados: el acceso se hace solo mediante getters/setters.
    private String id;
    private String name;
    private String identification;
    private String phone;

    /**
     * Constructs a new Person with the common attributes shared by every role.
     *
     * @param id             unique identifier of the person
     * @param name           full name
     * @param identification identification document number
     * @param phone          contact phone number
     */
    public Person(String id, String name, String identification, String phone) {
        this.id = id;
        this.name = name;
        this.identification = identification;
        this.phone = phone;
    }

    // ---------- Getters y setters (encapsulamiento obligatorio) ----------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns a description specific to the person's role (Client or Vendor).
     * Declared as abstract on purpose: it forces every subclass to provide its
     * own implementation, which is the polymorphism mechanism requested by the
     * analysis question about role-specific behavior.
     *
     * @return a role-specific description of the person
     */
    // Método abstracto: obliga a Client y Vendor a "llenarlo" cada uno a su manera.
    // Esto es lo que se debe explicar en la sustentación como ejemplo de polimorfismo.
    public abstract String getRoleDescription();

    /**
     * Builds a complete textual summary combining the common attributes with
     * the role-specific description provided by the subclass.
     *
     * @return full summary of the person
     */
    public String getFullSummary() {
        // Aquí se combina lo común (de Person) con lo específico (getRoleDescription()),
        // sin que esta clase necesite saber si es un Client o un Vendor.
        return "ID: " + id + " | Name: " + name + " | Identification: " + identification
                + " | Phone: " + phone + " | " + getRoleDescription();
    }
}