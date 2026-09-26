package com.gamezone.model;

/**
 * Abstract class that represents a generic accessory in the GameZone system.
 * This class extends Product and adds a list of compatible consoles.
 * It cannot be instantiated directly; subclasses must provide specific implementations.
 * 
 * @author Dev 1
 */
public class Controller extends Accessory {

    private String connectionType;

    /**
     * Constructor for a Controller.
     * Calls the parent constructor with common product attributes.
     * 
     * @param id Unique code that identifies the product
     * @param title Name or title of the controller
     * @param price Sale price of the controller
     * @param availableQuantity Number of units available in stock
     * @param connectionType Type of connection (e.g., USB, Wireless, Bluetooth)
     */
    public Controller(String id, String title, double price, int availableQuantity,
                      String connectionType) {
        super(id, title, price, availableQuantity);
        this.connectionType = connectionType;
    }

    /**
     * Gets the connection type.
     * 
     * @return The connection type
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * Sets the connection type.
     * 
     * @param connectionType The new connection type
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Generates a description of the controller.
     * Includes the specific connection type attribute.
     * 
     * @return A string with the controller description
     */
    @Override
    public String getDescription() {
        return "Controller{" +
               "id='" + getId() + '\'' +
               ", title='" + getTitle() + '\'' +
               ", price=" + getPrice() +
               ", availableQuantity=" + getAvailableQuantity() +
               ", connectionType='" + connectionType + '\'' +
               '}';
    }
}