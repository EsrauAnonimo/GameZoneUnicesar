package com.gamezone.model;

import java.time.LocalDate;

/**
 * Represents a basic warranty in the GameZone system.
 * Basic warranties have a duration of 6 months and no additional cost.
 * 
 * @author Dev 1
 */
public class BasicWarranty extends Warranty {

    /**
     * Constructor for a BasicWarranty.
     * Calls the parent constructor to initialize common attributes.
     * 
     * @param id Unique code that identifies the warranty
     * @param product The product covered by the warranty
     * @param sale The sale associated with the warranty
     * @param startDate Date when the warranty starts
     */
    public BasicWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * Gets the duration of the basic warranty in months.
     * 
     * @return 6 months
     */
    @Override
    public int getDurationInMonths() {
    final int BASIC_DURATION_MONTHS = 6;
    return BASIC_DURATION_MONTHS;
}
    /**
     * Gets the type of the basic warranty.
     * 
     * @return "Garantía Básica"
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Básica";
    }

    /**
     * Gets the additional cost of the basic warranty.
     * Basic warranties have no additional cost.
     * 
     * @return 0.0
     */
    @Override
    public double getAdditionalCost() {
    final double NO_ADDITIONAL_COST = 0.0;
    return NO_ADDITIONAL_COST;
}
}