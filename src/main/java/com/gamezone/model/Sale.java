package com.gamezone.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


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


    public Sale(String id, LocalDate date, Customer customer, Seller seller, List<Product> products) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.seller = seller;
        this.products = products;
        this.total = calculateTotal();
    }
    
    
    public double calculateTotal() {
        double sum = 0;
        for (Product product : products) {
            sum += product.getPrice();
        }
        return sum;
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
}