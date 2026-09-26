package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract class that represents a promotion in the GameZone system.
 * Promotions have a validity period and a discount calculation strategy
 * that must be implemented by subclasses.
 * 
 * @author Dev 1
 */
public abstract class Promotion {

    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Constructor for a Promotion.
     * 
     * @param id Unique code that identifies the promotion
     * @param name Name or description of the promotion
     * @param startDate Date when the promotion starts
     * @param endDate Date when the promotion ends
     */
    public Promotion(String id, String name, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Gets the promotion ID.
     * 
     * @return The promotion ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the promotion ID.
     * 
     * @param id The new promotion ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the promotion name.
     * 
     * @return The promotion name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the promotion name.
     * 
     * @param name The new promotion name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the start date.
     * 
     * @return The start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     * 
     * @param startDate The new start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date.
     * 
     * @return The end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date.
     * 
     * @param endDate The new end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Checks if the promotion is active on a given date.
     * A promotion is active if the date is between startDate and endDate (inclusive).
     * 
     * @param date The date to check
     * @return true if the promotion is active, false otherwise
     */
    public boolean isActive(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculates the discount amount for a given sale.
     * This method is abstract; subclasses must implement their own strategy.
     * 
     * @param sale The sale to calculate the discount for
     * @return The discount amount
     */
    public abstract double calculateDiscount(Sale sale);
}