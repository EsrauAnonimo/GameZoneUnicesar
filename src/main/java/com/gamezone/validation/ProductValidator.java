package com.gamezone.validation;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Product;

/**
 * Validator class for Product objects.
 * Contains static methods to ensure product data meets business rules.
 * 
 * @author Dev 1
 */
public class ProductValidator {

    /**
     * Validates the essential fields of a product.
     * 
     * @param product The product to validate
     * @throws InvalidDataException If any validation rule fails
     */
    public static void validate(Product product) {
        if (product == null) {
            throw new InvalidDataException("El producto no puede ser nulo.");
        }
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            throw new InvalidDataException("El ID del producto no puede estar vacío.");
        }
        if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
            throw new InvalidDataException("El título del producto no puede estar vacío.");
        }
        if (product.getPrice() <= 0) {
            throw new InvalidDataException("El precio del producto debe ser mayor a cero.");
        }
        if (product.getAvailableQuantity() < 0) {
            throw new InvalidDataException("La cantidad disponible no puede ser negativa.");
        }
    }
}