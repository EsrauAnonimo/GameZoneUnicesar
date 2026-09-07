package com.gamezone.model;

/**
 * Represents a vendor (employee) who attends clients and registers sales.
 */
public class Vendor extends Person {

    // Atributos particulares del vendedor (no los tiene el cliente).
    private String employeeCode;
    private String shift;

    /**
     * Constructs a new Vendor.
     *
     * @param id             unique identifier
     * @param name           full name
     * @param identification identification document number
     * @param phone          contact phone number
     * @param employeeCode   employee code assigned by the store
     * @param shift          assigned work shift
     */
    public Vendor(String id, String name, String identification, String phone,
                   String employeeCode, String shift) {
        super(id, name, identification, phone);
        this.employeeCode = employeeCode;
        this.shift = shift;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoleDescription() {
        // Descripción concreta para el rol Vendedor.
        return "Role: Vendor | Employee Code: " + employeeCode + " | Shift: " + shift;
    }
}
