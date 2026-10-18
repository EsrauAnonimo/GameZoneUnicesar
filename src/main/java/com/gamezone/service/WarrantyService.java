package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.WarrantyRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for warranties: assignment to sold products and queries by
 * product, sale, validity and expiration.
 */
public class WarrantyService {

    private static final String ID_PREFIX = "WAR-";

    private final WarrantyRepository repository;

    /**
     * Creates the service.
     *
     * @param repository repository used to persist warranties
     */
    public WarrantyService(WarrantyRepository repository) {
        this.repository = repository;
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
        validateAssignment(product, sale, startDate);
        List<Warranty> warranties = repository.loadAll();
        BasicWarranty warranty =
                new BasicWarranty(generateNextId(warranties), product, sale, startDate);
        warranties.add(warranty);
        repository.saveAll(warranties);
        return warranty;
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
        validateAssignment(product, sale, startDate);
        List<Warranty> warranties = repository.loadAll();
        ExtendedWarranty warranty =
                new ExtendedWarranty(generateNextId(warranties), product, sale, startDate);
        warranties.add(warranty);
        repository.saveAll(warranties);
        return warranty;
    }

    /**
     * Finds the warranty of a product in a given sale.
     *
     * @param productId id of the covered product
     * @param saleId    id of the sale
     * @return the first matching warranty, or {@code null} if there is none
     */
    public Warranty findWarrantyByProduct(String productId, String saleId) {
        for (Warranty warranty : repository.loadAll()) {
            if (warranty.getProduct().getId().equals(productId)
                    && warranty.getSale().getId().equals(saleId)) {
                return warranty;
            }
        }
        return null;
    }

    /**
     * Lists every registered warranty.
     *
     * @return all warranties
     */
    public List<Warranty> listAllWarranties() {
        return repository.loadAll();
    }

    /**
     * Lists the warranties that are active today.
     *
     * @return active warranties
     */
    public List<Warranty> listActiveWarranties() {
        LocalDate today = LocalDate.now();
        List<Warranty> result = new ArrayList<>();
        for (Warranty warranty : repository.loadAll()) {
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
        for (Warranty warranty : repository.loadAll()) {
            if (warranty.isActive(today) && !warranty.getEndDate().isAfter(limit)) {
                result.add(warranty);
            }
        }
        return result;
    }

    private void validateAssignment(Product product, Sale sale, LocalDate startDate) {
        if (product == null || sale == null || startDate == null) {
            throw new IllegalArgumentException(
                    "El producto, la venta y la fecha de inicio son obligatorios.");
        }
    }

    private String generateNextId(List<Warranty> warranties) {
        int max = 0;
        for (Warranty warranty : warranties) {
            String id = warranty.getId();
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
