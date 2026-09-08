package com.gamezone.model;

/**
 * Represents a seller (employee) who attends clients and registers sales.
 */
public class Seller extends Person {

    // Atributos particulares del vendedor (no los tiene el cliente).
    private String employeeCode;
    private String workshift;

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
    public Seller(String id, String name, String identification, String phone,
                   String employeeCode, String shift) {
        super(id, name, identification, phone);
        this.employeeCode = employeeCode;
        this.workshift = shift;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getWorkShift() {
        return workshift;
    }

    public void setWorkShift(String shift) {
        this.workshift = shift;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoleDescription() {
        String workShift = null;
        // Descripción concreta para el rol Vendedor.
        return "Role: Seller | Employee Code: " + employeeCode + " | Shift: " + workShift;
    }
}
