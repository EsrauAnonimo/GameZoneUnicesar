package com.gamezone.model;

/**
 * Represents a console product in the GameZone system.
 * This class extends Product and adds specific attributes for consoles.
 */
public class Console extends Product {

    private String brand;
    private String model;
    private String generation;

    public Console(String id, String title, double price, int availableQuantity,
                   String brand, String model, String generation) {
        super(id, title, price, availableQuantity);
        this.brand = brand;
        this.model = model;
        this.generation = generation;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getGeneration() {
        return generation;
    }

    @Override
    public String getDescription() {
        return "Console{" +
               "price=" + getPrice() +
               ", availableQuantity=" + getAvailableQuantity() +
               ", brand='" + brand + '\'' +
               ", model='" + model + '\'' +
               ", generation='" + generation + '\'' +
               '}';
    }
}