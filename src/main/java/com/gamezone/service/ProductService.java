package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductPersistence;
import com.gamezone.validation.ProductValidator;
import java.util.List;

/**
 * Service class for product operations.
 * Handles business logic for products and communicates with persistence layer.
 */
public class ProductService {

    private ProductPersistence persistence;

    public ProductService() {
        this(new ProductPersistence());
    }

    /**
     * Inyección de dependencias para solucionar DIP y SRP (V-01, V-03, V-09).
     *
     * @param persistence the persistence provider for products
     */
    public ProductService(ProductPersistence persistence) {
        this.persistence = persistence != null ? persistence : new ProductPersistence();
    }

    // Método genérico para solucionar OCP (Abierto/Cerrado)
    public void registerProduct(Product product) {
        ProductValidator.validate(product);

        List<Product> products = persistence.loadAll();
        for (Product p : products) {
            if (p.getId().equals(product.getId())) {
                throw new BusinessRuleException("Ya existe un producto registrado con el ID: " + product.getId());
            }
        }

        products.add(product);
        persistence.save(products);
    }

    // Métodos antiguos que ahora delegan al genérico para no romper la UI
    public void registerVideoGame(VideoGame videoGame) {
        registerProduct(videoGame);
    }

    public void registerConsole(Console console) {
        registerProduct(console);
    }

    public List<Product> listAllProducts() {
        return persistence.loadAll();
    }

    public Product findById(String productId) {
        if (productId == null) {
            return null;
        }
        for (Product product : persistence.loadAll()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    public void updateStock(String productId, int quantity) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new InvalidDataException("El ID del producto no puede estar vacío.");
        }
        if (quantity < 0) {
            throw new InvalidDataException("La cantidad disponible no puede ser negativa.");
        }

        List<Product> products = persistence.loadAll();
        Product target = null;
        
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                product.setAvailableQuantity(quantity);
                target = product;
                break;
            }
        }

        ProductValidator.validateProductExists(target, productId);
        persistence.save(products);
    }

    public void restoreStock(String productId, int quantity) {
        if (quantity <= 0) {
            return;
        }

        List<Product> products = persistence.loadAll();
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                // Delegacion a metodo de dominio encapsulado (V-07, LSP)
                product.increaseStock(quantity);
                break;
            }
        }

        persistence.save(products);
    }
}