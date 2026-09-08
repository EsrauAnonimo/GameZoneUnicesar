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
}