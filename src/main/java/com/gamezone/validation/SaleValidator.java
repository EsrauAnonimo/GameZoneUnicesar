package com.gamezone.validation;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Sale;

/**
 * Validator class for Sale objects.
 * Contains static methods to ensure a sale has all required data.
 * 
 * @author Dev 1
 */
public class SaleValidator {

    /**
     * Validates the essential fields of a sale.
     * 
     * @param sale The sale to validate
     * @throws InvalidDataException If any validation rule fails
     */
    public static void validate(Sale sale) {
        if (sale == null) {
            throw new InvalidDataException("La venta no puede ser nula.");
        }
        if (sale.getCustomer() == null) {
            throw new InvalidDataException("La venta debe tener un cliente asociado.");
        }
        if (sale.getSeller() == null) {
            throw new InvalidDataException("La venta debe tener un vendedor asociado.");
        }
        if (sale.getProducts() == null || sale.getProducts().isEmpty()) {
            throw new InvalidDataException("La venta debe contener al menos un producto.");
        }
    }
}
