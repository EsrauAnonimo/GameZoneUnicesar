package com.gamezone.validation;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import java.util.List;

/**
 * Validator class for Sale objects.
 * Ensures that every sale is properly formed, having a valid customer,
 * a seller, and at least one product in the cart.
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
        if (sale.getId() == null || sale.getId().trim().isEmpty()) {
            throw new InvalidDataException("El ID de la venta no puede estar vacío.");
        }
        validateSaleData(sale.getCustomer(), sale.getSeller(), sale.getProducts());
    }

    /**
     * Validates the core participants and products of a sale.
     *
     * @param customer customer associated with the sale
     * @param seller   seller processing the sale
     * @param products list of items being sold
     * @throws InvalidDataException if customer or seller is null, or products is empty
     */
    public static void validateSaleData(Customer customer, Seller seller, List<Product> products) {
        if (customer == null) {
            throw new InvalidDataException("La venta debe tener un cliente asociado.");
        }
        if (seller == null) {
            throw new InvalidDataException("La venta debe tener un vendedor asociado.");
        }
        if (products == null || products.isEmpty()) {
            throw new InvalidDataException("La venta debe contener al menos un producto.");
        }
    }

    /**
     * Validates that a sale exists.
     *
     * @param sale   sale reference
     * @param saleId sale identifier
     * @throws ResourceNotFoundException if sale is null
     */
    public static void validateSaleExists(Sale sale, String saleId) {
        if (sale == null) {
            throw new ResourceNotFoundException("Venta con ID " + saleId + " no encontrada.");
        }
    }
}
