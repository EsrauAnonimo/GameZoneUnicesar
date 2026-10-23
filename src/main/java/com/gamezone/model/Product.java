package com.gamezone.model;

import java.io.Serializable;

/**
 * Abstract base class that represents a generic product in the GameZone system.
 * This class contains common attributes and behaviors shared by all product types.
 * It cannot be instantiated directly; subclasses must provide specific implementations.
 */
public abstract class Product implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * Gets the product ID.
     * Needed for stock update functionality.
     * 
     * @return The product ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the product title.
     * Used by every description/listing method of the subclasses.
     * 
     * @return The product title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the product title.
     * 
     * @param title The new title to set
     */
    public void setTitle(String title) {
        this.title = title;
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

    /**
     * Indicates whether this product is eligible for warranty coverage.
     * Subclasses like Console override this to return true.
     *
     * @return true if warranty eligible, false otherwise
     */
    public boolean isWarrantyEligible() {
        return false;
    }

    /**
     * Calculates the additional cost of an extended warranty for this product.
     * Default returns 0.0 for non-eligible products.
     *
     * @return the extended warranty cost
     */
    public double getWarrantyCost() {
        return 0.0;
    }

    /**
     * Indicates whether this product is an accessory.
     *
     * @return true if accessory, false otherwise
     */
    public boolean isAccessory() {
        return false;
    }

    /**
     * Updates the stock of this product through the appropriate service.
     * By default, delegates to ProductService for standard products.
     *
     * @param productService service for standard products
     * @param accessoryService service for accessories
     * @param newQuantity the updated stock quantity
     */
    public void updateStock(com.gamezone.service.ProductService productService,
                            com.gamezone.service.AccessoryService accessoryService,
                            int newQuantity) {
        if (productService != null) {
            productService.updateStock(getId(), newQuantity);
        }
    }
        /**
     * Increases the available stock of the product.
     * 
     * @param quantity The amount to add to the stock.
     */
    public void increaseStock(int quantity) {
        if (quantity > 0) {
            this.availableQuantity += quantity;
        }
    }

    /**
     * Gets the category of the product for promotion purposes.
     * 
     * @return The category string.
     */
    public String getCategory() {
        return "GENERAL";
    }
}