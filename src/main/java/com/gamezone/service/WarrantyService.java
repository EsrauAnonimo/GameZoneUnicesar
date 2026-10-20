package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.persistence.WarrantyRepository.WarrantyRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for warranties: assignment to sold products and queries by
 * product, sale, validity and expiration. The repository only stores ids, so
 * this service resolves the sale and product references when it needs full
 * warranty objects.
 */
public class WarrantyService {

    private static final String ID_PREFIX = "WAR-";

    private final WarrantyRepository repository;
    private final SalePersistence salePersistence;
    private final ProductService productService;

    /**
     * Creates the service.
     *
     * @param repository      repository that stores warranty ids
     * @param salePersistence persistence used to resolve sales by id (it is used
     *                        instead of SaleService to avoid a constructor cycle)
     * @param productService  service used to resolve products by id
     */
    public WarrantyService(WarrantyRepository repository, SalePersistence salePersistence,
                           ProductService productService) {
        this.repository = repository;
        this.salePersistence = salePersistence;
        this.productService = productService;
    }

    /**
     * Assigns a basic warranty to a product of a sale and persists it.
     *
     * @param product   the covered product
     * @param sale      the sale the product belongs to
     * @param startDate the date the warranty starts
     * @return the created warranty
     * @throws IllegalArgumentException if any argument is null
     */
    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) {
        String id = saveRecord(WarrantyRepository.TYPE_BASIC, product, sale, startDate);
        return new BasicWarranty(id, product, sale, startDate);
    }

    /**
     * Assigns an extended warranty to a product of a sale and persists it.
     *
     * @param product   the covered product
     * @param sale      the sale the product belongs to
     * @param startDate the date the warranty starts
     * @return the created warranty
     * @throws IllegalArgumentException if any argument is null
     */
    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) {
        String id = saveRecord(WarrantyRepository.TYPE_EXTENDED, product, sale, startDate);
        return new ExtendedWarranty(id, product, sale, startDate);
    }

    /**
     * Finds the warranty of a product in a given sale.
     *
     * @param productId id of the covered product
     * @param saleId    id of the sale
     * @return the first matching warranty, or {@code null} if there is none
     */
    public Warranty findWarrantyByProduct(String productId, String saleId) {
        Map<String, Sale> salesById = loadSalesById();
        for (WarrantyRecord record : repository.loadAll()) {
            if (record.getProductId().equals(productId) && record.getSaleId().equals(saleId)) {
                Warranty warranty = resolve(record, salesById);
                if (warranty != null) {
                    return warranty;
                }
            }
        }
        return null;
    }

    /**
     * Lists every registered warranty whose sale and product still exist.
     *
     * @return all warranties
     */
    public List<Warranty> listAllWarranties() {
        Map<String, Sale> salesById = loadSalesById();
        List<Warranty> result = new ArrayList<>();
        for (WarrantyRecord record : repository.loadAll()) {
            Warranty warranty = resolve(record, salesById);
            if (warranty != null) {
                result.add(warranty);
            }
        }
        return result;
    }

    /**
     * Lists the warranties that are active today.
     *
     * @return active warranties
     */
    public List<Warranty> listActiveWarranties() {
        LocalDate today = LocalDate.now();
        List<Warranty> result = new ArrayList<>();
        for (Warranty warranty : listAllWarranties()) {
            if (warranty.isActive(today)) {
                result.add(warranty);
            }
        }
        return result;
    }

    /**
     * Lists the active warranties whose end date falls within the next days.
     *
     * @param daysAhead number of days to look ahead (zero or more)
     * @return active warranties that expire within that period
     * @throws IllegalArgumentException if daysAhead is negative
     */
    public List<Warranty> listWarrantiesExpiringSoon(int daysAhead) {
        if (daysAhead < 0) {
            throw new IllegalArgumentException("Los dias de anticipacion no pueden ser negativos.");
        }
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);
        List<Warranty> result = new ArrayList<>();
        for (Warranty warranty : listAllWarranties()) {
            if (warranty.isActive(today) && !warranty.getEndDate().isAfter(limit)) {
                result.add(warranty);
            }
        }
        return result;
    }

    /**
     * Cancels any warranty associated with the given product and sale, removes
     * the record from persistence, and returns the refunded cost (0.0 for basic
     * warranty, 10% of product price for extended warranty).
     *
     * @param productId id of the product whose warranty is cancelled
     * @param saleId    id of the sale
     * @return the refund amount for the cancelled warranty
     */
    public double cancelWarranties(String productId, String saleId) {
        if (productId == null || saleId == null) {
            return 0.0;
        }

        List<WarrantyRecord> records = repository.loadAll();
        List<WarrantyRecord> remaining = new ArrayList<>();
        double refund = 0.0;
        Map<String, Sale> salesById = loadSalesById();

        for (WarrantyRecord record : records) {
            if (record.getProductId().equals(productId) && record.getSaleId().equals(saleId)) {
                if (WarrantyRepository.TYPE_EXTENDED.equals(record.getType())) {
                    Sale sale = salesById.get(saleId);
                    Product product = sale != null ? findProduct(sale, productId) : null;
                    if (product == null) {
                        for (Product p : productService.listAllProducts()) {
                            if (p.getId().equals(productId)) {
                                product = p;
                                break;
                            }
                        }
                    }
                    if (product != null) {
                        refund += product.getPrice() * 0.10;
                    }
                }
            } else {
                remaining.add(record);
            }
        }

        if (remaining.size() != records.size()) {
            repository.saveAll(remaining);
        }

        return Math.round(refund * 100.0) / 100.0;
    }

    /**
     * Validates the arguments, stores a new warranty record and returns its id.
     */
    private String saveRecord(String type, Product product, Sale sale, LocalDate startDate) {
        validateAssignment(product, sale, startDate);
        List<WarrantyRecord> records = repository.loadAll();
        String id = generateNextId(records);
        records.add(new WarrantyRecord(type, id, product.getId(), sale.getId(), startDate));
        repository.saveAll(records);
        return id;
    }

    private void validateAssignment(Product product, Sale sale, LocalDate startDate) {
        if (product == null || sale == null || startDate == null) {
            throw new IllegalArgumentException(
                    "El producto, la venta y la fecha de inicio son obligatorios.");
        }
    }

    private Map<String, Sale> loadSalesById() {
        Map<String, Sale> salesById = new HashMap<>();
        for (Sale sale : salePersistence.loadAll()) {
            salesById.put(sale.getId(), sale);
        }
        return salesById;
    }

    /**
     * Rebuilds a full warranty from its record, or returns null when the sale
     * or the product no longer exists.
     */
    private Warranty resolve(WarrantyRecord record, Map<String, Sale> salesById) {
        Sale sale = salesById.get(record.getSaleId());
        if (sale == null) {
            return null;
        }
        Product product = findProduct(sale, record.getProductId());
        if (product == null) {
            return null;
        }
        if (WarrantyRepository.TYPE_BASIC.equals(record.getType())) {
            return new BasicWarranty(record.getId(), product, sale, record.getStartDate());
        }
        if (WarrantyRepository.TYPE_EXTENDED.equals(record.getType())) {
            return new ExtendedWarranty(record.getId(), product, sale, record.getStartDate());
        }
        return null;
    }

    /**
     * Looks for the product first among the items of the sale (which also
     * covers accessories) and then in the product inventory.
     */
    private Product findProduct(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        for (Product product : productService.listAllProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    private String generateNextId(List<WarrantyRecord> records) {
        int max = 0;
        for (WarrantyRecord record : records) {
            String id = record.getId();
            if (id != null && id.startsWith(ID_PREFIX)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                    // ids with another format are ignored
                }
            }
        }
        return String.format("%s%03d", ID_PREFIX, max + 1);
    }
}
