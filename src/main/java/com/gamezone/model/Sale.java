package com.gamezone.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a sale transaction of the store.
 * It groups the customer, the seller and the sold products, and keeps the
 * monetary result of the transaction: subtotal, applied discount, extended
 * warranty cost and final total. It implements Serializable because
 * SalePersistence stores sales using Java serialization.
 */
public class Sale implements Serializable {

    private static final long serialVersionUID = 1L;


    private String id;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;
    private double total;
    private String appliedPromotionName;
    private double discountAmount;
    private double extendedWarrantyCost;

    public Sale(String id, LocalDate date, Customer customer, Seller seller, List<Product> products) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.seller = seller;
        this.products = products;
        this.total = calculateTotal();
        this.extendedWarrantyCost = 0.0;
    }
    
    public double calculateTotal() {
        double sum = 0;
        if (products != null) {
            for (Product product : products) {
                if (product != null) {
                    sum += product.getPrice();
                }
            }
        }
        return sum;
    }

    /**
     * Returns the subtotal of the sale: the sum of the prices of the sold
     * products, before any discount is applied. It is a named alias of
     * calculateTotal() so that callers do not confuse the subtotal with the
     * final total of the sale.
     *
     * @return the subtotal of the sale
     */
    public double getSubtotal() {
        return calculateTotal();
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Seller getSeller() {
        return seller;
    }

    public List<Product> getProducts() {
        return products;
    }

    public double getTotal() {
        return total;
    }

    /**
     * Sets the final total of the sale.
     *
     * @param total the final total of the sale
     */
    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * Gets the name of the promotion that was applied to this sale.
     *
     * @return the applied promotion name, or null if no promotion was applied
     */
    public String getAppliedPromotionName() {
        return appliedPromotionName;
    }

    /**
     * Sets the name of the promotion applied to this sale.
     *
     * @param appliedPromotionName the applied promotion name
     */
    public void setAppliedPromotionName(String appliedPromotionName) {
        this.appliedPromotionName = appliedPromotionName;
    }

    /**
     * Gets the amount subtracted from the subtotal by the applied promotion.
     *
     * @return the discount amount (zero if no promotion was applied)
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the amount subtracted from the subtotal by the applied promotion.
     *
     * @param discountAmount the discount amount
     */
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Gets the additional cost of extended warranties for this sale.
     *
     * @return the extended warranty cost
     */
    public double getExtendedWarrantyCost() {
        return extendedWarrantyCost;
    }

    /**
     * Sets the additional cost of extended warranties for this sale.
     *
     * @param extendedWarrantyCost the extended warranty cost
     */
    public void setExtendedWarrantyCost(double extendedWarrantyCost) {
        this.extendedWarrantyCost = extendedWarrantyCost;
    }



    /**
     * Checks if the sale can be returned relative to today.
     * Kept as a convenience delegating to {@link #canBeReturnedOn(LocalDate)} (V-11).
     * 
     * @return true if the sale can be returned, false otherwise
     */
    public boolean canBeReturned() {
        return canBeReturnedOn(LocalDate.now());
    }

    /**
     * Checks if the sale is within the 30-day window relative to a specified date.
     *
     * @param evaluationDate the date against which the sale is checked
     * @return true if the evaluation date is within 30 days of the sale date
     */
    public boolean canBeReturnedOn(LocalDate evaluationDate) {
        if (date == null || evaluationDate == null) {
            return false;
        }
        LocalDate limitDate = date.plusDays(30);
        return !evaluationDate.isAfter(limitDate);
    }
}
