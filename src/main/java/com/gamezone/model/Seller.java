package com.gamezone.model;

/**
 * Represents a seller (employee) who attends customers and registers sales.
 */
public class Seller extends Person {

    // Atributos particulares del vendedor (no los tiene el cliente).
    private String employeeCode;
    private String workShift;

    /**
     * Constructs a new Seller.
     *
     * @param id             unique identifier
     * @param name           full name
     * @param identification identification document number
     * @param phone          contact phone number
     * @param employeeCode   employee code assigned by the store
     * @param workShift      assigned work shift
     */
    public Seller(String id, String name, String identification, String phone,
                   String employeeCode, String workShift) {
        super(id, name, identification, phone);
        this.employeeCode = employeeCode;
        this.workShift = workShift;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getWorkShift() {
        return workShift;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoleDescription() {
        // Descripción concreta para el rol Vendedor.
        return "Role: Seller | Employee Code: " + employeeCode + " | Shift: " + workShift;
    }
}