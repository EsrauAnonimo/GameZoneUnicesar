package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.persistence.SalePersistence;
import java.util.List;

/**
 * Service class for sale operations.
 * Validates stock for every item in a sale and delegates the stock update
 * to the correct service depending on the item type: Accessory items are
 * updated through AccessoryService, everything else (VideoGame, Console)
 * through ProductService.
 */
public class SaleService {

    private SalePersistence persistence;
    private ProductService productService;
    private AccessoryService accessoryService;

    /**
     * Creates a SaleService.
     *
     * @param persistence      persistence layer used to load/save sales
     * @param productService   service used to update stock for non-accessory products
     * @param accessoryService service used to update stock for accessory items
     */
    public SaleService(SalePersistence persistence, ProductService productService, AccessoryService accessoryService) {
        this.persistence = persistence;
        this.productService = productService;
        this.accessoryService = accessoryService;
    }

    /**
     * Registers a sale: validates that the sale is not empty, validates
     * available stock for every item, updates stock through the
     * appropriate service (Accessory vs regular Product) and persists
     * the sale. Total is already calculated by Sale itself.
     *
     * @param sale the sale to register
     */
    public void registerSale(Sale sale) {
        List<Product> products = sale.getProducts();

        if (products == null || products.isEmpty()) {
            System.out.println("Error: a sale must have at least one product.");
            return;
        }

        for (Product product : products) {
            if (product.getAvailableQuantity() < 1) {
                System.out.println("Error: not enough stock for " + product.getId());
                return;
            }
        }

        for (Product product : products) {
            int newQuantity = product.getAvailableQuantity() - 1;
            if (product instanceof Accessory) {
                accessoryService.updateStock(product.getId(), newQuantity);
            } else {
                productService.updateStock(product.getId(), newQuantity);
            }
        }

        List<Sale> sales = persistence.loadAll();
        sales.add(sale);
        persistence.save(sales);
    }


    public List<Sale> listAllSales() {
        return persistence.loadAll();
    }


    public List<Sale> getSalesByCustomer(String customerId) {
        List<Sale> result = new java.util.ArrayList<>();
        for (Sale sale : persistence.loadAll()) {
            if (sale.getCustomer().getId().equals(customerId)) {
                result.add(sale);
            }
        }
        return result;
    }


    public List<Sale> getSalesBySeller(String sellerId) {
        List<Sale> result = new java.util.ArrayList<>();
        for (Sale sale : persistence.loadAll()) {
            if (sale.getSeller().getId().equals(sellerId)) {
                result.add(sale);
            }
        }
        return result;
    }
}