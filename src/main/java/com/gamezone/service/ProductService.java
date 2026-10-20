package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
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
        this.persistence = new ProductPersistence();
    }

    public void registerVideoGame(VideoGame videoGame) {
        ProductValidator.validate(videoGame);

        List<Product> products = persistence.loadAll();
        for (Product product : products) {
            if (product.getId().equals(videoGame.getId())) {
                throw new BusinessRuleException("Ya existe un producto registrado con el ID: " + videoGame.getId());
            }
        }

        products.add(videoGame);
        persistence.save(products);
    }

    public void registerConsole(Console console) {
        ProductValidator.validate(console);

        List<Product> products = persistence.loadAll();
        for (Product product : products) {
            if (product.getId().equals(console.getId())) {
                throw new BusinessRuleException("Ya existe un producto registrado con el ID: " + console.getId());
            }
        }

        products.add(console);
        persistence.save(products);
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

    /**
     * Increases the stock of a product by the given quantity and persists the
     * change. The return module uses this method to put the units of returned
     * products back into the inventory, so it is the additive counterpart of
     * the stock decrease performed when a sale is registered.
     *
     * <p>Non positive quantities are ignored, and a product id that is not
     * registered leaves the inventory untouched.</p>
     *
     * @param productId id of the product whose stock must be increased
     * @param quantity  amount of units to add back to the available stock
     */
    public void restoreStock(String productId, int quantity) {
        if (quantity <= 0) {
            return;
        }

        List<Product> products = persistence.loadAll();
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                product.setAvailableQuantity(product.getAvailableQuantity() + quantity);
                break;
            }
        }

        persistence.save(products);
    }
}