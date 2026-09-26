package com.gamezone.model;

/**
 * Abstract class that represents a generic accessory in the GameZone system.
 * This class extends Product and adds a list of compatible consoles.
 * It cannot be instantiated directly; subclasses must provide specific implementations.
 * 
 * @author Dev 1
 */
public class Memory extends Accessory {

    private int capacityGb;
    private String memoryType;

    /**
     * Constructor for a Memory.
     * Calls the parent constructor with common product attributes.
     * 
     * @param id Unique code that identifies the product
     * @param title Name or title of the memory
     * @param price Sale price of the memory
     * @param availableQuantity Number of units available in stock
     * @param capacityGb Storage capacity in gigabytes
     * @param memoryType Type of memory (e.g., SSD, HDD, NVMe)
     */
    public Memory(String id, String title, double price, int availableQuantity,
                  int capacityGb, String memoryType) {
        super(id, title, price, availableQuantity);
        this.capacityGb = capacityGb;
        this.memoryType = memoryType;
    }

    /**
     * Gets the storage capacity in gigabytes.
     * 
     * @return The capacity in GB
     */
    public int getCapacityGb() {
        return capacityGb;
    }

    /**
     * Sets the storage capacity in gigabytes.
     * 
     * @param capacityGb The new capacity in GB
     */
    public void setCapacityGb(int capacityGb) {
        this.capacityGb = capacityGb;
    }

    /**
     * Gets the memory type.
     * 
     * @return The memory type
     */
    public String getMemoryType() {
        return memoryType;
    }

    /**
     * Sets the memory type.
     * 
     * @param memoryType The new memory type
     */
    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    /**
     * Generates a description of the memory.
     * Includes the specific capacity and memory type attributes.
     * 
     * @return A string with the memory description
     */
   @Override
public String getDescription() {
    return String.format(
        "Memory | ID: %s | Title: %s | Price: %.2f | Available: %d | Capacity: %dGB | Type: %s",
        getId(), getTitle(), getPrice(), getAvailableQuantity(), capacityGb, memoryType
    );
}
}