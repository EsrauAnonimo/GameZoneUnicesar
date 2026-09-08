package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.persistence.SalePersistence;
import java.util.List;


public class SaleService {

    private SalePersistence persistence;
    private ProductService productService;

    public SaleService(SalePersistence persistence, ProductService productService) {
        this.persistence = persistence;
        this.productService = productService;
    }


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
            product.setAvailableQuantity(newQuantity);
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