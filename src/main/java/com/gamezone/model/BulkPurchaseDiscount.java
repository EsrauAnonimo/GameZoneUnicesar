package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a bulk purchase discount promotion.
 * Applies a percentage to the total sale only if the sale contains
 * at least a minimum number of products.
 * 
 * @author Dev 1
 */
public class BulkPurchaseDiscount extends Promotion {

    private int minimumQuantity;
    private double percentage;

    /**
     * Constructor for a BulkPurchaseDiscount.
     * 
     * @param id Unique code that identifies the promotion
     * @param name Name or description of the promotion
     * @param startDate Date when the promotion starts
     * @param endDate Date when the promotion ends
     * @param minimumQuantity Minimum number of products required
     * @param percentage Discount percentage (0-100)
     */
    public BulkPurchaseDiscount(String id, String name, LocalDate startDate,
                                LocalDate endDate, int minimumQuantity, double percentage) {
        super(id, name, startDate, endDate);
        this.minimumQuantity = minimumQuantity;
        this.percentage = percentage;
    }

    /**
     * Gets the minimum quantity required for the discount.
     * 
     * @return The minimum quantity
     */
    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    /**
     * Sets the minimum quantity required for the discount.
     * 
     * @param minimumQuantity The new minimum quantity
     */
    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
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
     * Calculates the discount amount for the sale.
     * Applies the percentage to the total only if the sale has at least
     * the minimum number of products; returns 0 otherwise.
     * 
     * @param sale The sale to calculate the discount for
     * @return The discount amount
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale == null || sale.getProducts() == null) {
            return 0.0;
        }
        
        List<Product> products = sale.getProducts();
        if (minimumQuantity <= 0 || products.size() < minimumQuantity) {
        return 0.0;
         }
        
        double total = sale.calculateTotal();
        return total * (percentage / 100.0);
    }
}