package com.gamezone.model;

/**
 * Abstract base class that represents a generic product in the GameZone system.
 * This class contains common attributes and behaviors shared by all product types.
 * It cannot be instantiated directly; subclasses must provide specific implementations.
 */
public abstract class Product {

    // Private attributes according to the class diagram
    private String id;
    private String title;
    private double price;
    private int availableQuantity;

    /**
     * Constructor to initialize the common attributes of a product.
     * 
     * @param id Unique code that identifies the product
     * @param title Name or title of the product
     * @param price Sale price of the product
     * @param availableQuantity Number of units available in stock
     */
    public Product(String id, String title, double price, int availableQuantity) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.availableQuantity = availableQuantity;
    }

    /**
     * Abstract method that must be implemented by subclasses.
     * Each subclass must provide its own specific description.
     * 
     * @return A String with the complete description of the product
     */
    public abstract String getDescription();

    /**
     * Gets the product price.
     * 
     * @return The price of the product
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the available quantity in stock.
     * 
     * @return The available quantity
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Updates the available quantity in stock.
     * 
     * @param quantity The new quantity to set
     */
    public void setAvailableQuantity(int quantity) {
        this.availableQuantity = quantity;
    }
}