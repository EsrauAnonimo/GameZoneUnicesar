package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.persistence.SalePersistence;
import java.util.List;

/**
 * Service class for sale operations.
 * Validates stock for every item in a sale, resolves the best promotion for
 * the sale through PromotionService and delegates the stock update to the
 * correct service depending on the item type: Accessory items are updated
 * through AccessoryService, everything else (VideoGame, Console) through
 * ProductService.
 */
public class SaleService {

    private SalePersistence persistence;
    private ProductService productService;
    private AccessoryService accessoryService;
    private PromotionService promotionService;

    /**
     * Creates a SaleService.
     *
     * @param persistence      persistence layer used to load/save sales
     * @param productService   service used to update stock for non-accessory products
     * @param accessoryService service used to update stock for accessory items
     * @param promotionService service used to resolve the best promotion for a sale
     */
    public SaleService(SalePersistence persistence, ProductService productService,
                       AccessoryService accessoryService, PromotionService promotionService) {
        this.persistence = persistence;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.promotionService = promotionService;
    }

    /**
     * Registers a sale: validates that the sale is not empty, validates
     * available stock for every item, applies the best promotion available
     * for the sale, updates stock through the appropriate service (Accessory
     * vs regular Product) and persists the sale.
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

        applyBestPromotion(sale);

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

    /**
     * Resolves the best promotion for the sale and, if there is one, stores
     * its name and its discount amount in the sale and adjusts the final
     * total. The subtotal is never modified, so a sale can always be
     * recomputed from its products.
     * <p>
     * The promotion module is an auxiliary module: a malformed
     * promotions.csv must never make a sale disappear, so any failure while
     * reading or evaluating the promotions leaves the sale without discount
     * instead of interrupting its registration.
     *
     * @param sale the sale whose total must be adjusted
     */
    private void applyBestPromotion(Sale sale) {
        double subtotal = sale.getSubtotal();
        try {
            Promotion promotion = promotionService.findBestPromotionFor(sale);
            if (promotion == null) {
                return;
            }

            double discount = promotion.calculateDiscount(sale);
            if (discount > subtotal) {
                // Un descuento nunca puede superar el subtotal de la venta.
                discount = subtotal;
            }
            if (discount <= 0) {
                return;
            }

            double appliedDiscount = roundToCents(discount);
            sale.setAppliedPromotionName(promotion.getName());
            sale.setDiscountAmount(appliedDiscount);
            sale.setTotal(roundToCents(subtotal - appliedDiscount));
        } catch (RuntimeException e) {
            sale.setAppliedPromotionName(null);
            sale.setDiscountAmount(0);
            sale.setTotal(roundToCents(subtotal));
            System.out.println("Advertencia: no se pudo aplicar la promocion a la venta "
                    + sale.getId() + ". Se registra sin descuento. Detalle: " + e.getMessage());
        }
    }

    /**
     * Rounds a monetary amount to two decimal places, avoiding floating point
     * artifacts such as 9499.999999999998.
     *
     * @param value the amount to round
     * @return the amount rounded to cents
     */
    private double roundToCents(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
