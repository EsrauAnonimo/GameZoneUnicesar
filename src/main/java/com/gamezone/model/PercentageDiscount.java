package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a percentage-based discount promotion.
 * Applies a fixed percentage to the total amount of a sale.
 * 
 * @author Dev 1
 */
public class PercentageDiscount extends Promotion {

    private double percentage;

    /**
     * Constructor for a PercentageDiscount.
     * 
     * @param id Unique code that identifies the promotion
     * @param name Name or description of the promotion
     * @param startDate Date when the promotion starts
     * @param endDate Date when the promotion ends
     * @param percentage Discount percentage (0-100)
     */
    public PercentageDiscount(String id, String name, LocalDate startDate,
                              LocalDate endDate, double percentage) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
    }

    /**
     * Gets the discount percentage.
     * 
     * @return The discount percentage
     */
    public double getPercentage() {
        return percentage;
    }

    /**
     * Sets the discount percentage.
     * 
     * @param percentage The new discount percentage
     */
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    /**
     * Calculates the discount amount based on the sale total.
     * Applies the percentage to the total of the sale.
     * 
     * @param sale The sale to calculate the discount for
     * @return The discount amount
     */
    @Override
public double calculateDiscount(Sale sale) {
    if (sale == null || sale.getProducts() == null) {
        return 0.0;
    }
    double total = sale.calculateTotal();
    return total * (percentage / 100.0);
}
}