package com.gamezone.model;

/**
 * Represents a cable accessory in the GameZone system.
 * This class extends Accessory and adds length and connector type attributes.
 */
public class Cable extends Accessory {

    private double lengthMeters;
    private String connectorType;

    /**
     * Constructor for a Cable.
     * Calls the parent constructor with common product attributes.
     * 
     * @param id Unique code that identifies the product
     * @param title Name or title of the cable
     * @param price Sale price of the cable
     * @param availableQuantity Number of units available in stock
     * @param lengthMeters Length of the cable in meters
     * @param connectorType Type of connector (e.g., HDMI, USB-C, DisplayPort)
     */
    public Cable(String id, String title, double price, int availableQuantity,
                 double lengthMeters, String connectorType) {
        super(id, title, price, availableQuantity);
        this.lengthMeters = lengthMeters;
        this.connectorType = connectorType;
    }

    /**
     * Gets the cable length in meters.
     * 
     * @return The length in meters
     */
    public double getLengthMeters() {
        return lengthMeters;
    }

    /**
     * Sets the cable length in meters.
     * 
     * @param lengthMeters The new length in meters
     */
    public void setLengthMeters(double lengthMeters) {
        this.lengthMeters = lengthMeters;
    }

    /**
     * Gets the connector type.
     * 
     * @return The connector type
     */
    public String getConnectorType() {
        return connectorType;
    }

    /**
     * Sets the connector type.
     * 
     * @param connectorType The new connector type
     */
    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    /**
     * Generates a description of the cable.
     * Includes the specific length and connector type attributes.
     * 
     * @return A string with the cable description
     */
    @Override
    public String getDescription() {
        return "Cable{" +
               "id='" + getId() + '\'' +
               ", title='" + getTitle() + '\'' +
               ", price=" + getPrice() +
               ", availableQuantity=" + getAvailableQuantity() +
               ", lengthMeters=" + lengthMeters +
               ", connectorType='" + connectorType + '\'' +
               '}';
    }
}