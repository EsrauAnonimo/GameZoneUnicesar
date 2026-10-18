package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a category-based discount promotion.
 * Applies a percentage only to products of a specific category
 * (VIDEOGAME, CONSOLE, or ACCESSORY).
 * 
 * @author Dev 1
 */
public class CategoryDiscount extends Promotion {

    public static final String CATEGORY_VIDEOGAME = "VIDEOGAME";
    public static final String CATEGORY_CONSOLE = "CONSOLE";
    public static final String CATEGORY_ACCESSORY = "ACCESSORY";


    private double percentage;
    private String targetCategory;

    /**
     * Constructor for a CategoryDiscount.
     * 
     * @param id Unique code that identifies the promotion
     * @param name Name or description of the promotion
     * @param startDate Date when the promotion starts
     * @param endDate Date when the promotion ends
     * @param percentage Discount percentage (0-100)
     * @param targetCategory Category to apply the discount to (VIDEOGAME, CONSOLE, or ACCESSORY)
     */
    public CategoryDiscount(String id, String name, LocalDate startDate,
                            LocalDate endDate, double percentage, String targetCategory) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
        this.targetCategory = targetCategory;
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
     * Gets the target category.
     * 
     * @return The target category
     */
    public String getTargetCategory() {
        return targetCategory;
    }

    /**
     * Sets the target category.
     * 
     * @param targetCategory The new target category
     */
    public void setTargetCategory(String targetCategory) {
        this.targetCategory = targetCategory;
    }

    /**
     * Calculates the discount amount for products of the target category.
     * Sums the prices of matching products and applies the percentage.
     * 
     * @param sale The sale to calculate the discount for
     * @return The discount amount
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale == null || sale.getProducts() == null) {
            return 0.0;
        }
        
        double categoryTotal = 0.0;
        List<Product> products = sale.getProducts();
        
        if (targetCategory == null) {
    return 0.0;
}

for (Product product : products) {
    if (CATEGORY_VIDEOGAME.equals(targetCategory) && product instanceof VideoGame) {
        categoryTotal += product.getPrice();
    } else if (CATEGORY_CONSOLE.equals(targetCategory) && product instanceof Console) {
        categoryTotal += product.getPrice();
    } else if (CATEGORY_ACCESSORY.equals(targetCategory) && product instanceof Accessory) {
                categoryTotal += product.getPrice();
            }

}
        
        return categoryTotal * (percentage / 100.0);
    }
}