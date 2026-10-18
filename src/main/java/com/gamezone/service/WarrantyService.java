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

public class WarrantyService {

    private static final String ID_PREFIX = "WAR-";

    private final WarrantyRepository repository;

    public WarrantyService(WarrantyRepository repository) {
        this.repository = repository;
    }

    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) {
        validateAssignment(product, sale, startDate);
        List<Warranty> warranties = repository.loadAll();
        BasicWarranty warranty =
                new BasicWarranty(generateNextId(warranties), product, sale, startDate);
        warranties.add(warranty);
        repository.saveAll(warranties);
        return warranty;
    }

    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) {
        validateAssignment(product, sale, startDate);
        List<Warranty> warranties = repository.loadAll();
        ExtendedWarranty warranty =
                new ExtendedWarranty(generateNextId(warranties), product, sale, startDate);
        warranties.add(warranty);
        repository.saveAll(warranties);
        return warranty;
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
