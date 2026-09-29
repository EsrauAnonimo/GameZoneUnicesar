package com.gamezone.service;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains the business rules of the return module: validating and
 * registering product returns, and querying return history and monthly
 * balances. This is the only class in the module allowed to call
 * ReturnRepository; the UI layer must always go through this service.
 */
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final SaleService saleService;
    private final ProductService productService;
    private List<Return> returns;

    /**
     * Creates the service and immediately loads the previously stored
     * returns.
     *
     * @param returnRepository repository used to read and write return data
     * @param saleService      service used to validate and look up sales
     * @param productService   service used to restore stock of returned
     *                         products
     */
    public ReturnService(ReturnRepository returnRepository, SaleService saleService,
                          ProductService productService) {
        this.returnRepository = returnRepository;
        this.saleService = saleService;
        this.productService = productService;
        this.returns = returnRepository.loadAll();
    }

    /**
     * Registers a new return for the given sale: validates that the sale
     * exists, that it is still within the 30-day return window, and that
     * every product id belongs to that sale. On success it builds the
     * return with today's date, calculates the refund amount, restores the
     * stock of every returned product, persists the updated list and
     * returns the new Return.
     *
     * @param saleId     id of the original sale
     * @param productIds ids of the products being returned from that sale
     * @param reason     reason given for the return
     * @return the newly registered return
     * @throws IllegalArgumentException with a message in Spanish if the
     *                                  sale does not exist, is outside the
     *                                  30-day window, or a product id does
     *                                  not belong to the sale
     */
    public Return registerReturn(String saleId, List<String> productIds, String reason) {
        Sale sale = findSaleById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("No existe una venta con el ID indicado.");
        }
        if (!sale.canBeReturned()) {
            throw new IllegalArgumentException("La venta ya supero el plazo de 30 dias para devoluciones.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un producto para devolver.");
        }

        List<Product> productsToReturn = new ArrayList<>();
        for (String productId : productIds) {
            Product product = findProductInSale(sale, productId);
            if (product == null) {
                throw new IllegalArgumentException(
                        "El producto " + productId + " no pertenece a la venta indicada.");
            }
            productsToReturn.add(product);
        }
        rejectAlreadyReturned(sale, productsToReturn);

        Return newReturn = new Return(UUID.randomUUID().toString(), LocalDate.now(), sale, reason);
        for (Product product : productsToReturn) {
            newReturn.addReturnedProduct(product);
        }
        newReturn.calculateRefundAmount();

        for (Product product : productsToReturn) {
            productService.restoreStock(product.getId(), 1);
        }

        returns.add(newReturn);
        returnRepository.saveAll(returns);
        return newReturn;
    }

    /**
     * Returns the full history of returns.
     *
     * @return list of all returns
     */
    public List<Return> viewAllReturns() {
        return returns;
    }

    /**
     * Returns the returns whose original sale belongs to the given customer.
     *
     * @param customerId id of the customer
     * @return list of returns made by that customer
     */
    public List<Return> viewReturnsByCustomer(String customerId) {
        List<Return> result = new ArrayList<>();
        for (Return theReturn : returns) {
            Sale sale = theReturn.getOriginalSale();
            if (sale != null && sale.getCustomer() != null
                    && sale.getCustomer().getId().equals(customerId)) {
                result.add(theReturn);
            }
        }
        return result;
    }

    /**
     * Returns the returns associated with a specific sale.
     *
     * @param saleId id of the sale
     * @return list of returns made against that sale
     */
    public List<Return> viewReturnsBySale(String saleId) {
        List<Return> result = new ArrayList<>();
        for (Return theReturn : returns) {
            if (theReturn.getOriginalSale() != null
                    && theReturn.getOriginalSale().getId().equals(saleId)) {
                result.add(theReturn);
            }
        }
        return result;
    }

    /**
     * Calculates the monthly balance for the given month and year: total
     * sales minus total returns registered in that period.
     *
     * @param month month to evaluate (1-12)
     * @param year  year to evaluate
     * @return the difference between total sales and total returns for that
     *         month
     */
    public double generateMonthlyBalance(int month, int year) {
        double monthlySales = calculateMonthlySales(month, year);
        double monthlyReturns = calculateMonthlyReturns(month, year);
        return monthlySales - monthlyReturns;
    }

    /**
     * Calculates the total amount of sales registered in the given month
     * and year, using each sale's final total.
     *
     * @param month month to evaluate (1-12)
     * @param year  year to evaluate
     * @return the sum of the final totals of all sales in that period
     */
    public double calculateMonthlySales(int month, int year) {
        double total = 0;
        for (Sale sale : saleService.listAllSales()) {
            if (sale.getDate() != null
                    && sale.getDate().getMonthValue() == month
                    && sale.getDate().getYear() == year) {
                total += sale.getTotal();
            }
        }
        return total;
    }

    /**
     * Calculates the total amount refunded through returns registered in
     * the given month and year.
     *
     * @param month month to evaluate (1-12)
     * @param year  year to evaluate
     * @return the sum of the refund amounts of all returns in that period
     */
    public double calculateMonthlyReturns(int month, int year) {
        double total = 0;
        for (Return theReturn : returns) {
            if (theReturn.getDate() != null
                    && theReturn.getDate().getMonthValue() == month
                    && theReturn.getDate().getYear() == year) {
                total += theReturn.getRefundAmount();
            }
        }
        return total;
    }

    private Sale findSaleById(String saleId) {
        for (Sale sale : saleService.listAllSales()) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }

    private Product findProductInSale(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Rejects a return that would give back more units of a product than the
     * ones the sale actually contained.
     *
     * <p>Without this check the same unit could be returned twice: the stock
     * would grow past its original value and the customer would be refunded
     * twice for the same product. Units are counted instead of compared as a
     * set, so a sale that contains the same product more than once keeps
     * working.</p>
     *
     * @param sale             the original sale of the return
     * @param productsToReturn the products of the return being registered
     * @throws IllegalArgumentException if a product would be over returned
     */
    private void rejectAlreadyReturned(Sale sale, List<Product> productsToReturn) {
        for (Product product : productsToReturn) {
            int sold = countInSale(sale, product.getId());
            int alreadyReturned = countAlreadyReturned(sale.getId(), product.getId());
            int requested = countInList(productsToReturn, product.getId());

            if (alreadyReturned + requested > sold) {
                throw new IllegalArgumentException("El producto " + product.getId()
                        + " ya fue devuelto y no quedan unidades por devolver.");
            }
        }
    }

    private int countInSale(Sale sale, String productId) {
        int count = 0;
        for (Product product : sale.getProducts()) {
            if (product != null && product.getId().equals(productId)) {
                count++;
            }
        }
        return count;
    }

    private int countAlreadyReturned(String saleId, String productId) {
        int count = 0;
        for (Return theReturn : returns) {
            if (theReturn.getOriginalSale() == null
                    || !theReturn.getOriginalSale().getId().equals(saleId)) {
                continue;
            }
            count += countInList(theReturn.getReturnedProducts(), productId);
        }
        return count;
    }

    private int countInList(List<Product> products, String productId) {
        int count = 0;
        for (Product product : products) {
            if (product != null && product.getId().equals(productId)) {
                count++;
            }
        }
        return count;
    }
}