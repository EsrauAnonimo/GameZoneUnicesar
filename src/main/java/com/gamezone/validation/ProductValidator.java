package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Product;

/**
 * Validator class for Product objects.
 * Ensures that all products have valid IDs, titles, prices, and quantities
 * before they are processed or stored in the system.
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
        validateProductData(product.getId(), product.getTitle(), product.getPrice(), product.getAvailableQuantity());
    }

    /**
     * Validates raw product data fields.
     *
     * @param id    unique identifier
     * @param title product title
     * @param price unit price
     * @param stock available quantity
     * @throws InvalidDataException if any field violates data rules
     */
    public static void validateProductData(String id, String title, double price, int stock) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidDataException("El ID del producto no puede estar vacío.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidDataException("El título del producto no puede estar vacío.");
        }
        if (price <= 0) {
            throw new InvalidDataException("El precio del producto debe ser mayor a cero.");
        }
        if (stock < 0) {
            throw new InvalidDataException("La cantidad disponible no puede ser negativa.");
        }
    }

    /**
     * Validates that a product has enough available stock for a requested quantity.
     *
     * @param product           product to inspect
     * @param requestedQuantity quantity to sell or check
     * @throws ResourceNotFoundException if product is null
     * @throws InvalidDataException      if requestedQuantity is non-positive
     * @throws BusinessRuleException     if available stock is lower than requested
     */
    public static void validateStockAvailability(Product product, int requestedQuantity) {
        if (product == null) {
            throw new ResourceNotFoundException("Producto no encontrado.");
        }
        if (requestedQuantity <= 0) {
            throw new InvalidDataException("La cantidad solicitada debe ser mayor a cero.");
        }
        if (product.getAvailableQuantity() < requestedQuantity) {
            throw new BusinessRuleException("No hay suficiente stock para el producto " + product.getId()
                    + ". Disponible: " + product.getAvailableQuantity() + ", solicitado: " + requestedQuantity);
        }
    }

    /**
     * Validates that a product exists.
     *
     * @param product   product reference
     * @param productId id being searched
     * @throws ResourceNotFoundException if product is null
     */
    public static void validateProductExists(Product product, String productId) {
        if (product == null) {
            throw new ResourceNotFoundException("Producto con ID " + productId + " no encontrado.");
        }
    }
}