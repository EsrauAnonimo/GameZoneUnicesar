package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents an extended warranty in the GameZone system.
 * Extended warranties have a duration of 12 months and an additional
 * cost equal to 10% of the product price.
 * 
 * @author Dev 1
 */
public class ExtendedWarranty extends Warranty {

    /**
     * Constructor for an ExtendedWarranty.
     * Calls the parent constructor to initialize common attributes.
     * 
     * @param id Unique code that identifies the warranty
     * @param product The product covered by the warranty
     * @param sale The sale associated with the warranty
     * @param startDate Date when the warranty starts
     */
    public ExtendedWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * Gets the duration of the extended warranty in months.
     * 
     * @return 12 months
     */
    @Override
    public int getDurationInMonths() {
    final int EXTENDED_DURATION_MONTHS = 12;
    return EXTENDED_DURATION_MONTHS;
}

    /**
     * Gets the type of the extended warranty.
     * 
     * @return "Garantía Extendida"
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Extendida";
    }

    /**
     * Gets the additional cost of the extended warranty.
     * The cost is 10% of the product price.
     * 
     * @return 10% of the product price
     */
    @Override
    public double getAdditionalCost() {
    final double EXTENDED_WARRANTY_RATE = 0.10;
    
    if (getProduct() == null) {
        return 0.0;
    }
    return getProduct().getPrice() * EXTENDED_WARRANTY_RATE;
}
}
