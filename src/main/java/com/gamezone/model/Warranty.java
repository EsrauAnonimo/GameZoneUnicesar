package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract class that represents a warranty in the GameZone system.
 * A warranty is associated with a product and a sale, and has a duration
 * and additional cost that must be defined by subclasses.
 * 
 * @author Dev 1
 */
public abstract class Warranty {

    private String id;
    private Product product;
    private Sale sale;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Constructor for a Warranty.
     * Calculates the end date by adding the duration in months to the start date.
     * 
     * @param id Unique code that identifies the warranty
     * @param product The product covered by the warranty
     * @param sale The sale associated with the warranty
     * @param startDate Date when the warranty starts
     */
    public Warranty(String id, Product product, Sale sale, LocalDate startDate) {
        this.id = id;
        this.product = product;
        this.sale = sale;
        this.startDate = startDate;
        this.endDate = startDate.plusMonths(getDurationInMonths());
    }

    /**
     * Gets the warranty ID.
     * 
     * @return The warranty ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the product covered by the warranty.
     * 
     * @return The product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Gets the sale associated with the warranty.
     * 
     * @return The sale
     */
    public Sale getSale() {
        return sale;
    }

    /**
     * Gets the start date of the warranty.
     * 
     * @return The start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Gets the end date of the warranty.
     * 
     * @return The end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Checks if the warranty is active on a given date.
     * 
     * @param date The date to check
     * @return true if the warranty is active, false otherwise
     */
    public boolean isActive(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Generates a formatted certificate for the warranty.
     * The certificate is written in Spanish.
     * 
     * @return A formatted string with the warranty certificate
     */
    public String generateWarrantyCertificate() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== CERTIFICADO DE GARANTÍA =====\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Tipo: ").append(getWarrantyType()).append("\n");
        sb.append("Producto: ").append(product != null ? product.getTitle() : "N/A").append("\n");
        sb.append("Fecha de inicio: ").append(startDate).append("\n");
        sb.append("Fecha de fin: ").append(endDate).append("\n");
        sb.append("Duración: ").append(getDurationInMonths()).append(" meses\n");
        sb.append("Costo adicional: $").append(String.format("%.2f", getAdditionalCost())).append("\n");
        sb.append("====================================");
        return sb.toString();
    }

    /**
     * Gets the duration of the warranty in months.
     * This method is abstract; subclasses must implement it.
     * 
     * @return The duration in months
     */
    public abstract int getDurationInMonths();

    /**
     * Gets the type of the warranty.
     * This method is abstract; subclasses must implement it.
     * 
     * @return The warranty type
     */
    public abstract String getWarrantyType();

    /**
     * Gets the additional cost of the warranty.
     * This method is abstract; subclasses must implement it.
     * 
     * @return The additional cost
     */
    public abstract double getAdditionalCost();
}
