package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductPersistence;
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
        List<Product> products = persistence.loadAll();
        products.add(videoGame);
        persistence.save(products);
    }

    public void registerConsole(Console console) {
        List<Product> products = persistence.loadAll();
        products.add(console);
        persistence.save(products);
    }

    public List<Product> listAllProducts() {
        return persistence.loadAll();
    }

    public void updateStock(String productId, int quantity) {
        List<Product> products = persistence.loadAll();
        
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                product.setAvailableQuantity(quantity);
                break;
            }
        }
        
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