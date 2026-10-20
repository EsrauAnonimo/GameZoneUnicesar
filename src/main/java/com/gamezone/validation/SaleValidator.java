package com.gamezone.validation;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Sale;

/**
 * Validator class for Sale objects.
 * Ensures that every sale is properly formed, having a valid customer,
 * a seller, and at least one product in the cart.
 * 
 * @author Dev 1
 * @version 1.0
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
