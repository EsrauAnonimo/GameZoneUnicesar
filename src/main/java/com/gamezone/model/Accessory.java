package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that represents a generic accessory in the GameZone system.
 * This class extends Product and adds a list of compatible consoles.
 * It cannot be instantiated directly; subclasses must provide specific implementations.
 */
public abstract class Accessory extends Product {

    private List<Console> compatibleConsoles;

    /**
     * Constructor for an Accessory.
     * Calls the parent constructor with common product attributes.
     * 
     * @param id Unique code that identifies the product
     * @param title Name or title of the accessory
     * @param price Sale price of the accessory
     * @param availableQuantity Number of units available in stock
     */
    public Accessory(String id, String title, double price, int availableQuantity) {
        super(id, title, price, availableQuantity);
        this.compatibleConsoles = new ArrayList<>();
    }

    /**
     * Adds a console to the list of compatible consoles.
     * 
     * @param console The console to add
     */
    public void addCompatibleConsole(Console console) {
        this.compatibleConsoles.add(console);
    }

    /**
     * Removes a console from the list of compatible consoles.
     * 
     * @param console The console to remove
     */
    public void removeCompatibleConsole(Console console) {
        this.compatibleConsoles.remove(console);
    }

    /**
     * Gets the list of compatible consoles.
     * 
     * @return A list of compatible consoles
     */
    public List<Console> getCompatibleConsoles() {
        return compatibleConsoles;
    }

    /**
     * Sets the list of compatible consoles.
     * 
     * @param compatibleConsoles The new list of compatible consoles
     */
    public void setCompatibleConsoles(List<Console> compatibleConsoles) {
        this.compatibleConsoles = compatibleConsoles;
    }

    /**
     * Generates a description of the accessory.
     * This method is abstract; subclasses must implement their own version.
     * 
     * @return A string with the accessory description
     */
    @Override
    public abstract String getDescription();
}